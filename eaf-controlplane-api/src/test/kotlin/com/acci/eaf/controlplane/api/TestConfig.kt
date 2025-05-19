package com.acci.eaf.controlplane.api

import com.acci.eaf.controlplane.api.audit.AuditLogger
import com.acci.eaf.controlplane.api.mapper.TenantMapperInterface
import com.acci.eaf.controlplane.api.service.TenantPageService
import com.acci.eaf.core.interfaces.TenantServiceApi
import com.acci.eaf.core.tenant.TenantContextHolder
import com.acci.eaf.core.tenant.TenantContextInterceptor
import com.acci.eaf.multitenancy.repository.TenantRepository
import com.acci.eaf.multitenancy.service.TenantService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.util.UUID
import org.mockito.Mockito
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Test configuration class for the controlplane API.
 * Provides mocked beans for testing.
 */
@TestConfiguration
@EnableWebMvc
@EnableWebSecurity
@EnableMethodSecurity
class TestConfig : WebMvcConfigurer {

    /**
     * Provides a mocked JWT decoder for testing security.
     */
    @Bean
    @Primary
    fun jwtDecoder(): JwtDecoder = Mockito.mock(NimbusJwtDecoder::class.java)

    /**
     * Basic Security Filter Chain for testing
     */
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf { it.disable() }
            .authorizeHttpRequests { authorize -> authorize.anyRequest().permitAll() }
        return http.build()
    }

    /**
     * Provides an ObjectMapper for JSON serialization/deserialization
     */
    @Bean
    fun objectMapper(): ObjectMapper =
        Jackson2ObjectMapperBuilder.json()
            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .modules(JavaTimeModule())
            .build()

    /**
     * Provides a mocked TenantRepository for tests that don't need a real database.
     */
    @Bean
    @Primary
    fun tenantRepository(): TenantRepository = Mockito.mock(TenantRepository::class.java)

    /**
     * Provides a mocked AuditLogger for tests.
     */
    @Bean
    @Primary
    fun auditLogger(): AuditLogger = Mockito.mock(AuditLogger::class.java)

    /**
     * Provides a mocked TenantService for tests.
     */
    @Bean
    @Primary
    fun tenantService(): TenantService = Mockito.mock(TenantService::class.java)

    /**
     * Provides a mocked TenantPageService for tests.
     */
    @Bean
    @Primary
    fun tenantPageService(): TenantPageService = Mockito.mock(TenantPageService::class.java)

    /**
     * Provides a mocked TenantMapper for tests.
     */
    @Bean
    @Primary
    fun tenantMapper(): TenantMapperInterface = Mockito.mock(TenantMapperInterface::class.java)

    /**
     * Provides a mocked TenantServiceApi for tests.
     */
    @Bean
    @Primary
    fun tenantServiceApi(): TenantServiceApi = Mockito.mock(TenantServiceApi::class.java)

    /**
     * Überschreibt den TenantContextInterceptor für Tests
     */
    @Bean
    @Primary
    fun tenantContextInterceptor(tenantServiceApi: TenantServiceApi): TenantContextInterceptor = NoOpTenantContextInterceptor(tenantServiceApi)

    /**
     * Eine No-Operation-Implementierung des TenantContextInterceptor für Tests,
     * die immer true zurückgibt und den Tenant-Header nicht erfordert.
     */
    class NoOpTenantContextInterceptor(private val tenantServiceApi: TenantServiceApi) : TenantContextInterceptor(tenantServiceApi) {
        override fun preHandle(
            request: HttpServletRequest,
            response: HttpServletResponse,
            handler: Any,
        ): Boolean {
            // Setze einen Standard-Tenant für Tests
            val tenantIdHeader = request.getHeader("X-Tenant-ID")
            if (tenantIdHeader != null) {
                try {
                    val tenantId = UUID.fromString(tenantIdHeader)
                    TenantContextHolder.setTenantId(tenantId)
                } catch (e: Exception) {
                    // Bei Fehler: Standard-UUID
                    TenantContextHolder.setTenantId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                }
            } else {
                // Falls kein Header existiert: Standard-UUID
                TenantContextHolder.setTenantId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
            }

            return true // Immer erlauben
        }
    }

    /**
     * Fügt den NoOp-Interceptor zur Registry hinzu
     */
    override fun addInterceptors(registry: InterceptorRegistry) {
        // Nur den NoOp-Interceptor hinzufügen
        registry.addInterceptor(tenantContextInterceptor(tenantServiceApi()))
    }
}
