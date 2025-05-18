package com.acci.eaf.multitenancy.service

import com.acci.eaf.multitenancy.domain.TenantStatus
import com.acci.eaf.multitenancy.dto.CreateTenantDto
import com.acci.eaf.multitenancy.dto.TenantDto
import com.acci.eaf.multitenancy.dto.UpdateTenantDto
import java.util.UUID

/**
 * Service for managing tenants.
 */
interface TenantService {

    /**
     * Creates a new tenant.
     *
     * @param createTenantDto Data for the new tenant
     * @return The created tenant
     * @throws TenantNameAlreadyExistsException if a tenant with the same name already exists
     * @throws InvalidTenantNameException if the tenant name is invalid
     */
    fun createTenant(createTenantDto: CreateTenantDto): TenantDto

    /**
     * Retrieves a tenant by its ID.
     *
     * @param tenantId ID of the tenant to retrieve
     * @return The tenant with the specified ID
     * @throws TenantNotFoundException if no tenant with the specified ID exists
     */
    fun getTenantById(tenantId: UUID): TenantDto

    /**
     * Retrieves a tenant by its name.
     *
     * @param name Name of the tenant to retrieve
     * @return The tenant with the specified name
     * @throws TenantNotFoundByNameException if no tenant with the specified name exists
     */
    fun getTenantByName(name: String): TenantDto

    /**
     * Lists all tenants.
     *
     * @return List of all tenants
     */
    fun getAllTenants(): List<TenantDto>

    /**
     * Lists all tenants with the specified status.
     *
     * @param status Status to filter by
     * @return List of tenants with the specified status
     */
    fun getTenantsByStatus(status: TenantStatus): List<TenantDto>

    /**
     * Updates an existing tenant.
     *
     * @param tenantId ID of the tenant to update
     * @param updateTenantDto Data to update the tenant with
     * @return The updated tenant
     * @throws TenantNotFoundException if no tenant with the specified ID exists
     * @throws TenantNameAlreadyExistsException if the new name is already in use by another tenant
     * @throws InvalidTenantStatusTransitionException if the status transition is not allowed
     * @throws InvalidTenantNameException if the new tenant name is invalid
     */
    fun updateTenant(tenantId: UUID, updateTenantDto: UpdateTenantDto): TenantDto

    /**
     * Soft-deletes a tenant by changing its status to ARCHIVED.
     *
     * @param tenantId ID of the tenant to delete
     * @return The deleted tenant
     * @throws TenantNotFoundException if no tenant with the specified ID exists
     */
    fun deleteTenant(tenantId: UUID): TenantDto

    /**
     * Checks if a tenant with the specified name exists.
     *
     * @param name Name to check
     * @return true if a tenant with the specified name exists, false otherwise
     */
    fun existsByName(name: String): Boolean
} 
