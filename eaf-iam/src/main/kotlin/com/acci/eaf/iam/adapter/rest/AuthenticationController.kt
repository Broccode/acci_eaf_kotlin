package com.acci.eaf.iam.adapter.rest

import com.acci.eaf.iam.adapter.rest.dto.LoginRequestDTO
import com.acci.eaf.iam.adapter.rest.dto.LoginResponseDTO
import com.acci.eaf.iam.application.service.AuthenticationService
import com.acci.eaf.iam.domain.exception.AuthenticationException
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

/**
 * Controller für Authentifizierungsendpunkte.
 */
@RestController
@RequestMapping("/api/iam/auth")
class AuthenticationController(private val authenticationService: AuthenticationService) {

    private val logger = LoggerFactory.getLogger(AuthenticationController::class.java)

    /**
     * Endpunkt für die Benutzerauthentifizierung.
     *
     * @param loginRequest die Login-Anfrage mit Benutzername/E-Mail, Passwort und optional Tenant-Hint
     * @return LoginResponseDTO mit Access Token und ggf. Refresh Token
     */
    @PostMapping("/login")
    fun login(@Valid @RequestBody loginRequest: LoginRequestDTO): ResponseEntity<LoginResponseDTO> =
        try {
            val response = authenticationService.authenticate(loginRequest)
            ResponseEntity.ok(response)
        } catch (e: AuthenticationException) {
            logger.debug("Authentifizierungsfehler: {}", e.message)
            // Generische Fehlermeldung werfen, um keine Informationen preiszugeben
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Ungültige Anmeldeinformationen")
        } catch (e: Exception) {
            logger.error("Unerwarteter Fehler bei der Authentifizierung", e)
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ein Fehler ist aufgetreten")
        }
}
