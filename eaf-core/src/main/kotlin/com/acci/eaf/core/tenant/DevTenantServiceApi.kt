package com.acci.eaf.core.tenant

import com.acci.eaf.core.interfaces.TenantInfo
import com.acci.eaf.core.interfaces.TenantServiceApi
import com.acci.eaf.core.interfaces.TenantStatus
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

/**
 * Eine einfache Entwicklungs-Implementierung des TenantServiceApi-Interfaces.
 *
 * Diese Klasse wird nur im Entwicklungsprofil verwendet und ermöglicht
 * das Testen von tenantbasierten Funktionen ohne eine echte Tenant-Datenbank.
 */
@Service
@Profile("dev")
class DevTenantServiceApi : TenantServiceApi {
    // In-Memory-Speicher für Test-Tenants
    private val tenants = ConcurrentHashMap<UUID, TenantInfo>()

    // Standard-Tenant für Entwicklung
    private val defaultTenantId = UUID.fromString("00000000-0000-0000-0000-000000000001")

    init {
        // Initialisiere mit einem Standard-Entwicklungs-Tenant
        tenants[defaultTenantId] = TenantInfo(
            tenantId = defaultTenantId,
            status = TenantStatus.ACTIVE
        )
    }

    override fun getTenantById(tenantId: UUID): TenantInfo = tenants[tenantId] ?: throw RuntimeException("Tenant with ID $tenantId not found")

    override fun existsById(tenantId: UUID): Boolean = tenants.containsKey(tenantId)

    /**
     * Fügt einen neuen Tenant hinzu oder aktualisiert einen vorhandenen.
     * Diese Methode ist nur für Testzwecke.
     */
    fun addOrUpdateTenant(tenantInfo: TenantInfo) {
        tenants[tenantInfo.tenantId] = tenantInfo
    }

    /**
     * Liefert die ID des Standard-Entwicklungs-Tenants.
     */
    fun getDefaultTenantId(): UUID = defaultTenantId
}
