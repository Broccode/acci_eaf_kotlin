package com.acci.eaf.core.tenant

import java.util.concurrent.Executor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

/**
 * Configuration for asynchronous execution with tenant context propagation.
 *
 * This configuration ensures that asynchronous method executions (e.g., using @Async)
 * properly propagate the tenant context from the caller thread to the execution thread.
 */
@Configuration
@EnableAsync
class TenantAsyncConfig {

    /**
     * Creates a task executor for asynchronous methods that preserves tenant context.
     *
     * @return An Executor configured with the TenantTaskDecorator
     */
    @Bean(name = ["tenantAwareTaskExecutor"])
    fun tenantAwareTaskExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor().apply {
            corePoolSize = 5
            maxPoolSize = 10
            queueCapacity = 25
            setThreadNamePrefix("tenant-exec-")
            setTaskDecorator(TenantTaskDecorator())
        }
        executor.initialize()
        return executor
    }
} 
