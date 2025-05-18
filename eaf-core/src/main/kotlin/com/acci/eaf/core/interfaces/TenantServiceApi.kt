package com.acci.eaf.core.interfaces

import java.util.UUID

/**
 * Interface für den Zugriff auf Tenant-Informationen.
 *
 * Dieses Interface definiert die grundlegenden Methoden, die für die Tenant-Validierung
 * in der Tenant-Kontext-Propagierung benötigt werden.
 */
interface TenantServiceApi {

    /**
     * Holt einen Tenant anhand seiner ID.
     *
     * @param tenantId ID des Tenants
     * @return Der Tenant mit der angegebenen ID
     * @throws Exception wenn kein Tenant mit der angegebenen ID existiert
     */
    fun getTenantById(tenantId: UUID): TenantInfo

    /**
     * Prüft, ob ein Tenant existiert.
     *
     * @param tenantId ID des Tenants
     * @return true, wenn der Tenant existiert, sonst false
     */
    fun existsById(tenantId: UUID): Boolean
}

/**
 * Basisinformationen zu einem Tenant.
 *
 * @property tenantId ID des Tenants
 * @property status Status des Tenants
 */
data class TenantInfo(
    val tenantId: UUID,
    val status: TenantStatus,
)

/**
 * Mögliche Status eines Tenants.
 */
enum class TenantStatus {
    PENDING_VERIFICATION,
    ACTIVE,
    INACTIVE,
    SUSPENDED,
    ARCHIVED,
} 
