package com.acci.eaf.iam.adapter.persistence

import com.acci.eaf.iam.audit.AuditService
import com.acci.eaf.iam.domain.event.ServiceAccountActivatedEvent
import com.acci.eaf.iam.domain.event.ServiceAccountCreatedEvent
import com.acci.eaf.iam.domain.event.ServiceAccountDeactivatedEvent
import com.acci.eaf.iam.domain.event.ServiceAccountDetailsUpdatedEvent
import com.acci.eaf.iam.domain.event.ServiceAccountRolesAssignedEvent
import com.acci.eaf.iam.domain.event.ServiceAccountRolesRemovedEvent
import com.acci.eaf.iam.domain.event.ServiceAccountSecretRotatedEvent
import org.axonframework.eventhandling.EventHandler
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ServiceAccountProjection(private val jpaRepository: ServiceAccountJpaRepository, private val auditService: AuditService) {

    private val logger = LoggerFactory.getLogger(ServiceAccountProjection::class.java)

    @EventHandler
    fun on(event: ServiceAccountCreatedEvent) {
        logger.debug("Handling ServiceAccountCreatedEvent for service account: {}", event.serviceAccountId)

        val entity = ServiceAccountEntity(
            serviceAccountId = event.serviceAccountId,
            tenantId = event.tenantId,
            clientId = event.clientId,
            clientSecretHash = event.clientSecretHash,
            salt = event.salt,
            description = event.description,
            status = event.status,
            createdAt = event.createdAt,
            expiresAt = event.expiresAt
        )

        jpaRepository.save(entity)
        logger.info("Created service account read model: {} for tenant: {}", event.serviceAccountId, event.tenantId)

        // Audit logging
        auditService.logServiceAccountCreated(
            serviceAccountId = event.serviceAccountId,
            clientId = event.clientId,
            tenantId = event.tenantId,
            description = event.description,
            expiresAt = event.expiresAt,
            initiatedBy = event.initiatedBy
        )
    }

    @EventHandler
    fun on(event: ServiceAccountDetailsUpdatedEvent) {
        logger.debug("Handling ServiceAccountDetailsUpdatedEvent for service account: {}", event.serviceAccountId)

        val entity = jpaRepository.findById(event.serviceAccountId).orElse(null)
        if (entity != null) {
            val updatedFields = mutableMapOf<String, Any?>()

            if (entity.description != event.description) {
                updatedFields["description"] = event.description
                entity.description = event.description
            }
            if (entity.status != event.status) {
                updatedFields["status"] = event.status
                entity.status = event.status
            }
            if (entity.expiresAt != event.expiresAt) {
                updatedFields["expiresAt"] = event.expiresAt
                entity.expiresAt = event.expiresAt
            }

            jpaRepository.save(entity)
            logger.info("Updated service account read model: {}", event.serviceAccountId)

            // Audit logging only if there were actual changes
            if (updatedFields.isNotEmpty()) {
                auditService.logServiceAccountDetailsUpdated(
                    serviceAccountId = event.serviceAccountId,
                    clientId = entity.clientId,
                    tenantId = entity.tenantId,
                    updatedFields = updatedFields,
                    initiatedBy = event.initiatedBy
                )
            }
        } else {
            logger.warn("Service account entity not found for update: {}", event.serviceAccountId)
        }
    }

    @EventHandler
    fun on(event: ServiceAccountSecretRotatedEvent) {
        logger.debug("Handling ServiceAccountSecretRotatedEvent for service account: {}", event.serviceAccountId)

        val entity = jpaRepository.findById(event.serviceAccountId).orElse(null)
        if (entity != null) {
            entity.clientSecretHash = event.newClientSecretHash
            entity.salt = event.newSalt

            jpaRepository.save(entity)
            logger.info("Rotated secret for service account read model: {}", event.serviceAccountId)

            // Audit logging
            auditService.logServiceAccountSecretRotated(
                serviceAccountId = event.serviceAccountId,
                clientId = entity.clientId,
                tenantId = entity.tenantId,
                initiatedBy = event.initiatedBy
            )
        } else {
            logger.warn("Service account entity not found for secret rotation: {}", event.serviceAccountId)
        }
    }

    @EventHandler
    fun on(event: ServiceAccountDeactivatedEvent) {
        logger.debug("Handling ServiceAccountDeactivatedEvent for service account: {}", event.serviceAccountId)

        val entity = jpaRepository.findById(event.serviceAccountId).orElse(null)
        if (entity != null) {
            entity.status = com.acci.eaf.iam.domain.model.ServiceAccountStatus.INACTIVE

            jpaRepository.save(entity)
            logger.info("Deactivated service account read model: {}", event.serviceAccountId)

            // Audit logging
            auditService.logServiceAccountDeactivated(
                serviceAccountId = event.serviceAccountId,
                clientId = entity.clientId,
                tenantId = entity.tenantId,
                initiatedBy = event.initiatedBy
            )
        } else {
            logger.warn("Service account entity not found for deactivation: {}", event.serviceAccountId)
        }
    }

    @EventHandler
    fun on(event: ServiceAccountActivatedEvent) {
        logger.debug("Handling ServiceAccountActivatedEvent for service account: {}", event.serviceAccountId)

        val entity = jpaRepository.findById(event.serviceAccountId).orElse(null)
        if (entity != null) {
            entity.status = com.acci.eaf.iam.domain.model.ServiceAccountStatus.ACTIVE

            jpaRepository.save(entity)
            logger.info("Activated service account read model: {}", event.serviceAccountId)

            // Audit logging
            auditService.logServiceAccountActivated(
                serviceAccountId = event.serviceAccountId,
                clientId = entity.clientId,
                tenantId = entity.tenantId,
                initiatedBy = event.initiatedBy
            )
        } else {
            logger.warn("Service account entity not found for activation: {}", event.serviceAccountId)
        }
    }

    @EventHandler
    fun on(event: ServiceAccountRolesAssignedEvent) {
        logger.debug("Handling ServiceAccountRolesAssignedEvent for service account: {}", event.serviceAccountId)

        // Note: Role assignments will be handled by a separate projection for the service_account_roles table
        // This event handler is here for completeness and future extensions
        logger.info("Service account roles assigned (handled by separate roles projection): {}", event.serviceAccountId)

        // Get client ID for audit logging
        val entity = jpaRepository.findById(event.serviceAccountId).orElse(null)
        if (entity != null) {
            // Audit logging
            auditService.logServiceAccountRolesAssigned(
                serviceAccountId = event.serviceAccountId,
                clientId = entity.clientId,
                tenantId = entity.tenantId,
                roleIds = event.assignedRoles,
                initiatedBy = event.initiatedBy
            )
        }
    }

    @EventHandler
    fun on(event: ServiceAccountRolesRemovedEvent) {
        logger.debug("Handling ServiceAccountRolesRemovedEvent for service account: {}", event.serviceAccountId)

        // Note: Role removals will be handled by a separate projection for the service_account_roles table
        // This event handler is here for completeness and future extensions
        logger.info("Service account roles removed (handled by separate roles projection): {}", event.serviceAccountId)

        // Get client ID for audit logging
        val entity = jpaRepository.findById(event.serviceAccountId).orElse(null)
        if (entity != null) {
            // Audit logging
            auditService.logServiceAccountRolesRemoved(
                serviceAccountId = event.serviceAccountId,
                clientId = entity.clientId,
                tenantId = entity.tenantId,
                roleIds = event.removedRoles,
                initiatedBy = event.initiatedBy
            )
        }
    }
}
