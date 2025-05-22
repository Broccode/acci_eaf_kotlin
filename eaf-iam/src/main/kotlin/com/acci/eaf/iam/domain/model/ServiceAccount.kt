package com.acci.eaf.iam.domain.model

import java.time.OffsetDateTime
import java.util.UUID

/**
 * Represents a Service Account for machine-to-machine authentication.
 */
data class ServiceAccount(
    /**
     * Unique identifier for the Service Account.
     */
    val serviceAccountId: UUID = UUID.randomUUID(),

    /**
     * Identifier of the tenant this service account belongs to.
     */
    val tenantId: UUID,

    /**
     * Client ID for the service account, unique per tenant. System-generated.
     */
    val clientId: String,

    /**
     * Hashed client secret.
     */
    val clientSecretHash: String,

    /**
     * Salt used for hashing the client secret.
     */
    val salt: String,

    /**
     * Optional description for the service account.
     */
    val description: String?,

    /**
     * Status of the service account.
     */
    val status: ServiceAccountStatus,

    /**
     * Set of Role IDs assigned to this service account.
     * Assuming Role entity has a UUID as its ID.
     */
    val roles: Set<UUID> = emptySet(),

    /**
     * Timestamp when the service account was created.
     */
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    /**
     * Timestamp when the service account expires. Nullable for no expiration.
     */
    val expiresAt: OffsetDateTime?,
)

/**
 * Enum representing the status of a Service Account.
 */
enum class ServiceAccountStatus {
    ACTIVE,
    INACTIVE,
}
