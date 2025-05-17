package com.acci.eaf.core.tenant

import java.util.UUID

/**
 * Holds the tenant context for the current thread.
 * 
 * This class provides methods to set, get, and clear the tenant ID for the current execution context.
 * It uses ThreadLocal to store the tenant ID, which ensures thread safety and isolation between requests.
 */
object TenantContextHolder {
    
    private val currentTenantId = ThreadLocal<UUID?>()
    
    /**
     * Sets the tenant ID for the current thread.
     * 
     * @param tenantId The UUID of the tenant to set as current
     */
    fun setTenantId(tenantId: UUID) {
        currentTenantId.set(tenantId)
    }
    
    /**
     * Gets the tenant ID for the current thread.
     * 
     * @return The UUID of the current tenant, or null if no tenant is set
     */
    fun getCurrentTenantId(): UUID? {
        return currentTenantId.get()
    }
    
    /**
     * Checks if a tenant ID is set for the current thread.
     * 
     * @return true if a tenant ID is set, false otherwise
     */
    fun hasTenantId(): Boolean {
        return currentTenantId.get() != null
    }
    
    /**
     * Clears the tenant ID for the current thread.
     * 
     * Should be called after request processing is complete to prevent memory leaks.
     */
    fun clear() {
        currentTenantId.remove()
    }
} 