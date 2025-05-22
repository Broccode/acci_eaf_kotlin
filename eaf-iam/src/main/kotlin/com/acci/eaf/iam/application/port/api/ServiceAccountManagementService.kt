package com.acci.eaf.iam.application.port.api

import java.time.OffsetDateTime
import java.util.UUID
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

// === Application Layer DTOs (Ports) ===
// These DTOs are separate from Domain Commands and UI DTOs

data class CreateServiceAccountRequest(
    val tenantId: UUID,
    val description: String?,
    // Null for default, specific value for custom
    val expiresAt: OffsetDateTime?,
    val requestedRoles: Set<UUID>?,
    val initiatedBy: String,
)

data class UpdateServiceAccountRequest(
    val tenantId: UUID,
    val serviceAccountId: UUID,
    val description: String?,
    val status: ServiceAccountStatusDto?,
    val expiresAt: OffsetDateTime?,
    val initiatedBy: String,
)

// === Application Layer DTOs ===

enum class ServiceAccountStatusDto {
    ACTIVE,
    INACTIVE,
}

data class ServiceAccountDto(
    val serviceAccountId: UUID,
    val tenantId: UUID,
    val clientId: String,
    val description: String?,
    val status: ServiceAccountStatusDto,
    val createdAt: OffsetDateTime,
    val expiresAt: OffsetDateTime?,
    val roles: Set<UUID>,
)

data class ServiceAccountCreationResult(
    val serviceAccount: ServiceAccountDto,
    // Plaintext secret, displayed only once
    val clientSecret: String,
)

data class ServiceAccountSecretResult(
    val serviceAccountId: UUID,
    // New plaintext secret, displayed only once
    val clientSecret: String,
)

// === Application Port Interface ===

interface ServiceAccountManagementService {

    fun createServiceAccount(request: CreateServiceAccountRequest): ServiceAccountCreationResult

    fun listServiceAccounts(tenantId: UUID, pageable: Pageable): Page<ServiceAccountDto>

    fun getServiceAccount(tenantId: UUID, serviceAccountId: UUID): ServiceAccountDto?

    fun updateServiceAccount(request: UpdateServiceAccountRequest): ServiceAccountDto?

    fun deleteServiceAccount(
        tenantId: UUID,
        serviceAccountId: UUID,
        initiatedBy: String,
    ): Boolean

    fun rotateSecret(request: RotateSecretRequest): ServiceAccountSecretResult?

    fun assignRoles(request: AssignRolesRequest): Unit

    fun removeRoles(request: RemoveRolesRequest): Unit

    fun deactivateServiceAccount(
        tenantId: UUID,
        serviceAccountId: UUID,
        initiatedBy: String,
    ): Unit

    fun activateServiceAccount(
        tenantId: UUID,
        serviceAccountId: UUID,
        initiatedBy: String,
    ): Unit

    fun getServiceAccountByClientId(tenantId: UUID, clientId: String): ServiceAccountDto?
}

// === Additional Request DTOs ===

data class RotateSecretRequest(val tenantId: UUID, val serviceAccountId: UUID, val initiatedBy: String)

data class AssignRolesRequest(val tenantId: UUID, val serviceAccountId: UUID, val roleIds: Set<UUID>, val initiatedBy: String)

data class RemoveRolesRequest(val tenantId: UUID, val serviceAccountId: UUID, val roleIds: Set<UUID>, val initiatedBy: String)
