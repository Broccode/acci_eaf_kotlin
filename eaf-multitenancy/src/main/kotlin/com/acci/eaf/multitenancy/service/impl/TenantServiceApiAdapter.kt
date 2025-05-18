package com.acci.eaf.multitenancy.service.impl

import com.acci.eaf.core.interfaces.TenantInfo
import com.acci.eaf.core.interfaces.TenantServiceApi
import com.acci.eaf.core.interfaces.TenantStatus
import com.acci.eaf.multitenancy.service.TenantService
import java.util.UUID
import org.springframework.stereotype.Component

/**
 * Adapter, der das TenantServiceApi-Interface aus eaf-core implementiert und
 * an den TenantService aus eaf-multitenancy delegiert.
 *
 * Dies löst die zirkuläre Abhängigkeit zwischen eaf-core und eaf-multitenancy.
 */
@Component
class TenantServiceApiAdapter(private val tenantService: TenantService) : TenantServiceApi {

    /**
     * Implementiert getTenantById aus TenantServiceApi durch Delegation an TenantService.
     */
    override fun getTenantById(tenantId: UUID): TenantInfo {
        val tenant = tenantService.getTenantById(tenantId)

        // Konvertiere TenantDto zu TenantInfo
        return TenantInfo(
            tenantId = tenant.tenantId,
            status = mapStatus(tenant.status)
        )
    }

    /**
     * Implementiert existsById aus TenantServiceApi.
     */
    override fun existsById(tenantId: UUID): Boolean {
        return try {
            tenantService.getTenantById(tenantId)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Konvertiert den TenantStatus aus eaf-multitenancy in den TenantStatus aus eaf-core.
     */
    private fun mapStatus(status: com.acci.eaf.multitenancy.domain.TenantStatus): TenantStatus {
        return when (status) {
            com.acci.eaf.multitenancy.domain.TenantStatus.PENDING_VERIFICATION -> TenantStatus.PENDING_VERIFICATION
            com.acci.eaf.multitenancy.domain.TenantStatus.ACTIVE -> TenantStatus.ACTIVE
            com.acci.eaf.multitenancy.domain.TenantStatus.INACTIVE -> TenantStatus.INACTIVE
            com.acci.eaf.multitenancy.domain.TenantStatus.SUSPENDED -> TenantStatus.SUSPENDED
            com.acci.eaf.multitenancy.domain.TenantStatus.ARCHIVED -> TenantStatus.ARCHIVED
        }
    }
} 
