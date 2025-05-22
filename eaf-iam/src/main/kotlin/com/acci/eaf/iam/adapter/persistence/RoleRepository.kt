package com.acci.eaf.iam.adapter.persistence

import com.acci.eaf.iam.domain.model.Role
import java.util.Optional
import java.util.UUID
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface RoleRepository : JpaRepository<Role, UUID> {

    /**
     * Findet eine Rolle anhand ihres Namens und der tenantId.
     */
    fun findByNameAndTenantId(name: String, tenantId: UUID?): Optional<Role>

    /**
     * Findet alle Rollen für einen bestimmten Tenant.
     */
    fun findByTenantId(tenantId: UUID?, pageable: Pageable): Page<Role>

    /**
     * Findet alle Rollen, die systemweit (tenantId = null) oder für einen bestimmten Tenant verfügbar sind.
     */
    @Query("SELECT r FROM Role r WHERE r.tenantId IS NULL OR r.tenantId = :tenantId")
    fun findAllAvailableForTenant(@Param("tenantId") tenantId: UUID, pageable: Pageable): Page<Role>

    /**
     * Findet alle Rollen eines bestimmten Benutzers.
     */
    @Query("SELECT r FROM Role r JOIN r.users u WHERE u.id = :userId")
    fun findByUserId(@Param("userId") userId: UUID): List<Role>

    /**
     * Prüft, ob eine Rolle mit diesem Namen für den gegebenen Tenant bereits existiert.
     */
    fun existsByNameAndTenantId(name: String, tenantId: UUID?): Boolean
}
