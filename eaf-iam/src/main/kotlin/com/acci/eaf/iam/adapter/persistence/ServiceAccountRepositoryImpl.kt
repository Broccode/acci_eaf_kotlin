package com.acci.eaf.iam.adapter.persistence

import com.acci.eaf.iam.domain.model.ServiceAccount
import com.acci.eaf.iam.domain.repository.ServiceAccountRepository
import java.util.UUID
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class ServiceAccountRepositoryImpl(
    private val jpaRepository: ServiceAccountJpaRepository,
    private val roleJpaRepository: ServiceAccountRoleJpaRepository,
) : ServiceAccountRepository {

    override fun save(serviceAccount: ServiceAccount): ServiceAccount {
        val entity = toEntity(serviceAccount)
        val savedEntity = jpaRepository.save(entity)

        // Handle roles separately
        saveRoles(savedEntity.serviceAccountId, serviceAccount.roles)

        return toDomain(savedEntity)
    }

    override fun findById(serviceAccountId: UUID): ServiceAccount? =
        jpaRepository.findById(serviceAccountId)
            .map { toDomain(it) }
            .orElse(null)

    override fun findByTenantIdAndId(tenantId: UUID, serviceAccountId: UUID): ServiceAccount? =
        jpaRepository.findByTenantIdAndServiceAccountId(tenantId, serviceAccountId)
            ?.let { toDomain(it) }

    override fun findByTenantIdAndClientId(tenantId: UUID, clientId: String): ServiceAccount? =
        jpaRepository.findByTenantIdAndClientId(tenantId, clientId)
            ?.let { toDomain(it) }

    override fun findByClientId(clientId: String): ServiceAccount? =
        jpaRepository.findByClientId(clientId)
            ?.let { toDomain(it) }

    override fun findAllByTenantId(tenantId: UUID, pageable: Pageable): Page<ServiceAccount> =
        jpaRepository.findAllByTenantId(tenantId, pageable)
            .map { toDomain(it) }

    override fun existsByTenantIdAndClientId(tenantId: UUID, clientId: String): Boolean =
        jpaRepository.existsByTenantIdAndClientId(tenantId, clientId)

    // === Mapping Functions ===

    private fun toEntity(domain: ServiceAccount): ServiceAccountEntity =
        ServiceAccountEntity(
            serviceAccountId = domain.serviceAccountId,
            tenantId = domain.tenantId,
            clientId = domain.clientId,
            clientSecretHash = domain.clientSecretHash,
            salt = domain.salt,
            description = domain.description,
            status = domain.status,
            createdAt = domain.createdAt,
            expiresAt = domain.expiresAt
        )

    private fun toDomain(entity: ServiceAccountEntity): ServiceAccount {
        // Load roles for this service account
        val roles = loadRoles(entity.serviceAccountId)

        return ServiceAccount(
            serviceAccountId = entity.serviceAccountId,
            tenantId = entity.tenantId,
            clientId = entity.clientId,
            clientSecretHash = entity.clientSecretHash,
            salt = entity.salt,
            description = entity.description,
            status = entity.status,
            roles = roles,
            createdAt = entity.createdAt,
            expiresAt = entity.expiresAt
        )
    }

    private fun saveRoles(serviceAccountId: UUID, roles: Set<UUID>) {
        // Delete existing roles
        roleJpaRepository.deleteByServiceAccountId(serviceAccountId)

        // Save new roles
        roles.forEach { roleId ->
            val roleEntity = ServiceAccountRoleEntity(
                serviceAccountId = serviceAccountId,
                roleId = roleId
            )
            roleJpaRepository.save(roleEntity)
        }
    }

    private fun loadRoles(serviceAccountId: UUID): Set<UUID> =
        roleJpaRepository.findByServiceAccountId(serviceAccountId)
            .map { it.roleId }
            .toSet()
}
