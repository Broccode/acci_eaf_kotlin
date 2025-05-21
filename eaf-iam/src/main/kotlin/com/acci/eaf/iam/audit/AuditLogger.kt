package com.acci.eaf.iam.audit

import java.util.UUID
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

/**
 * Komponente für die Protokollierung von Audit-Events für Sicherheitsrelevante Aktivitäten.
 * Dies ist eine einfache Implementierung, die Logs in die Konsole schreibt.
 * In einem Produktionssystem würden diese Ereignisse in einer separaten Datenbank oder
 * einem spezialisierten Audit-Log-System gespeichert werden.
 */
@Component
class AuditLogger {
    private val logger = LoggerFactory.getLogger(AuditLogger::class.java)

    /**
     * Log a user creation event.
     */
    fun logUserCreation(userId: UUID, username: String, tenantId: UUID) {
        val actor = getCurrentActor()
        logger.info(
            "AUDIT: USER_CREATED - Actor: {}, UserId: {}, Username: {}, TenantId: {}",
            actor, userId, username, tenantId
        )
        // TODO: Integrate with central audit log
    }

    /**
     * Log a user update event.
     */
    fun logUserUpdate(
        userId: UUID,
        username: String,
        tenantId: UUID,
        updatedFields: Map<String, Any?>,
    ) {
        val actor = getCurrentActor()
        logger.info(
            "AUDIT: USER_UPDATED - Actor: {}, UserId: {}, Username: {}, TenantId: {}, UpdatedFields: {}",
            actor, userId, username, tenantId, updatedFields
        )
        // TODO: Integrate with central audit log
    }

    /**
     * Log a user password change event.
     */
    fun logPasswordChange(userId: UUID, username: String, tenantId: UUID) {
        val actor = getCurrentActor()
        logger.info(
            "AUDIT: USER_PASSWORD_CHANGED - Actor: {}, UserId: {}, Username: {}, TenantId: {}",
            actor, userId, username, tenantId
        )
        // TODO: Integrate with central audit log
    }

    /**
     * Log a user status change event.
     */
    fun logUserStatusChange(userId: UUID, username: String, tenantId: UUID, newStatus: String) {
        val actor = getCurrentActor()
        logger.info(
            "AUDIT: USER_STATUS_CHANGED - Actor: {}, UserId: {}, Username: {}, TenantId: {}, NewStatus: {}",
            actor, userId, username, tenantId, newStatus
        )
        // TODO: Integrate with central audit log
    }

    /**
     * Protokolliert eine erfolgreiche Authentifizierung.
     *
     * @param username der Benutzername
     * @param tenantId die ID des Tenants
     */
    fun logAuthenticationSuccess(username: String, tenantId: UUID) {
        logger.info("AUDIT: Authentication success for user '{}' in tenant '{}'", username, tenantId)
    }

    /**
     * Protokolliert eine fehlgeschlagene Authentifizierung.
     *
     * @param username der Benutzername
     * @param tenantId die ID des Tenants
     * @param reason der Grund für den Fehlschlag
     */
    fun logAuthenticationFailure(username: String, tenantId: UUID, reason: String) {
        logger.warn("AUDIT: Authentication failure for user '{}' in tenant '{}': {}", username, tenantId, reason)
    }

    /**
     * Protokolliert eine Kontosperrung.
     *
     * @param username der Benutzername
     * @param tenantId die ID des Tenants
     * @param reason der Grund für die Sperrung
     */
    fun logAccountLockout(username: String, tenantId: UUID, reason: String) {
        logger.warn("AUDIT: Account locked for user '{}' in tenant '{}': {}", username, tenantId, reason)
    }

    /**
     * Protokolliert eine Kontoentsperre.
     *
     * @param username der Benutzername
     * @param tenantId die ID des Tenants
     */
    fun logAccountUnlock(username: String, tenantId: UUID) {
        logger.info("AUDIT: Account unlocked for user '{}' in tenant '{}'", username, tenantId)
    }

    /**
     * Protokolliert eine Token-Generierung.
     *
     * @param username der Benutzername
     * @param tenantId die ID des Tenants
     * @param tokenType der Typ des Tokens (z.B. "access", "refresh")
     */
    fun logTokenGeneration(username: String, tenantId: UUID, tokenType: String) {
        logger.info("AUDIT: {} token generated for user '{}' in tenant '{}'", tokenType, username, tenantId)
    }

    /**
     * Get the current actor from the security context.
     */
    private fun getCurrentActor(): String =
        try {
            val authentication = SecurityContextHolder.getContext().authentication
            authentication?.name ?: "Anonymous"
        } catch (e: Exception) {
            "Unknown"
        }
}
