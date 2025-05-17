package com.acci.eaf.multitenancy.repository

import com.acci.eaf.multitenancy.domain.Tenant
import com.acci.eaf.multitenancy.domain.TenantStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

/**
 * Repository for managing [Tenant] entities.
 */
@Repository
interface TenantRepository : JpaRepository<Tenant, UUID> {

    /**
     * Finds a tenant by its name.
     *
     * @param name The name of the tenant to find
     * @return An [Optional] containing the tenant if found, or empty if not found
     */
    fun findByName(name: String): Optional<Tenant>
    
    /**
     * Finds all tenants with the given status.
     *
     * @param status The status to filter by
     * @return List of tenants with the specified status
     */
    fun findByStatus(status: TenantStatus): List<Tenant>
    
    /**
     * Checks if a tenant with the given name exists.
     *
     * @param name The name to check
     * @return true if a tenant with the given name exists, false otherwise
     */
    fun existsByName(name: String): Boolean
} 