package com.acci.eaf.controlplane.api.dto

import java.time.OffsetDateTime
import java.util.UUID

/**
 * Control Plane API DTO for creating service accounts.
 * This is the external API contract, separate from internal Application DTOs.
 */
data class ServiceAccountCreateRequest(
    val description: String?,
    val expiresAt: OffsetDateTime?, // null for default expiration
    val roles: Set<UUID>? = emptySet(),
)
