package com.acci.eaf.controlplane.api.controller

import com.acci.eaf.controlplane.api.audit.AuditLogger
import com.acci.eaf.controlplane.api.dto.CreateTenantRequestDto
import com.acci.eaf.controlplane.api.dto.PagedTenantsResponseDto
import com.acci.eaf.controlplane.api.dto.TenantPageParams
import com.acci.eaf.controlplane.api.dto.TenantResponseDto
import com.acci.eaf.controlplane.api.dto.UpdateTenantRequestDto
import com.acci.eaf.controlplane.api.mapper.TenantMapperInterface
import com.acci.eaf.controlplane.api.service.TenantPageService
import com.acci.eaf.multitenancy.domain.TenantStatus
import com.acci.eaf.multitenancy.service.TenantService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import java.util.UUID
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

/**
 * REST Controller for tenant management operations.
 */
@RestController
@RequestMapping("/tenants")
@Validated
@Tag(name = "Tenants", description = "API for tenant management operations")
@SecurityRequirement(name = "bearerAuth")
class TenantController(
    private val tenantService: TenantService,
    private val tenantPageService: TenantPageService,
    private val tenantMapper: TenantMapperInterface,
    private val auditLogger: AuditLogger,
) {

    /**
     * Create a new tenant.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Create a new tenant",
        description = "Creates a new tenant with the given details. Requires ADMIN role.",
        responses = [
            ApiResponse(responseCode = "201", description = "Tenant created successfully"),
            ApiResponse(responseCode = "400", description = "Invalid input data"),
            ApiResponse(responseCode = "409", description = "Tenant with the same name already exists"),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Forbidden")
        ]
    )
    fun createTenant(@Valid @RequestBody requestDto: CreateTenantRequestDto): ResponseEntity<TenantResponseDto> {
        val createTenantDto = tenantMapper.toServiceDto(requestDto)
        val createdTenant = tenantService.createTenant(createTenantDto)
        val responseDto = tenantMapper.toResponseDto(createdTenant)

        // Audit logging
        auditLogger.logTenantCreation(responseDto.tenantId, responseDto.name)

        // Build the location URI for the created resource - robuster Ansatz
        val location = try {
            // Zuerst versuchen, über den aktuellen Request zu gehen
            val servletRequestAttributes = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
            if (servletRequestAttributes != null) {
                ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{tenantId}")
                    .buildAndExpand(responseDto.tenantId)
                    .toUri()
            } else {
                // Fallback, wenn kein Request-Kontext verfügbar ist
                ServletUriComponentsBuilder
                    .fromPath("/tenants/{tenantId}")
                    .buildAndExpand(responseDto.tenantId)
                    .toUri()
            }
        } catch (e: Exception) {
            // Absoluter Fallback, wenn ServletUriComponentsBuilder fehlschlägt
            java.net.URI.create("/tenants/${responseDto.tenantId}")
        }

        return ResponseEntity
            .created(location)
            .body(responseDto)
    }

    /**
     * Get a paginated list of tenants with optional filtering.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "List tenants",
        description = "Returns a paginated list of tenants with optional filtering. Requires ADMIN role.",
        responses = [
            ApiResponse(responseCode = "200", description = "Success"),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Forbidden")
        ]
    )
    fun getTenants(
        @Parameter(description = "Page number (0-based)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(defaultValue = "20") size: Int,
        @Parameter(description = "Filter by tenant status")
        @RequestParam(required = false) status: TenantStatus?,
        @Parameter(description = "Filter by tenant name (contains)")
        @RequestParam(required = false) nameContains: String?,
    ): ResponseEntity<PagedTenantsResponseDto> {
        val pageParams = TenantPageParams(
            page = page,
            size = size,
            status = status,
            nameContains = nameContains
        )

        val tenantPage = tenantPageService.getTenants(pageParams)
        val responseDto = tenantMapper.toPagedResponseDto(tenantPage)

        return ResponseEntity.ok(responseDto)
    }

    /**
     * Get a tenant by ID.
     */
    @GetMapping("/{tenantId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get tenant by ID",
        description = "Returns a specific tenant by its ID. Requires ADMIN role.",
        responses = [
            ApiResponse(responseCode = "200", description = "Success"),
            ApiResponse(responseCode = "404", description = "Tenant not found"),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Forbidden")
        ]
    )
    fun getTenantById(
        @Parameter(description = "Tenant ID", required = true)
        @PathVariable tenantId: UUID,
    ): ResponseEntity<TenantResponseDto> {
        val tenantDto = tenantService.getTenantById(tenantId)
        val responseDto = tenantMapper.toResponseDto(tenantDto)

        return ResponseEntity.ok(responseDto)
    }

    /**
     * Update a tenant.
     */
    @PutMapping("/{tenantId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Update tenant",
        description = "Updates an existing tenant. Requires ADMIN role.",
        responses = [
            ApiResponse(responseCode = "200", description = "Tenant updated successfully"),
            ApiResponse(responseCode = "400", description = "Invalid input data"),
            ApiResponse(responseCode = "404", description = "Tenant not found"),
            ApiResponse(responseCode = "409", description = "Tenant name conflict"),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Forbidden")
        ]
    )
    fun updateTenant(
        @Parameter(description = "Tenant ID", required = true)
        @PathVariable tenantId: UUID,
        @Valid @RequestBody requestDto: UpdateTenantRequestDto,
    ): ResponseEntity<TenantResponseDto> {
        val updateTenantDto = tenantMapper.toServiceDto(requestDto)
        val updatedTenant = tenantService.updateTenant(tenantId, updateTenantDto)
        val responseDto = tenantMapper.toResponseDto(updatedTenant)

        // Audit logging
        val updatedFields = mutableMapOf<String, Any?>()
        requestDto.name?.let { updatedFields["name"] = it }
        requestDto.status?.let { updatedFields["status"] = it }
        auditLogger.logTenantUpdate(tenantId, responseDto.name, updatedFields)

        return ResponseEntity.ok(responseDto)
    }

    /**
     * Delete (deactivate) a tenant.
     */
    @DeleteMapping("/{tenantId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Delete tenant",
        description = "Deactivates a tenant by changing its status to ARCHIVED. Requires ADMIN role.",
        responses = [
            ApiResponse(responseCode = "204", description = "Tenant deleted successfully"),
            ApiResponse(responseCode = "404", description = "Tenant not found"),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Forbidden")
        ]
    )
    fun deleteTenant(
        @Parameter(description = "Tenant ID", required = true)
        @PathVariable tenantId: UUID,
    ): ResponseEntity<Void> {
        // Get the tenant name before deletion for audit logging
        val tenant = tenantService.getTenantById(tenantId)

        // Perform the deletion (soft delete)
        tenantService.deleteTenant(tenantId)

        // Audit logging
        auditLogger.logTenantDeletion(tenantId, tenant.name)

        return ResponseEntity.noContent().build()
    }
}
