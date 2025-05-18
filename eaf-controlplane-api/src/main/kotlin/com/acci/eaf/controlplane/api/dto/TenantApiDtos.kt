package com.acci.eaf.controlplane.api.dto

import com.acci.eaf.multitenancy.domain.TenantStatus
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.Instant
import java.util.UUID

/**
 * API response DTO for tenant data
 */
data class TenantResponseDto(
    val tenantId: UUID,
    val name: String,
    val status: TenantStatus,
    val createdAt: Instant,
    val updatedAt: Instant,
)

/**
 * API request DTO for creating a new tenant
 */
data class CreateTenantRequestDto(
    @field:NotBlank(message = "Tenant name cannot be blank")
    @field:Size(min = 3, max = 100, message = "Tenant name must be between 3 and 100 characters")
    @field:Pattern(
        regexp = "^[a-zA-Z0-9\\-]+$",
        message = "Tenant name can only contain alphanumeric characters and hyphens"
    )
    val name: String,

    val status: TenantStatus = TenantStatus.PENDING_VERIFICATION,

    @field:Pattern(
        regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
        message = "Invalid email format"
    )
    val adminEmail: String? = null,
)

/**
 * API request DTO for updating an existing tenant
 */
data class UpdateTenantRequestDto(
    @field:Size(min = 3, max = 100, message = "Tenant name must be between 3 and 100 characters")
    @field:Pattern(
        regexp = "^[a-zA-Z0-9\\-]+$",
        message = "Tenant name can only contain alphanumeric characters and hyphens"
    )
    val name: String? = null,

    val status: TenantStatus? = null,
)

/**
 * Pagination and filtering parameters for tenant listing
 */
data class TenantPageParams(
    val page: Int = 0,
    val size: Int = 20,
    val status: TenantStatus? = null,
    val nameContains: String? = null,
)

/**
 * API response DTO for paginated tenant data
 */
data class PagedTenantsResponseDto(
    val tenants: List<TenantResponseDto>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
) 
