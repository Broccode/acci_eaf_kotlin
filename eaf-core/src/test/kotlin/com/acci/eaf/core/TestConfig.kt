package com.acci.eaf.core

import com.acci.eaf.core.interfaces.TenantInfo
import com.acci.eaf.core.interfaces.TenantServiceApi
import com.acci.eaf.core.interfaces.TenantStatus
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

/**
 * Testkonfiguration für EAF Core Tests.
 */
@Configuration
@Profile("test")
class TestConfig {

    /**
     * Stellt eine Mock-Implementierung von TenantServiceApi für Tests bereit.
     */
    @Bean
    @Primary
    fun testTenantServiceApi(): TenantServiceApi =
        object : TenantServiceApi {
            // In-Memory-Speicher für Test-Tenants
            private val tenants = ConcurrentHashMap<UUID, TenantInfo>()

            // Aktiver Test-Tenant
            private val testTenantId = UUID.fromString("fe2b6d3a-bb5a-43d6-b505-e764cd1bf30f")

            init {
                // Initialisiere mit einem aktiven Test-Tenant
                tenants[testTenantId] = TenantInfo(
                    tenantId = testTenantId,
                    status = TenantStatus.ACTIVE
                )
            }

            override fun getTenantById(tenantId: UUID): TenantInfo = tenants[tenantId] ?: throw RuntimeException("Tenant not found")

            override fun existsById(tenantId: UUID): Boolean = tenants.containsKey(tenantId)
        }
}
