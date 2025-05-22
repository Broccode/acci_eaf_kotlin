package com.acci.eaf.iam.domain.repository

import com.acci.eaf.iam.domain.model.ServiceAccount
import java.util.UUID
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ServiceAccountRepository {
    fun save(serviceAccount: ServiceAccount): ServiceAccount
    fun findById(serviceAccountId: UUID): ServiceAccount?
    fun findByTenantIdAndId(tenantId: UUID, serviceAccountId: UUID): ServiceAccount?
    fun findByTenantIdAndClientId(tenantId: UUID, clientId: String): ServiceAccount?
    fun findByClientId(clientId: String): ServiceAccount? // For OAuth2 authentication
    fun findAllByTenantId(tenantId: UUID, pageable: Pageable): Page<ServiceAccount>
    fun existsByTenantIdAndClientId(tenantId: UUID, clientId: String): Boolean
    // delete an account by marking it inactive, actual deletion might be a separate admin function or not allowed
}
