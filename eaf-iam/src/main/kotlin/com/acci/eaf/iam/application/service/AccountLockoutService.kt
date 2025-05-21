package com.acci.eaf.iam.application.service

import com.acci.eaf.iam.adapter.persistence.UserRepository
import com.acci.eaf.iam.domain.model.User
import com.acci.eaf.iam.domain.model.UserStatus
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Service zur Verwaltung der Kontosperrung nach zu vielen fehlgeschlagenen Anmeldeversuchen.
 */
@Service
class AccountLockoutService(
    private val userRepository: UserRepository,
    @Value("\${app.security.lockout.max-attempts:5}") private val maxFailedAttempts: Int,
    @Value("\${app.security.lockout.duration-minutes:15}") private val lockoutDurationMinutes: Long
) {
    // In-Memory-Cache für fehlgeschlagene Anmeldeversuche
    // In einer Produktionsumgebung sollte ein verteilter Cache oder eine Datenbank verwendet werden
    private val failedAttempts = ConcurrentHashMap<String, MutableList<Instant>>()
    private val lockedAccounts = ConcurrentHashMap<String, Instant>()

    /**
     * Registriert einen fehlgeschlagenen Anmeldeversuch und prüft, ob das Konto gesperrt werden sollte.
     *
     * @param username der Benutzername
     * @param tenantId die ID des Tenants
     * @return true, wenn das Konto gesperrt wurde, sonst false
     */
    @Transactional
    fun recordFailedAttempt(username: String, tenantId: UUID): Boolean {
        val key = "${username}@${tenantId}"

        // Prüfen, ob das Konto bereits gesperrt ist
        if (isAccountLocked(username, tenantId)) {
            return true
        }

        // Aktuelle Zeit erfassen
        val now = Instant.now()

        // Fehlgeschlagene Versuche für diesen Benutzer abrufen oder initialisieren
        val attempts = failedAttempts.computeIfAbsent(key) { mutableListOf() }

        // Alte Versuche (älter als die Sperrzeit) entfernen
        val cutoff = now.minus(Duration.ofMinutes(lockoutDurationMinutes))
        attempts.removeIf { it.isBefore(cutoff) }

        // Neuen Versuch hinzufügen
        attempts.add(now)

        // Wenn die maximale Anzahl an Versuchen erreicht wurde, Konto sperren
        if (attempts.size >= maxFailedAttempts) {
            lockAccount(username, tenantId)
            return true
        }

        return false
    }

    /**
     * Prüft, ob ein Konto gesperrt ist.
     *
     * @param username der Benutzername
     * @param tenantId die ID des Tenants
     * @return true, wenn das Konto gesperrt ist, sonst false
     */
    fun isAccountLocked(username: String, tenantId: UUID): Boolean {
        val key = "${username}@${tenantId}"
        val lockTime = lockedAccounts[key] ?: return false

        // Prüfen, ob die Sperrzeit abgelaufen ist
        if (Instant.now().isAfter(lockTime.plus(Duration.ofMinutes(lockoutDurationMinutes)))) {
            // Sperre aufheben, wenn die Zeit abgelaufen ist
            lockedAccounts.remove(key)
            failedAttempts.remove(key)
            unlockAccount(username, tenantId)
            return false
        }

        return true
    }

    /**
     * Zurücksetzen der fehlgeschlagenen Anmeldeversuche nach einer erfolgreichen Anmeldung.
     *
     * @param username der Benutzername
     * @param tenantId die ID des Tenants
     */
    fun resetFailedAttempts(username: String, tenantId: UUID) {
        val key = "${username}@${tenantId}"
        failedAttempts.remove(key)
    }

    /**
     * Sperrt ein Benutzerkonto.
     *
     * @param username der Benutzername
     * @param tenantId die ID des Tenants
     */
    @Transactional
    fun lockAccount(username: String, tenantId: UUID) {
        val key = "${username}@${tenantId}"
        lockedAccounts[key] = Instant.now()

        userRepository.findByUsernameAndTenantId(username, tenantId).ifPresent { user ->
            user.status = UserStatus.LOCKED_BY_SYSTEM
            userRepository.save(user)
        }
    }

    /**
     * Entsperrt ein Benutzerkonto.
     *
     * @param username der Benutzername
     * @param tenantId die ID des Tenants
     */
    @Transactional
    fun unlockAccount(username: String, tenantId: UUID) {
        userRepository.findByUsernameAndTenantId(username, tenantId).ifPresent { user ->
            if (user.status == UserStatus.LOCKED_BY_SYSTEM) {
                user.status = UserStatus.ACTIVE
                userRepository.save(user)
            }
        }
    }
}
