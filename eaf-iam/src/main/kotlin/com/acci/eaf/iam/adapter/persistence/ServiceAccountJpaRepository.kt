package com.acci.eaf.iam.adapter.persistence

import java.util.UUID
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ServiceAccountJpaRepository : JpaRepository<ServiceAccountEntity, UUID> {

    fun findByTenantIdAndServiceAccountId(tenantId: UUID, serviceAccountId: UUID): ServiceAccountEntity?

    fun findByTenantIdAndClientId(tenantId: UUID, clientId: String): ServiceAccountEntity?

    fun findByClientId(clientId: String): ServiceAccountEntity?

    fun findAllByTenantId(tenantId: UUID, pageable: Pageable): Page<ServiceAccountEntity>

    fun existsByTenantIdAndClientId(tenantId: UUID, clientId: String): Boolean
}
