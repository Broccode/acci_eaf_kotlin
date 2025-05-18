package com.acci.eaf.controlplane.api.audit

import java.util.UUID
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

/**
 * Simple audit logger for tenant operations.
 * This is a placeholder implementation until the central audit log (Story 10.7) is available.
 */
@Component
class AuditLogger {
    private val logger = LoggerFactory.getLogger(AuditLogger::class.java)

    /**
     * Log a tenant creation event.
     */
    fun logTenantCreation(tenantId: UUID, tenantName: String) {
        val actor = getCurrentActor()
        logger.info(
            "AUDIT: TENANT_CREATED - Actor: {}, TenantId: {}, TenantName: {}",
            actor, tenantId, tenantName
        )
        // TODO (EAF-XXX): Integrate with central audit log (Story 10.7)
    }

    /**
     * Log a tenant update event.
     */
    fun logTenantUpdate(
        tenantId: UUID,
        tenantName: String,
        updatedFields: Map<String, Any?>,
    ) {
        val actor = getCurrentActor()
        logger.info(
            "AUDIT: TENANT_UPDATED - Actor: {}, TenantId: {}, TenantName: {}, UpdatedFields: {}",
            actor, tenantId, tenantName, updatedFields
        )
        // TODO (EAF-XXX): Integrate with central audit log (Story 10.7)
    }

    /**
     * Log a tenant deletion (deactivation) event.
     */
    fun logTenantDeletion(tenantId: UUID, tenantName: String) {
        val actor = getCurrentActor()
        logger.info(
            "AUDIT: TENANT_DELETED - Actor: {}, TenantId: {}, TenantName: {}",
            actor, tenantId, tenantName
        )
        // TODO (EAF-XXX): Integrate with central audit log (Story 10.7)
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
