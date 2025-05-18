package com.acci.eaf.core.tenant

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Configuration for registering the TenantContextInterceptor in Spring Web MVC.
 *
 * This configuration ensures that the tenant context interceptor is applied
 * to all relevant endpoints, allowing tenant context propagation.
 */
@Configuration
class TenantWebMvcConfig(private val tenantContextInterceptor: TenantContextInterceptor) : WebMvcConfigurer {

    /**
     * Adds the tenant context interceptor to the interceptor registry.
     *
     * @param registry The interceptor registry
     */
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(tenantContextInterceptor)
            .addPathPatterns("/**") // Apply to all paths
            .excludePathPatterns("/actuator/**", "/health", "/error") // Exclude management endpoints
    }
} 
