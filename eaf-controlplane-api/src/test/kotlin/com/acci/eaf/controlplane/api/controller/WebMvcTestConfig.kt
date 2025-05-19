package com.acci.eaf.controlplane.api.controller

import com.acci.eaf.core.interfaces.TenantServiceApi
import com.acci.eaf.core.tenant.TenantContextHolder
import com.acci.eaf.core.tenant.TenantContextInterceptor
import jakarta.annotation.PostConstruct
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.util.UUID
import org.mockito.Mockito
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import org.springframework.web.util.UriComponentsBuilder

/**
 * Test-Konfiguration für WebMvcTests des TenantControllers.
 * Diese Konfiguration stellt sicher, dass nur die für den Controller-Test notwendigen
 * Beans geladen werden und keine JPA- oder Datenbank-Abhängigkeiten aktiviert sind.
 */
@TestConfiguration
@EnableWebMvc
@EnableWebSecurity
class WebMvcTestConfig : WebMvcConfigurer {

    @Bean
    @Primary
    fun jwtDecoder(): JwtDecoder = Mockito.mock(NimbusJwtDecoder::class.java)

    /**
     * Mock for JPA EntityManager to prevent autowiring errors
     */
    @Bean
    @Primary
    fun entityManagerFactory(): jakarta.persistence.EntityManagerFactory {
        val emf = Mockito.mock(jakarta.persistence.EntityManagerFactory::class.java)
        val em = Mockito.mock(jakarta.persistence.EntityManager::class.java)
        Mockito.`when`(emf.createEntityManager()).thenReturn(em)
        return emf
    }

    /**
     * Mock a JPA EntityManager bean directly
     */
    @Bean
    @Primary
    fun entityManager(): jakarta.persistence.EntityManager = Mockito.mock(jakarta.persistence.EntityManager::class.java)

    /**
     * Mock for TenantRepository
     */
    @Bean
    @Primary
    fun tenantRepository(): com.acci.eaf.multitenancy.repository.TenantRepository =
        Mockito.mock(com.acci.eaf.multitenancy.repository.TenantRepository::class.java)

    /**
     * Mock for TenantService
     */
    @Bean
    @Primary
    fun tenantService(): com.acci.eaf.multitenancy.service.TenantService = Mockito.mock(com.acci.eaf.multitenancy.service.TenantService::class.java)

    /**
     * Mock for TenantPageService
     */
    @Bean
    @Primary
    fun tenantPageService(): com.acci.eaf.controlplane.api.service.TenantPageService =
        Mockito.mock(com.acci.eaf.controlplane.api.service.TenantPageService::class.java)

    /**
     * Mock for TenantMapper
     */
    @Bean
    @Primary
    fun tenantMapper(): com.acci.eaf.controlplane.api.mapper.TenantMapperInterface =
        Mockito.mock(com.acci.eaf.controlplane.api.mapper.TenantMapperInterface::class.java)

    /**
     * Mock for AuditLogger
     */
    @Bean
    @Primary
    fun auditLogger(): com.acci.eaf.controlplane.api.audit.AuditLogger = Mockito.mock(com.acci.eaf.controlplane.api.audit.AuditLogger::class.java)

    /**
     * Mocks für JPA-spezifische Komponenten
     */
    @Bean
    @Primary
    fun jpaMappingContext(): org.springframework.data.jpa.mapping.JpaMetamodelMappingContext =
        Mockito.mock(org.springframework.data.jpa.mapping.JpaMetamodelMappingContext::class.java)

    /**
     * Stellt einen Mock für TenantServiceApi bereit
     */
    @Bean
    @Primary
    fun tenantServiceApi(): TenantServiceApi = Mockito.mock(TenantServiceApi::class.java)

    /**
     * Stellt einen NoOp-Tenant-Interceptor für Tests bereit
     */
    @Bean
    @Primary
    fun tenantContextInterceptor(tenantServiceApi: TenantServiceApi): TenantContextInterceptor {
        return object : TenantContextInterceptor(tenantServiceApi) {
            override fun preHandle(
                request: HttpServletRequest,
                response: HttpServletResponse,
                handler: Any,
            ): Boolean {
                // Setze eine Standard-Tenant-ID für Tests
                try {
                    val tenantId = UUID.fromString("00000000-0000-0000-0000-000000000001")
                    TenantContextHolder.setTenantId(tenantId)
                } catch (e: Exception) {
                    // Bei Fehlern: Ignorieren und fortfahren
                    println("Fehler beim Setzen der Tenant-ID in WebMvcTestConfig: ${e.message}")
                }

                return true // Immer erlauben für Tests
            }
        }
    }

    /**
     * Konfiguriert einen MockHttpServletRequest für den ServletUriComponentsBuilder
     */
    @PostConstruct
    fun setupMockRequest() {
        val mockRequest = MockHttpServletRequest()
        mockRequest.scheme = "http"
        mockRequest.serverName = "localhost"
        mockRequest.serverPort = 8080
        mockRequest.contextPath = ""
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(mockRequest))

        // Alternativ: Direkte Konfiguration über Reflection
        try {
            val staticContextField = ServletUriComponentsBuilder::class.java
                .getDeclaredField("staticContext")
            staticContextField.isAccessible = true

            val staticContext = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host("localhost")
                .port(8080)
                .path("")
            staticContextField.set(null, staticContext)
        } catch (e: Exception) {
            println("Fehler beim Konfigurieren des ServletUriComponentsBuilder: ${e.message}")
        }
    }

    /**
     * Fügt keine Interceptoren hinzu, um Probleme mit TenantContextInterceptor zu vermeiden
     */
    override fun addInterceptors(registry: InterceptorRegistry) {
        // Keine Interceptoren hinzufügen
    }

    /**
     * Konfiguriert die Security für Tests
     */
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf { it.disable() }
            .authorizeHttpRequests { authorize -> authorize.anyRequest().permitAll() }
        return http.build()
    }
}
