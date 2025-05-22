package com.acci.eaf.iam.domain.command

import com.acci.eaf.iam.domain.model.ServiceAccountStatus
import java.time.OffsetDateTime
import java.util.UUID
import org.axonframework.modelling.command.TargetAggregateIdentifier

// === Create Service Account ===
data class CreateServiceAccountCommand(
    // Marks serviceAccountId as the target for routing this command
    // ID for the new aggregate
    @TargetAggregateIdentifier
    val serviceAccountId: UUID = UUID.randomUUID(),
    val tenantId: UUID,
    val description: String?,
    // User's requested expiration, null for default
    val requestedExpiresAt: OffsetDateTime?,
    val requestedRoles: Set<UUID>?,
    // User/process that initiated the command for auditing
    val initiatedBy: String,
)

// === Update Service Account ===
data class UpdateServiceAccountDetailsCommand(
    @TargetAggregateIdentifier
    val serviceAccountId: UUID,
    // For validation, ensuring command targets correct tenant's aggregate
    val tenantId: UUID,
    // Consider Optional fields if explicit null vs. not-provided is important
    val description: String?,
    val status: ServiceAccountStatus?,
    // Nullable to allow clearing expiration if permitted
    val requestedExpiresAt: OffsetDateTime?,
    val initiatedBy: String,
)

// === Assign Roles to Service Account ===
data class AssignRolesToServiceAccountCommand(
    @TargetAggregateIdentifier
    val serviceAccountId: UUID,
    val tenantId: UUID,
    val rolesToAssign: Set<UUID>,
    val initiatedBy: String,
)

// === Remove Roles from Service Account ===
data class RemoveRolesFromServiceAccountCommand(
    @TargetAggregateIdentifier
    val serviceAccountId: UUID,
    val tenantId: UUID,
    val rolesToRemove: Set<UUID>,
    val initiatedBy: String,
)

// === Rotate Service Account Secret ===
data class RotateServiceAccountSecretCommand(
    @TargetAggregateIdentifier
    val serviceAccountId: UUID,
    val tenantId: UUID,
    val initiatedBy: String,
)

// === Deactivate Service Account ===
// (Soft delete by changing status to INACTIVE)
data class DeactivateServiceAccountCommand(
    @TargetAggregateIdentifier
    val serviceAccountId: UUID,
    val tenantId: UUID,
    val initiatedBy: String,
)

// === Activate Service Account ===
// (To re-activate an INACTIVE account)
data class ActivateServiceAccountCommand(
    @TargetAggregateIdentifier
    val serviceAccountId: UUID,
    val tenantId: UUID,
    val initiatedBy: String,
)
