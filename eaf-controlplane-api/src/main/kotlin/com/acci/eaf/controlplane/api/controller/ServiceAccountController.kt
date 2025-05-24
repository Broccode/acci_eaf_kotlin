package com.acci.eaf.controlplane.api.controller

import com.acci.eaf.controlplane.api.dto.ServiceAccountCreateRequest
import com.acci.eaf.controlplane.api.dto.ServiceAccountResponse
import com.acci.eaf.controlplane.api.dto.ServiceAccountSecretResponse
import com.acci.eaf.controlplane.api.dto.ServiceAccountStatusResponse
import com.acci.eaf.controlplane.api.dto.ServiceAccountUpdateRequest
import com.acci.eaf.iam.application.port.api.CreateServiceAccountRequest as AppCreateRequest
import com.acci.eaf.iam.application.port.api.RotateSecretRequest
import com.acci.eaf.iam.application.port.api.ServiceAccountManagementService
import com.acci.eaf.iam.application.port.api.ServiceAccountStatusDto
import com.acci.eaf.iam.application.port.api.UpdateServiceAccountRequest as AppUpdateRequest
import java.util.UUID
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
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
import org.springframework.web.bind.annotation.RestController

// Potentially import a shared ServiceAccountManagementService interface from eaf-iam (application layer)
// import com.acci.eaf.iam.application.port.api.ServiceAccountManagementService

// Placeholder DTOs removed, actual DTOs are imported above.

// TODO: Temporarily disabled until IAM module is properly configured
// @RestController
@RequestMapping("/api/controlplane/tenants/{tenantId}/service-accounts")
@Validated // Enables validation of path variables, request parameters, etc.
class ServiceAccountController(private val serviceAccountManagementService: ServiceAccountManagementService) {

    // Subtask 3.2: POST /service-accounts for creating Service Accounts
    @PostMapping
    @PreAuthorize("hasAuthority('service_account:create') and @tenantSecurity.isUserInTenant(#tenantId)")
    fun createServiceAccount(
        @PathVariable tenantId: UUID,
        @RequestBody request: ServiceAccountCreateRequest,
    ): ResponseEntity<ServiceAccountSecretResponse> {
        val appRequest = AppCreateRequest(
            tenantId = tenantId,
            description = request.description,
            expiresAt = request.expiresAt,
            requestedRoles = request.roles,
            initiatedBy = "admin" // TODO: Get from security context
        )

        val result = serviceAccountManagementService.createServiceAccount(appRequest)

        val response = ServiceAccountSecretResponse(
            serviceAccountId = result.serviceAccount.serviceAccountId,
            clientId = result.serviceAccount.clientId,
            clientSecret = result.clientSecret
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    // Subtask 3.3: GET /service-accounts for listing Service Accounts
    @GetMapping
    @PreAuthorize("hasAuthority('service_account:read') and @tenantSecurity.isUserInTenant(#tenantId)")
    fun listServiceAccounts(@PathVariable tenantId: UUID, pageable: Pageable): ResponseEntity<Page<ServiceAccountResponse>> {
        val page = serviceAccountManagementService.listServiceAccounts(tenantId, pageable)
        val responsePage = page.map { dto ->
            ServiceAccountResponse(
                serviceAccountId = dto.serviceAccountId,
                clientId = dto.clientId,
                description = dto.description,
                status = mapStatusToResponse(dto.status),
                createdAt = dto.createdAt,
                expiresAt = dto.expiresAt,
                roles = dto.roles
            )
        }
        return ResponseEntity.ok(responsePage)
    }

    // Subtask 3.4: GET /service-accounts/{serviceAccountId} for retrieving details
    @GetMapping("/{serviceAccountId}")
    @PreAuthorize("hasAuthority('service_account:read') and @tenantSecurity.isUserInTenant(#tenantId)")
    fun getServiceAccountDetails(@PathVariable tenantId: UUID, @PathVariable serviceAccountId: UUID): ResponseEntity<ServiceAccountResponse> {
        val dto = serviceAccountManagementService.getServiceAccount(tenantId, serviceAccountId)
            ?: return ResponseEntity.notFound().build()

        val response = ServiceAccountResponse(
            serviceAccountId = dto.serviceAccountId,
            clientId = dto.clientId,
            description = dto.description,
            status = mapStatusToResponse(dto.status),
            createdAt = dto.createdAt,
            expiresAt = dto.expiresAt,
            roles = dto.roles
        )

        return ResponseEntity.ok(response)
    }

    // Subtask 3.5: PUT /service-accounts/{serviceAccountId} for updating
    @PutMapping("/{serviceAccountId}")
    @PreAuthorize("hasAuthority('service_account:update') and @tenantSecurity.isUserInTenant(#tenantId)")
    fun updateServiceAccount(
        @PathVariable tenantId: UUID,
        @PathVariable serviceAccountId: UUID,
        @RequestBody request: ServiceAccountUpdateRequest,
    ): ResponseEntity<ServiceAccountResponse> {
        val appRequest = AppUpdateRequest(
            tenantId = tenantId,
            serviceAccountId = serviceAccountId,
            description = request.description,
            status = request.status?.let { mapStatusFromResponse(it) },
            expiresAt = request.expiresAt,
            initiatedBy = "admin" // TODO: Get from security context
        )

        val updatedDto = serviceAccountManagementService.updateServiceAccount(appRequest)
            ?: return ResponseEntity.notFound().build()

        val response = ServiceAccountResponse(
            serviceAccountId = updatedDto.serviceAccountId,
            clientId = updatedDto.clientId,
            description = updatedDto.description,
            status = mapStatusToResponse(updatedDto.status),
            createdAt = updatedDto.createdAt,
            expiresAt = updatedDto.expiresAt,
            roles = updatedDto.roles
        )

        return ResponseEntity.ok(response)
    }

    // Subtask 3.6: DELETE /service-accounts/{serviceAccountId} for soft-deleting
    @DeleteMapping("/{serviceAccountId}")
    @PreAuthorize("hasAuthority('service_account:delete') and @tenantSecurity.isUserInTenant(#tenantId)")
    fun deleteServiceAccount(@PathVariable tenantId: UUID, @PathVariable serviceAccountId: UUID): ResponseEntity<Void> {
        val deleted = serviceAccountManagementService.deleteServiceAccount(
            tenantId = tenantId,
            serviceAccountId = serviceAccountId,
            initiatedBy = "admin" // TODO: Get from security context
        )

        return if (deleted) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }

    // Subtask 3.7: POST /service-accounts/{serviceAccountId}/rotate-secret for secret rotation
    @PostMapping("/{serviceAccountId}/rotate-secret")
    @PreAuthorize("hasAuthority('service_account:manage_credentials') and @tenantSecurity.isUserInTenant(#tenantId)")
    fun rotateServiceAccountSecret(@PathVariable tenantId: UUID, @PathVariable serviceAccountId: UUID): ResponseEntity<ServiceAccountSecretResponse> {
        val request = RotateSecretRequest(
            tenantId = tenantId,
            serviceAccountId = serviceAccountId,
            initiatedBy = "admin" // TODO: Get from security context
        )

        val result = serviceAccountManagementService.rotateSecret(request)
            ?: return ResponseEntity.notFound().build()

        val response = ServiceAccountSecretResponse(
            serviceAccountId = result.serviceAccountId,
            clientId = "", // Client ID doesn't change during rotation
            clientSecret = result.clientSecret
        )

        return ResponseEntity.ok(response)
    }

    // === Role Management Endpoints for Service Accounts ===

    @GetMapping("/{serviceAccountId}/roles")
    @PreAuthorize("hasAuthority('service_account:read') and @tenantSecurity.isUserInTenant(#tenantId)")
    fun getServiceAccountRoles(@PathVariable tenantId: UUID, @PathVariable serviceAccountId: UUID): ResponseEntity<Set<UUID>> {
        // TODO: Call serviceAccountManagementService.getServiceAccountRoles(tenantId, serviceAccountId)
        println("Received getServiceAccountRoles request for tenant $tenantId, account $serviceAccountId")
        return ResponseEntity.ok(setOf(UUID.randomUUID(), UUID.randomUUID()))
    }

    @PostMapping("/{serviceAccountId}/roles")
    @PreAuthorize("hasAuthority('service_account:assign_roles') and @tenantSecurity.isUserInTenant(#tenantId)")
    fun assignRolesToServiceAccount(
        @PathVariable tenantId: UUID,
        @PathVariable serviceAccountId: UUID,
        @RequestBody roleIds: Set<UUID>,
    ): ResponseEntity<Void> {
        // TODO: Call serviceAccountManagementService.assignRoles(tenantId, serviceAccountId, roleIds)
        println("Received assignRolesToServiceAccount request for tenant $tenantId, account $serviceAccountId with roles $roleIds")
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/{serviceAccountId}/roles")
    @PreAuthorize("hasAuthority('service_account:assign_roles') and @tenantSecurity.isUserInTenant(#tenantId)")
    fun removeRolesFromServiceAccount(
        @PathVariable tenantId: UUID,
        @PathVariable serviceAccountId: UUID,
        @RequestBody roleIds: Set<UUID>,
    ): ResponseEntity<Void> {
        // TODO: Call serviceAccountManagementService.removeRoles(tenantId, serviceAccountId, roleIds)
        println("Received removeRolesFromServiceAccount request for tenant $tenantId, account $serviceAccountId with roles $roleIds")
        return ResponseEntity.noContent().build()
    }

    @PutMapping("/{serviceAccountId}/roles")
    @PreAuthorize("hasAuthority('service_account:assign_roles') and @tenantSecurity.isUserInTenant(#tenantId)")
    fun setServiceAccountRoles(
        @PathVariable tenantId: UUID,
        @PathVariable serviceAccountId: UUID,
        @RequestBody roleIds: Set<UUID>,
    ): ResponseEntity<Void> {
        // TODO: Call serviceAccountManagementService.setRoles(tenantId, serviceAccountId, roleIds)
        println("Received setServiceAccountRoles request for tenant $tenantId, account $serviceAccountId with roles $roleIds")
        return ResponseEntity.noContent().build()
    }

    // === Private Helper Methods ===

    private fun mapStatusToResponse(status: ServiceAccountStatusDto): ServiceAccountStatusResponse =
        when (status) {
            ServiceAccountStatusDto.ACTIVE -> ServiceAccountStatusResponse.ACTIVE
            ServiceAccountStatusDto.INACTIVE -> ServiceAccountStatusResponse.INACTIVE
        }

    private fun mapStatusFromResponse(status: ServiceAccountStatusResponse): ServiceAccountStatusDto =
        when (status) {
            ServiceAccountStatusResponse.ACTIVE -> ServiceAccountStatusDto.ACTIVE
            ServiceAccountStatusResponse.INACTIVE -> ServiceAccountStatusDto.INACTIVE
        }
}
