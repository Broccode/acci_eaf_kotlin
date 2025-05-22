package com.acci.eaf.controlplane.api.dto

import java.time.OffsetDateTime
import java.util.UUID

/**
 * Control Plane API DTO for updating service accounts.
 * Separate from Application Layer DTOs.
 */
data class ServiceAccountUpdateRequest(
    val description: String?,
    val status: ServiceAccountStatusResponse?,
    val expiresAt: OffsetDateTime?,
    val roles: Set<UUID>? = null,
)
