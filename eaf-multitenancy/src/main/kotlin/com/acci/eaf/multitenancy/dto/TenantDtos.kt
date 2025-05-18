package com.acci.eaf.multitenancy.dto

import com.acci.eaf.multitenancy.domain.TenantStatus
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.Instant
import java.util.UUID

/**
 * Data Transfer Object representing a tenant for API responses.
 */
data class TenantDto(val tenantId: UUID, val name: String, val status: TenantStatus, val createdAt: Instant, val updatedAt: Instant)

/**
 * Data Transfer Object for creating a new tenant.
 */
data class CreateTenantDto(
    @field:NotBlank(message = "Tenant name cannot be blank")
    @field:Size(min = 3, max = 100, message = "Tenant name must be between 3 and 100 characters")
    @field:Pattern(
        regexp = "^[a-zA-Z0-9\\-]+$",
        message = "Tenant name can only contain alphanumeric characters and hyphens"
    )
    val name: String,

    val status: TenantStatus = TenantStatus.PENDING_VERIFICATION,
)

/**
 * Data Transfer Object for updating an existing tenant.
 */
data class UpdateTenantDto(
    @field:Size(min = 3, max = 100, message = "Tenant name must be between 3 and 100 characters")
    @field:Pattern(
        regexp = "^[a-zA-Z0-9\\-]+$",
        message = "Tenant name can only contain alphanumeric characters and hyphens"
    )
    val name: String? = null,

    val status: TenantStatus? = null,
)
