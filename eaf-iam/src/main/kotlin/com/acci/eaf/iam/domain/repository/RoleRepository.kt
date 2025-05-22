package com.acci.eaf.iam.domain.repository

import com.acci.eaf.iam.domain.model.Role
import java.util.UUID
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface RoleRepository : JpaRepository<Role, UUID> {
    fun findByNameAndTenantId(name: String, tenantId: UUID?): Role?
    fun findByTenantId(tenantId: UUID, pageable: Pageable): Page<Role>
    fun findByTenantIdIsNull(pageable: Pageable): Page<Role> // For global roles
}
