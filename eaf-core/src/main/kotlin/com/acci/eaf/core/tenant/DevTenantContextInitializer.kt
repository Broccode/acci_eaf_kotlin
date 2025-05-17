package com.acci.eaf.core.tenant

import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

/**
 * Initialisiert den TenantContext für die Entwicklungsumgebung.
 * 
 * Diese Komponente wird nur im dev-Profil verwendet und setzt den
 * Standard-Entwicklungs-Tenant in den TenantContextHolder, damit
 * Anfragen ohne explizite Tenant-ID trotzdem bearbeitet werden können.
 * 
 * Dies ist nur für Entwicklungszwecke gedacht und sollte in Produktion
 * nicht verwendet werden.
 */
@Component
@Profile("dev")
class DevTenantContextInitializer(private val devTenantServiceApi: DevTenantServiceApi) : CommandLineRunner {
    
    private val logger = LoggerFactory.getLogger(DevTenantContextInitializer::class.java)
    
    override fun run(vararg args: String?) {
        val defaultTenantId = devTenantServiceApi.getDefaultTenantId()
        
        logger.info("Initializing development environment with default tenant ID: {}", defaultTenantId)
        
        // In einer produktiven Umgebung würde man nie den TenantContext global setzen,
        // aber für Entwicklungszwecke ist dies praktisch.
        // In echten Anwendungsfällen wird der TenantContext pro Request gesetzt.
        TenantContextHolder.setTenantId(defaultTenantId)
        
        logger.info("Default tenant context initialized successfully")
    }
} 