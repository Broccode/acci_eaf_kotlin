package com.acci.eaf.core.tenant

import org.springframework.core.task.TaskDecorator

/**
 * Decorator for asynchronous tasks that ensures tenant context propagation.
 *
 * When Spring executes asynchronous methods (e.g., using @Async), this decorator
 * captures the current tenant context from the calling thread and applies it to
 * the thread executing the asynchronous task.
 */
class TenantTaskDecorator : TaskDecorator {

    /**
     * Decorates the given Runnable with tenant context propagation.
     *
     * @param runnable The original task to be executed
     * @return A wrapped Runnable that propagates the tenant context
     */
    override fun decorate(runnable: Runnable): Runnable {
        // Capture the current tenant ID before executing the task
        val tenantId = TenantContextHolder.getCurrentTenantId()

        // Return a wrapped Runnable that sets up and cleans up the tenant context
        return Runnable {
            try {
                // Set the captured tenant ID in the new thread
                if (tenantId != null) {
                    TenantContextHolder.setTenantId(tenantId)
                }

                // Execute the original task
                runnable.run()
            } finally {
                // Clean up the tenant context
                TenantContextHolder.clear()
            }
        }
    }
}
