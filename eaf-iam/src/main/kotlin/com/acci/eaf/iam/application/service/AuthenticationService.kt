package com.acci.eaf.iam.application.service

import com.acci.eaf.iam.adapter.persistence.UserRepository
import com.acci.eaf.iam.adapter.rest.dto.LoginRequestDTO
import com.acci.eaf.iam.adapter.rest.dto.LoginResponseDTO
import com.acci.eaf.iam.audit.AuditLogger
import com.acci.eaf.iam.config.JwtTokenProvider
import com.acci.eaf.iam.domain.exception.AuthenticationException
import com.acci.eaf.iam.domain.model.User
import com.acci.eaf.iam.domain.model.UserStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

/**
 * Service für die Authentifizierung von Benutzern.
 */
@Service
class AuthenticationService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val accountLockoutService: AccountLockoutService,
    private val auditLogger: AuditLogger
) {

    /**
     * Authentifiziert einen Benutzer mit Benutzername/E-Mail und Passwort.
     *
     * @param loginRequest die Login-Anfrage mit Benutzername/E-Mail, Passwort und optional Tenant-Hint
     * @return LoginResponseDTO mit Access Token und ggf. Refresh Token
     * @throws AuthenticationException wenn die Authentifizierung fehlschlägt
     */
    @Transactional
    fun authenticate(loginRequest: LoginRequestDTO): LoginResponseDTO {
        // Extrahiere Tenant-ID und Benutzernamen aus der Anfrage
        val (username, tenantId) = extractUsernameAndTenant(loginRequest)

        // Prüfe, ob das Konto gesperrt ist
        if (accountLockoutService.isAccountLocked(username, tenantId)) {
            auditLogger.logAuthenticationFailure(username, tenantId, "Konto ist gesperrt")
            throw AuthenticationException("Ungültige Anmeldeinformationen")
        }

        try {
            // Benutzer aus der Datenbank abrufen
            val user = findUser(username, tenantId)

            // Passwort überprüfen
            if (!passwordEncoder.matches(loginRequest.password, user.passwordHash)) {
                handleFailedAuthentication(username, tenantId, "Ungültiges Passwort")
            }

            // Prüfen, ob der Benutzer aktiv ist
            if (!user.isActive()) {
                handleFailedAuthentication(username, tenantId, "Benutzer ist nicht aktiv: ${user.status}")
            }

            // Erfolgreiche Authentifizierung
            return handleSuccessfulAuthentication(user)
        } catch (e: Exception) {
            when (e) {
                is AuthenticationException -> throw e
                else -> {
                    handleFailedAuthentication(username, tenantId, "Benutzer nicht gefunden oder interner Fehler")
                    throw AuthenticationException("Ungültige Anmeldeinformationen")
                }
            }
        }
    }

    /**
     * Extrahiert Benutzername und Tenant-ID aus der Login-Anfrage.
     * Unterstützt Formate wie "benutzer@tenant" oder separate Tenant-Hint-Parameter.
     *
     * @param loginRequest die Login-Anfrage
     * @return Pair mit Benutzername und Tenant-ID
     * @throws AuthenticationException wenn kein Tenant abgeleitet werden kann
     */
    private fun extractUsernameAndTenant(loginRequest: LoginRequestDTO): Pair<String, UUID> {
        // Hier müsste eigentlich ein TenantResolver-Service verwendet werden,
        // der anhand eines Tenant-Identifiers (Namen, Subdomains, etc.) die Tenant-ID ermittelt.
        // Für diese Implementierung nehmen wir an, dass der Tenant-Hint direkt die UUID ist.

        var username = loginRequest.usernameOrEmail
        var tenantIdStr = loginRequest.tenantHint

        // Prüfen, ob der Benutzername das Format "benutzer@tenant" hat
        if (username.contains("@")) {
            val parts = username.split("@", limit = 2)
            username = parts[0]
            tenantIdStr = parts[1]
        }

        // Prüfen, ob ein Tenant angegeben wurde
        if (tenantIdStr.isNullOrBlank()) {
            throw AuthenticationException("Kein Tenant angegeben")
        }

        try {
            return Pair(username, UUID.fromString(tenantIdStr))
        } catch (e: IllegalArgumentException) {
            // Hier würde normalerweise der TenantResolver die ID anhand des Namens auflösen
            throw AuthenticationException("Ungültiger Tenant-Identifier")
        }
    }

    /**
     * Sucht einen Benutzer anhand des Benutzernamens oder der E-Mail-Adresse.
     *
     * @param usernameOrEmail der Benutzername oder die E-Mail-Adresse
     * @param tenantId die ID des Tenants
     * @return der gefundene Benutzer
     * @throws AuthenticationException wenn der Benutzer nicht gefunden wurde
     */
    private fun findUser(usernameOrEmail: String, tenantId: UUID): User {
        val userByUsername = userRepository.findByUsernameAndTenantId(usernameOrEmail, tenantId)
        if (userByUsername.isPresent) {
            return userByUsername.get()
        }

        // Wenn der Benutzer nicht über den Benutzernamen gefunden wurde, versuche es mit der E-Mail-Adresse
        val userByEmail = userRepository.findByEmailAndTenantId(usernameOrEmail, tenantId)
        if (userByEmail.isPresent) {
            return userByEmail.get()
        }

        // Wenn der Benutzer weder über Benutzernamen noch E-Mail gefunden wurde, wirf eine Ausnahme
        throw AuthenticationException("Benutzer nicht gefunden")
    }

    /**
     * Behandelt eine erfolgreiche Authentifizierung.
     *
     * @param user der authentifizierte Benutzer
     * @return LoginResponseDTO mit Access Token und ggf. Refresh Token
     */
    @Transactional
    private fun handleSuccessfulAuthentication(user: User): LoginResponseDTO {
        // Fehlgeschlagene Anmeldeversuche zurücksetzen
        accountLockoutService.resetFailedAttempts(user.username, user.tenantId)

        // Letzten Login-Zeitpunkt aktualisieren
        user.lastLoginAt = Instant.now()
        userRepository.save(user)

        // Audit-Log für erfolgreiche Anmeldung
        auditLogger.logAuthenticationSuccess(user.username, user.tenantId)

        // Tokens generieren
        val accessToken = jwtTokenProvider.generateAccessToken(user)
        val refreshToken = jwtTokenProvider.generateRefreshToken(user)

        return LoginResponseDTO(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = jwtTokenProvider.getAccessTokenExpirationInSeconds()
        )
    }

    /**
     * Behandelt eine fehlgeschlagene Authentifizierung.
     *
     * @param username der Benutzername
     * @param tenantId die ID des Tenants
     * @param reason der Grund für den Fehlschlag
     */
    private fun handleFailedAuthentication(username: String, tenantId: UUID, reason: String) {
        // Fehlgeschlagenen Anmeldeversuch registrieren
        val locked = accountLockoutService.recordFailedAttempt(username, tenantId)

        // Audit-Log für fehlgeschlagene Anmeldung
        auditLogger.logAuthenticationFailure(username, tenantId, reason)

        // Generische Fehlermeldung werfen, um keine Informationen preiszugeben
        throw AuthenticationException("Ungültige Anmeldeinformationen")
    }
}
