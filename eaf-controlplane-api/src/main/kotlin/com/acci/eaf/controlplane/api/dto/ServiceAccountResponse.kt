package com.acci.eaf.controlplane.api.dto

import java.time.OffsetDateTime
import java.util.UUID

/**
 * Control Plane API DTO for service account responses.
 * Separate from Application Layer DTOs to maintain clean boundaries.
 */
data class ServiceAccountResponse(
    val serviceAccountId: UUID,
    val clientId: String,
    val description: String?,
    val status: ServiceAccountStatusResponse,
    val createdAt: OffsetDateTime,
    val expiresAt: OffsetDateTime?,
    val roles: Set<UUID>,
)

/**
 * Status enum for API responses - separate from domain model
 */
enum class ServiceAccountStatusResponse {
    ACTIVE,
    INACTIVE,
}
