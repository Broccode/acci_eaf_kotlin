package com.acci.eaf.iam.domain.aggregate

import com.acci.eaf.iam.config.ServiceAccountProperties
import com.acci.eaf.iam.domain.command.ActivateServiceAccountCommand
import com.acci.eaf.iam.domain.command.AssignRolesToServiceAccountCommand
import com.acci.eaf.iam.domain.command.CreateServiceAccountCommand
import com.acci.eaf.iam.domain.command.DeactivateServiceAccountCommand
import com.acci.eaf.iam.domain.command.RemoveRolesFromServiceAccountCommand
import com.acci.eaf.iam.domain.command.RotateServiceAccountSecretCommand
import com.acci.eaf.iam.domain.command.UpdateServiceAccountDetailsCommand
import com.acci.eaf.iam.domain.event.ServiceAccountActivatedEvent
import com.acci.eaf.iam.domain.event.ServiceAccountCreatedEvent
import com.acci.eaf.iam.domain.event.ServiceAccountDeactivatedEvent
import com.acci.eaf.iam.domain.event.ServiceAccountDetailsUpdatedEvent
import com.acci.eaf.iam.domain.event.ServiceAccountRolesAssignedEvent
import com.acci.eaf.iam.domain.event.ServiceAccountRolesRemovedEvent
import com.acci.eaf.iam.domain.event.ServiceAccountSecretRotatedEvent
import com.acci.eaf.iam.domain.exception.ServiceAccountNotFoundException
import com.acci.eaf.iam.domain.exception.ServiceAccountValidationException
import com.acci.eaf.iam.domain.model.ServiceAccountStatus
import com.acci.eaf.iam.domain.service.ServiceAccountCredentialsService
import java.time.OffsetDateTime
import java.util.UUID
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate

@Aggregate
class ServiceAccountAggregate {

    @AggregateIdentifier
    private lateinit var serviceAccountId: UUID
    private lateinit var tenantId: UUID
    private lateinit var clientId: String
    private lateinit var clientSecretHash: String
    private lateinit var salt: String
    private var description: String? = null
    private lateinit var status: ServiceAccountStatus
    private var roles: MutableSet<UUID> = mutableSetOf()
    private lateinit var createdAt: OffsetDateTime
    private var expiresAt: OffsetDateTime? = null

    // Default constructor required by Axon
    constructor()

    // === Command Handlers ===

    @CommandHandler
    constructor(
        command: CreateServiceAccountCommand,
        credentialsService: ServiceAccountCredentialsService,
        properties: ServiceAccountProperties,
    ) {
        // Validation
        validateTenantId(command.tenantId)
        validateDescription(command.description)
        validateExpirationDate(command.requestedExpiresAt, properties)

        // Generate credentials
        val clientId = credentialsService.generateClientId()
        val clientSecret = credentialsService.generateClientSecret()
        val (secretHash, salt) = credentialsService.hashClientSecret(clientSecret)

        // Determine expiration date
        val now = OffsetDateTime.now()
        val expiresAt = determineExpirationDate(command.requestedExpiresAt, now, properties)

        // Apply the creation event
        AggregateLifecycle.apply(
            ServiceAccountCreatedEvent(
                serviceAccountId = command.serviceAccountId,
                tenantId = command.tenantId,
                clientId = clientId,
                clientSecretHash = secretHash,
                salt = salt,
                description = command.description,
                status = ServiceAccountStatus.ACTIVE,
                roles = command.requestedRoles ?: emptySet(),
                createdAt = now,
                expiresAt = expiresAt,
                initiatedBy = command.initiatedBy
            )
        )
    }

    @CommandHandler
    fun handle(command: UpdateServiceAccountDetailsCommand, properties: ServiceAccountProperties) {
        validateAggregateExists()
        validateTenantMatches(command.tenantId)
        validateDescription(command.description)

        // Only validate expiration if it's being changed
        if (command.requestedExpiresAt != null) {
            validateExpirationDate(command.requestedExpiresAt, properties)
        }

        // Check if anything actually changed
        val newDescription = command.description ?: this.description
        val newStatus = command.status ?: this.status
        val newExpiresAt = command.requestedExpiresAt ?: this.expiresAt

        if (newDescription == this.description &&
            newStatus == this.status &&
            newExpiresAt == this.expiresAt
        ) {
            // No changes, no event needed
            return
        }

        AggregateLifecycle.apply(
            ServiceAccountDetailsUpdatedEvent(
                serviceAccountId = this.serviceAccountId,
                description = newDescription,
                status = newStatus,
                expiresAt = newExpiresAt,
                initiatedBy = command.initiatedBy
            )
        )
    }

    @CommandHandler
    fun handle(command: AssignRolesToServiceAccountCommand) {
        validateAggregateExists()
        validateTenantMatches(command.tenantId)
        validateRoles(command.rolesToAssign)

        val newRoles = command.rolesToAssign.filter { !this.roles.contains(it) }.toSet()
        if (newRoles.isEmpty()) {
            // No new roles to assign
            return
        }

        val allEffectiveRoles = this.roles + newRoles

        AggregateLifecycle.apply(
            ServiceAccountRolesAssignedEvent(
                serviceAccountId = this.serviceAccountId,
                assignedRoles = newRoles,
                allEffectiveRoles = allEffectiveRoles,
                initiatedBy = command.initiatedBy
            )
        )
    }

    @CommandHandler
    fun handle(command: RemoveRolesFromServiceAccountCommand) {
        validateAggregateExists()
        validateTenantMatches(command.tenantId)
        validateRoles(command.rolesToRemove)

        val rolesToRemove = command.rolesToRemove.filter { this.roles.contains(it) }.toSet()
        if (rolesToRemove.isEmpty()) {
            // No roles to remove
            return
        }

        val allEffectiveRoles = this.roles - rolesToRemove

        AggregateLifecycle.apply(
            ServiceAccountRolesRemovedEvent(
                serviceAccountId = this.serviceAccountId,
                removedRoles = rolesToRemove,
                allEffectiveRoles = allEffectiveRoles,
                initiatedBy = command.initiatedBy
            )
        )
    }

    @CommandHandler
    fun handle(command: RotateServiceAccountSecretCommand, credentialsService: ServiceAccountCredentialsService) {
        validateAggregateExists()
        validateTenantMatches(command.tenantId)

        // Generate new credentials
        val newClientSecret = credentialsService.generateClientSecret()
        val (newSecretHash, newSalt) = credentialsService.hashClientSecret(newClientSecret)

        AggregateLifecycle.apply(
            ServiceAccountSecretRotatedEvent(
                serviceAccountId = this.serviceAccountId,
                newClientSecretHash = newSecretHash,
                newSalt = newSalt,
                initiatedBy = command.initiatedBy
            )
        )
    }

    @CommandHandler
    fun handle(command: DeactivateServiceAccountCommand) {
        validateAggregateExists()
        validateTenantMatches(command.tenantId)

        if (this.status == ServiceAccountStatus.INACTIVE) {
            // Already inactive, no event needed
            return
        }

        AggregateLifecycle.apply(
            ServiceAccountDeactivatedEvent(
                serviceAccountId = this.serviceAccountId,
                initiatedBy = command.initiatedBy
            )
        )
    }

    @CommandHandler
    fun handle(command: ActivateServiceAccountCommand) {
        validateAggregateExists()
        validateTenantMatches(command.tenantId)

        if (this.status == ServiceAccountStatus.ACTIVE) {
            // Already active, no event needed
            return
        }

        AggregateLifecycle.apply(
            ServiceAccountActivatedEvent(
                serviceAccountId = this.serviceAccountId,
                initiatedBy = command.initiatedBy
            )
        )
    }

    // === Event Sourcing Handlers ===

    @EventSourcingHandler
    fun on(event: ServiceAccountCreatedEvent) {
        this.serviceAccountId = event.serviceAccountId
        this.tenantId = event.tenantId
        this.clientId = event.clientId
        this.clientSecretHash = event.clientSecretHash
        this.salt = event.salt
        this.description = event.description
        this.status = event.status
        this.roles = event.roles.toMutableSet()
        this.createdAt = event.createdAt
        this.expiresAt = event.expiresAt
    }

    @EventSourcingHandler
    fun on(event: ServiceAccountDetailsUpdatedEvent) {
        this.description = event.description
        this.status = event.status
        this.expiresAt = event.expiresAt
    }

    @EventSourcingHandler
    fun on(event: ServiceAccountRolesAssignedEvent) {
        this.roles.addAll(event.assignedRoles)
    }

    @EventSourcingHandler
    fun on(event: ServiceAccountRolesRemovedEvent) {
        this.roles.removeAll(event.removedRoles)
    }

    @EventSourcingHandler
    fun on(event: ServiceAccountSecretRotatedEvent) {
        this.clientSecretHash = event.newClientSecretHash
        this.salt = event.newSalt
    }

    @EventSourcingHandler
    fun on(event: ServiceAccountDeactivatedEvent) {
        this.status = ServiceAccountStatus.INACTIVE
    }

    @EventSourcingHandler
    fun on(event: ServiceAccountActivatedEvent) {
        this.status = ServiceAccountStatus.ACTIVE
    }

    // === Private Validation Methods ===

    private fun validateAggregateExists() {
        if (!this::serviceAccountId.isInitialized) {
            throw ServiceAccountNotFoundException("Service account not found")
        }
    }

    private fun validateTenantMatches(tenantId: UUID) {
        if (this.tenantId != tenantId) {
            throw ServiceAccountValidationException("Tenant ID mismatch")
        }
    }

    private fun validateTenantId(tenantId: UUID?) {
        if (tenantId == null) {
            throw ServiceAccountValidationException("Tenant ID cannot be null")
        }
    }

    private fun validateDescription(description: String?) {
        if (description != null && description.length > 1024) {
            throw ServiceAccountValidationException("Description cannot exceed 1024 characters")
        }
    }

    private fun validateExpirationDate(expiresAt: OffsetDateTime?, properties: ServiceAccountProperties) {
        if (expiresAt != null) {
            val now = OffsetDateTime.now()
            if (expiresAt.isBefore(now)) {
                throw ServiceAccountValidationException("Expiration date cannot be in the past")
            }

            val maxAllowedExpiration = now.plus(properties.maxExpiration)
            if (expiresAt.isAfter(maxAllowedExpiration)) {
                throw ServiceAccountValidationException(
                    "Expiration date cannot exceed maximum allowed period of ${properties.maxExpiration}"
                )
            }
        }
    }

    private fun validateRoles(roles: Set<UUID>?) {
        if (roles.isNullOrEmpty()) {
            throw ServiceAccountValidationException("At least one role must be specified")
        }
    }

    private fun determineExpirationDate(
        requestedExpiresAt: OffsetDateTime?,
        createdAt: OffsetDateTime,
        properties: ServiceAccountProperties,
    ): OffsetDateTime? =
        when {
            requestedExpiresAt != null -> requestedExpiresAt
            properties.allowNoExpiration -> null
            else -> createdAt.plus(properties.defaultExpiration)
        }
}
