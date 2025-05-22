package com.acci.eaf.iam.application.service

import com.acci.eaf.iam.application.port.api.AssignRolesRequest
import com.acci.eaf.iam.application.port.api.CreateServiceAccountRequest
import com.acci.eaf.iam.application.port.api.RemoveRolesRequest
import com.acci.eaf.iam.application.port.api.RotateSecretRequest
import com.acci.eaf.iam.application.port.api.ServiceAccountCreationResult
import com.acci.eaf.iam.application.port.api.ServiceAccountDto
import com.acci.eaf.iam.application.port.api.ServiceAccountManagementService
import com.acci.eaf.iam.application.port.api.ServiceAccountSecretResult
import com.acci.eaf.iam.application.port.api.ServiceAccountStatusDto
import com.acci.eaf.iam.application.port.api.UpdateServiceAccountRequest
import com.acci.eaf.iam.domain.command.ActivateServiceAccountCommand
import com.acci.eaf.iam.domain.command.AssignRolesToServiceAccountCommand
import com.acci.eaf.iam.domain.command.CreateServiceAccountCommand
import com.acci.eaf.iam.domain.command.DeactivateServiceAccountCommand
import com.acci.eaf.iam.domain.command.RemoveRolesFromServiceAccountCommand
import com.acci.eaf.iam.domain.command.RotateServiceAccountSecretCommand
import com.acci.eaf.iam.domain.command.UpdateServiceAccountDetailsCommand
import com.acci.eaf.iam.domain.model.ServiceAccount
import com.acci.eaf.iam.domain.model.ServiceAccountStatus
import com.acci.eaf.iam.domain.repository.ServiceAccountRepository
import java.util.UUID
import org.axonframework.commandhandling.gateway.CommandGateway
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class DefaultServiceAccountManagementService(
    private val commandGateway: CommandGateway,
    private val serviceAccountRepository: ServiceAccountRepository,
) : ServiceAccountManagementService {

    private val logger = LoggerFactory.getLogger(DefaultServiceAccountManagementService::class.java)

    override fun createServiceAccount(request: CreateServiceAccountRequest): ServiceAccountCreationResult {
        logger.info("Creating service account for tenant: {}", request.tenantId)

        val serviceAccountId = UUID.randomUUID()

        // Map Application Request to Domain Command
        val domainCommand = com.acci.eaf.iam.domain.command.CreateServiceAccountCommand(
            serviceAccountId = serviceAccountId,
            tenantId = request.tenantId,
            description = request.description,
            requestedExpiresAt = request.expiresAt,
            requestedRoles = request.requestedRoles,
            initiatedBy = request.initiatedBy
        )

        // Send command to aggregate
        commandGateway.sendAndWait<Void>(domainCommand)

        // Retrieve the created service account from read model
        val createdServiceAccount = serviceAccountRepository.findById(serviceAccountId)
            ?: throw RuntimeException("Service account creation failed")

        logger.info("Successfully created service account: {} for tenant: {}", serviceAccountId, request.tenantId)

        return ServiceAccountCreationResult(
            serviceAccount = mapDomainToDto(createdServiceAccount),
            clientSecret = "CLIENT_SECRET_PLACEHOLDER" // TODO: Handle secret properly via events
        )
    }

    override fun listServiceAccounts(tenantId: UUID, pageable: Pageable): Page<ServiceAccountDto> =
        serviceAccountRepository.findAllByTenantId(tenantId, pageable)
            .map { mapDomainToDto(it) }

    override fun getServiceAccount(tenantId: UUID, serviceAccountId: UUID): ServiceAccountDto? =
        serviceAccountRepository.findByTenantIdAndId(tenantId, serviceAccountId)
            ?.let { mapDomainToDto(it) }

    override fun updateServiceAccount(request: UpdateServiceAccountRequest): ServiceAccountDto? {
        logger.info("Updating service account: {}", request.serviceAccountId)

        // Map Application Request to Domain Command
        val domainCommand = UpdateServiceAccountDetailsCommand(
            serviceAccountId = request.serviceAccountId,
            tenantId = request.tenantId,
            description = request.description,
            status = request.status?.let { mapDtoToDomainStatus(it) },
            requestedExpiresAt = request.expiresAt,
            initiatedBy = request.initiatedBy
        )

        commandGateway.sendAndWait<Void>(domainCommand)

        val updatedServiceAccount = serviceAccountRepository.findByTenantIdAndId(request.tenantId, request.serviceAccountId)
            ?: return null

        logger.info("Successfully updated service account: {}", request.serviceAccountId)
        return mapDomainToDto(updatedServiceAccount)
    }

    override fun deleteServiceAccount(
        tenantId: UUID,
        serviceAccountId: UUID,
        initiatedBy: String,
    ): Boolean {
        logger.info("Deleting (deactivating) service account: {}", serviceAccountId)

        val domainCommand = DeactivateServiceAccountCommand(
            serviceAccountId = serviceAccountId,
            tenantId = tenantId,
            initiatedBy = initiatedBy
        )

        try {
            commandGateway.sendAndWait<Void>(domainCommand)
            logger.info("Successfully deactivated service account: {}", serviceAccountId)
            return true
        } catch (e: Exception) {
            logger.error("Failed to deactivate service account: {}", serviceAccountId, e)
            return false
        }
    }

    override fun rotateSecret(request: RotateSecretRequest): ServiceAccountSecretResult? {
        logger.info("Rotating secret for service account: {}", request.serviceAccountId)

        val domainCommand = RotateServiceAccountSecretCommand(
            serviceAccountId = request.serviceAccountId,
            tenantId = request.tenantId,
            initiatedBy = request.initiatedBy
        )

        commandGateway.sendAndWait<Void>(domainCommand)

        val serviceAccount = serviceAccountRepository.findByTenantIdAndId(request.tenantId, request.serviceAccountId)
            ?: return null

        logger.info("Successfully rotated secret for service account: {}", request.serviceAccountId)

        return ServiceAccountSecretResult(
            serviceAccountId = serviceAccount.serviceAccountId,
            clientSecret = "NEW_CLIENT_SECRET_PLACEHOLDER" // TODO: Handle secret properly via events
        )
    }

    override fun assignRoles(request: AssignRolesRequest) {
        logger.info("Assigning roles to service account: {}", request.serviceAccountId)

        val domainCommand = AssignRolesToServiceAccountCommand(
            serviceAccountId = request.serviceAccountId,
            tenantId = request.tenantId,
            rolesToAssign = request.roleIds,
            initiatedBy = request.initiatedBy
        )

        commandGateway.sendAndWait<Void>(domainCommand)
        logger.info("Successfully assigned roles to service account: {}", request.serviceAccountId)
    }

    override fun removeRoles(request: RemoveRolesRequest) {
        logger.info("Removing roles from service account: {}", request.serviceAccountId)

        val domainCommand = RemoveRolesFromServiceAccountCommand(
            serviceAccountId = request.serviceAccountId,
            tenantId = request.tenantId,
            rolesToRemove = request.roleIds,
            initiatedBy = request.initiatedBy
        )

        commandGateway.sendAndWait<Void>(domainCommand)
        logger.info("Successfully removed roles from service account: {}", request.serviceAccountId)
    }

    override fun deactivateServiceAccount(
        tenantId: UUID,
        serviceAccountId: UUID,
        initiatedBy: String,
    ) {
        logger.info("Deactivating service account: {}", serviceAccountId)

        val domainCommand = DeactivateServiceAccountCommand(
            serviceAccountId = serviceAccountId,
            tenantId = tenantId,
            initiatedBy = initiatedBy
        )

        commandGateway.sendAndWait<Void>(domainCommand)
        logger.info("Successfully deactivated service account: {}", serviceAccountId)
    }

    override fun activateServiceAccount(
        tenantId: UUID,
        serviceAccountId: UUID,
        initiatedBy: String,
    ) {
        logger.info("Activating service account: {}", serviceAccountId)

        val domainCommand = ActivateServiceAccountCommand(
            serviceAccountId = serviceAccountId,
            tenantId = tenantId,
            initiatedBy = initiatedBy
        )

        commandGateway.sendAndWait<Void>(domainCommand)
        logger.info("Successfully activated service account: {}", serviceAccountId)
    }

    override fun getServiceAccountByClientId(tenantId: UUID, clientId: String): ServiceAccountDto? =
        serviceAccountRepository.findByTenantIdAndClientId(tenantId, clientId)
            ?.let { mapDomainToDto(it) }

    // === Private Mapping Methods ===

    private fun mapDomainToDto(domain: ServiceAccount): ServiceAccountDto =
        ServiceAccountDto(
            serviceAccountId = domain.serviceAccountId,
            tenantId = domain.tenantId,
            clientId = domain.clientId,
            description = domain.description,
            status = mapDomainStatusToDto(domain.status),
            createdAt = domain.createdAt,
            expiresAt = domain.expiresAt,
            roles = domain.roles ?: emptySet()
        )

    private fun mapDomainStatusToDto(status: ServiceAccountStatus): ServiceAccountStatusDto =
        when (status) {
            ServiceAccountStatus.ACTIVE -> ServiceAccountStatusDto.ACTIVE
            ServiceAccountStatus.INACTIVE -> ServiceAccountStatusDto.INACTIVE
        }

    private fun mapDtoToDomainStatus(status: ServiceAccountStatusDto): ServiceAccountStatus =
        when (status) {
            ServiceAccountStatusDto.ACTIVE -> ServiceAccountStatus.ACTIVE
            ServiceAccountStatusDto.INACTIVE -> ServiceAccountStatus.INACTIVE
        }
}
