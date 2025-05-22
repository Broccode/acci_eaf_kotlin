package com.acci.eaf.iam.adapter.persistence

import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ServiceAccountRoleJpaRepository : JpaRepository<ServiceAccountRoleEntity, UUID> {

    fun findByServiceAccountId(serviceAccountId: UUID): List<ServiceAccountRoleEntity>

    fun deleteByServiceAccountId(serviceAccountId: UUID)

    fun deleteByServiceAccountIdAndRoleId(serviceAccountId: UUID, roleId: UUID)
}
