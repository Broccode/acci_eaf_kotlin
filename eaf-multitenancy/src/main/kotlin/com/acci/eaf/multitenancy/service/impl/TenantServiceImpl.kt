package com.acci.eaf.multitenancy.service.impl

import com.acci.eaf.multitenancy.domain.Tenant
import com.acci.eaf.multitenancy.domain.TenantStatus
import com.acci.eaf.multitenancy.dto.CreateTenantDto
import com.acci.eaf.multitenancy.dto.TenantDto
import com.acci.eaf.multitenancy.dto.UpdateTenantDto
import com.acci.eaf.multitenancy.exception.InvalidTenantNameException
import com.acci.eaf.multitenancy.exception.InvalidTenantStatusTransitionException
import com.acci.eaf.multitenancy.exception.TenantNameAlreadyExistsException
import com.acci.eaf.multitenancy.exception.TenantNotFoundByNameException
import com.acci.eaf.multitenancy.exception.TenantNotFoundException
import com.acci.eaf.multitenancy.repository.TenantRepository
import com.acci.eaf.multitenancy.service.TenantService
import jakarta.validation.Validator
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Implementation of the [TenantService] interface.
 */
@Service
class TenantServiceImpl(
    private val tenantRepository: TenantRepository,
    private val validator: Validator,
) : TenantService {

    @Transactional
    override fun createTenant(createTenantDto: CreateTenantDto): TenantDto {
        // Validate tenant name
        validateTenantName(createTenantDto.name)

        // Check if tenant with same name already exists
        if (tenantRepository.existsByName(createTenantDto.name)) {
            throw TenantNameAlreadyExistsException(createTenantDto.name)
        }

        // Create and save new tenant
        val tenant = Tenant(
            name = createTenantDto.name,
            status = createTenantDto.status
        )

        val savedTenant = tenantRepository.save(tenant)
        return savedTenant.toDto()
    }

    @Transactional(readOnly = true)
    override fun getTenantById(tenantId: UUID): TenantDto {
        return findTenantEntityById(tenantId).toDto()
    }

    @Transactional(readOnly = true)
    override fun getTenantByName(name: String): TenantDto {
        return tenantRepository.findByName(name)
            .orElseThrow { TenantNotFoundByNameException(name) }
            .toDto()
    }

    @Transactional(readOnly = true)
    override fun getAllTenants(): List<TenantDto> {
        return tenantRepository.findAll().map { it.toDto() }
    }

    @Transactional(readOnly = true)
    override fun getTenantsByStatus(status: TenantStatus): List<TenantDto> {
        return tenantRepository.findByStatus(status).map { it.toDto() }
    }

    @Transactional
    override fun updateTenant(tenantId: UUID, updateTenantDto: UpdateTenantDto): TenantDto {
        val tenant = findTenantEntityById(tenantId)

        // Update name if provided
        updateTenantDto.name?.let { newName ->
            // Only validate and update if the name has actually changed
            if (newName != tenant.name) {
                validateTenantName(newName)

                // Check if tenant with same name already exists
                if (tenantRepository.existsByName(newName)) {
                    throw TenantNameAlreadyExistsException(newName)
                }

                tenant.name = newName
            }
        }

        // Update status if provided
        updateTenantDto.status?.let { newStatus ->
            if (newStatus != tenant.status) {
                validateStatusTransition(tenant.status, newStatus)
                tenant.status = newStatus
            }
        }

        // Save and return updated tenant
        val updatedTenant = tenantRepository.save(tenant)
        return updatedTenant.toDto()
    }

    @Transactional
    override fun deleteTenant(tenantId: UUID): TenantDto {
        val tenant = findTenantEntityById(tenantId)

        // Soft delete by setting status to ARCHIVED
        tenant.status = TenantStatus.ARCHIVED

        val deletedTenant = tenantRepository.save(tenant)
        return deletedTenant.toDto()
    }

    @Transactional(readOnly = true)
    override fun existsByName(name: String): Boolean {
        return tenantRepository.existsByName(name)
    }

    /**
     * Finds a tenant entity by ID or throws an exception if not found.
     */
    private fun findTenantEntityById(tenantId: UUID): Tenant {
        return tenantRepository.findById(tenantId)
            .orElseThrow { TenantNotFoundException(tenantId) }
    }

    /**
     * Validates that a tenant name meets all requirements.
     */
    private fun validateTenantName(name: String) {
        // Check if name is empty
        if (name.isBlank()) {
            throw InvalidTenantNameException(name, "Tenant name cannot be blank")
        }

        // Check length
        if (name.length < 3 || name.length > 100) {
            // Tenant name must be between 3 and 100 characters
            throw InvalidTenantNameException(
                name,
                "Tenant name must be between 3 and 100 characters"
            )
        }

        // Check format (alphanumeric with hyphens)
        if (!name.matches(Regex("^[a-zA-Z0-9\\-]+$"))) {
            // Tenant name can only contain alphanumeric characters and hyphens
            throw InvalidTenantNameException(
                name,
                "Tenant name can only contain alphanumeric characters and hyphens"
            )
        }
    }

    /**
     * Validates that a status transition is allowed.
     */
    private fun validateStatusTransition(currentStatus: TenantStatus, newStatus: TenantStatus) {
        // Define allowed transitions
        val allowedTransitions = mapOf(
            TenantStatus.PENDING_VERIFICATION to setOf(
                TenantStatus.ACTIVE,
                TenantStatus.INACTIVE,
                TenantStatus.SUSPENDED,
                TenantStatus.ARCHIVED
            ),
            TenantStatus.ACTIVE to setOf(
                TenantStatus.INACTIVE,
                TenantStatus.SUSPENDED,
                TenantStatus.ARCHIVED
            ),
            TenantStatus.INACTIVE to setOf(
                TenantStatus.ACTIVE,
                TenantStatus.SUSPENDED,
                TenantStatus.ARCHIVED
            ),
            TenantStatus.SUSPENDED to setOf(
                TenantStatus.ACTIVE,
                TenantStatus.INACTIVE,
                TenantStatus.ARCHIVED
            ),
            // Cannot transition out of ARCHIVED
            TenantStatus.ARCHIVED to setOf()
        )

        // Check if transition is allowed
        if (newStatus !in (allowedTransitions[currentStatus] ?: setOf())) {
            // Cannot transition from current status to new status
            throw InvalidTenantStatusTransitionException(
                currentStatus.name,
                newStatus.name
            )
        }
    }
}

/**
 * Extension function to convert a Tenant entity to a TenantDto.
 */
private fun Tenant.toDto(): TenantDto =
    TenantDto(
        tenantId = this.tenantId,
        name = this.name,
        status = this.status,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    ) 
