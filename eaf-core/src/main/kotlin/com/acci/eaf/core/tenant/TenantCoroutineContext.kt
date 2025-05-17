package com.acci.eaf.core.tenant

import kotlinx.coroutines.ThreadContextElement
import java.util.UUID
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * A coroutine context element that preserves and restores the tenant context when crossing coroutine boundaries.
 * 
 * This element ensures that the tenant ID is correctly propagated when using coroutines,
 * even across thread boundaries.
 * 
 * Usage:
 * ```
 * // Capture current tenant ID and propagate to all child coroutines
 * val tenantContext = TenantCoroutineContext.capture()
 * 
 * // Launch a coroutine with tenant context
 * launch(Dispatchers.IO + tenantContext) {
 *     // TenantContextHolder.getCurrentTenantId() will have the same value as in the parent
 *     val tenantId = TenantContextHolder.getCurrentTenantId()
 *     ...
 * }
 * ```
 * 
 * @property tenantId The tenant ID to be propagated to child coroutines
 */
class TenantCoroutineContext(private val tenantId: UUID?) : AbstractCoroutineContextElement(Key), 
    ThreadContextElement<UUID?> {
    
    companion object Key : CoroutineContext.Key<TenantCoroutineContext> {
        /**
         * Captures the current tenant ID from TenantContextHolder and creates a new TenantCoroutineContext.
         * 
         * @return A new TenantCoroutineContext with the current tenant ID
         */
        fun capture(): TenantCoroutineContext = TenantCoroutineContext(TenantContextHolder.getCurrentTenantId())
    }
    
    /**
     * Updates the thread-local tenant ID before the coroutine is executed on a new thread.
     * 
     * @param context The coroutine context
     * @return The original tenant ID that was in TenantContextHolder before this update
     */
    override fun updateThreadContext(context: CoroutineContext): UUID? {
        val oldTenantId = TenantContextHolder.getCurrentTenantId()
        if (tenantId != null) {
            TenantContextHolder.setTenantId(tenantId)
        } else {
            TenantContextHolder.clear()
        }
        return oldTenantId
    }
    
    /**
     * Restores the original tenant ID after the coroutine has been executed.
     * 
     * @param context The coroutine context
     * @param oldState The original tenant ID to restore
     */
    override fun restoreThreadContext(context: CoroutineContext, oldState: UUID?) {
        if (oldState != null) {
            TenantContextHolder.setTenantId(oldState)
        } else {
            TenantContextHolder.clear()
        }
    }
} 