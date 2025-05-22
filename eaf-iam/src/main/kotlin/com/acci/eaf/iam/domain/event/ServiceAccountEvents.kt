package com.acci.eaf.iam.domain.event

import com.acci.eaf.iam.domain.model.ServiceAccountStatus
import java.time.OffsetDateTime
import java.util.UUID

// Event Sourcing: Events should capture what HAS HAPPENED in the past tense.

// === Service Account Created ===
data class ServiceAccountCreatedEvent(
    val serviceAccountId: UUID,
    val tenantId: UUID,
    val clientId: String,
    val clientSecretHash: String,
    val salt: String,
    val description: String?,
    val status: ServiceAccountStatus,
    val roles: Set<UUID>,
    val createdAt: OffsetDateTime,
    val expiresAt: OffsetDateTime?,
    val occurredOn: OffsetDateTime = OffsetDateTime.now(),
    val initiatedBy: String,
)

// === Service Account Details Updated ===
data class ServiceAccountDetailsUpdatedEvent(
    val serviceAccountId: UUID,
    val description: String?, // Captures the new state
    val status: ServiceAccountStatus, // Captures the new state
    val expiresAt: OffsetDateTime?, // Captures the new state
    val occurredOn: OffsetDateTime = OffsetDateTime.now(),
    val initiatedBy: String,
)

// === Service Account Roles Assigned ===
data class ServiceAccountRolesAssignedEvent(
    val serviceAccountId: UUID,
    val assignedRoles: Set<UUID>, // The roles that were newly assigned
    val allEffectiveRoles: Set<UUID>, // The complete set of roles after assignment
    val occurredOn: OffsetDateTime = OffsetDateTime.now(),
    val initiatedBy: String,
)

// === Service Account Roles Removed ===
data class ServiceAccountRolesRemovedEvent(
    val serviceAccountId: UUID,
    val removedRoles: Set<UUID>, // The roles that were removed
    val allEffectiveRoles: Set<UUID>, // The complete set of roles after removal
    val occurredOn: OffsetDateTime = OffsetDateTime.now(),
    val initiatedBy: String,
)

// === Service Account Secret Rotated ===
data class ServiceAccountSecretRotatedEvent(
    val serviceAccountId: UUID,
    val newClientSecretHash: String,
    val newSalt: String,
    val occurredOn: OffsetDateTime = OffsetDateTime.now(),
    val initiatedBy: String,
)

// === Service Account Deactivated ===
data class ServiceAccountDeactivatedEvent(val serviceAccountId: UUID, val occurredOn: OffsetDateTime = OffsetDateTime.now(), val initiatedBy: String)

// === Service Account Activated ===
data class ServiceAccountActivatedEvent(val serviceAccountId: UUID, val occurredOn: OffsetDateTime = OffsetDateTime.now(), val initiatedBy: String)
