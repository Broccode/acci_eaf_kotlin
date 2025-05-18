package com.acci.eaf.core

import org.axonframework.commandhandling.CommandBus
import org.axonframework.commandhandling.SimpleCommandBus
import org.axonframework.config.Configuration
import org.axonframework.config.DefaultConfigurer
import org.axonframework.eventhandling.EventBus
import org.axonframework.eventhandling.SimpleEventBus
import org.axonframework.queryhandling.QueryBus
import org.axonframework.queryhandling.SimpleQueryBus
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

/**
 * Test-Konfiguration für Axon Framework.
 * Diese Konfiguration stellt ein vereinfachtes Axon-Framework ohne Multi-Tenancy für Tests bereit.
 */
@org.springframework.context.annotation.Configuration
@Profile("test")
class TestConfiguration {

    /**
     * Stellt einen einfachen CommandBus für Tests bereit, der nicht vom TenantMessageHandlerInterceptor abhängt
     */
    @Bean
    @Primary
    fun simpleCommandBus(): CommandBus = SimpleCommandBus.builder().build()

    /**
     * Stellt einen einfachen EventBus für Tests bereit, der nicht vom TenantMessageHandlerInterceptor abhängt
     */
    @Bean
    @Primary
    fun simpleEventBus(): EventBus = SimpleEventBus.builder().build()

    /**
     * Stellt einen einfachen QueryBus für Tests bereit, der nicht vom TenantMessageHandlerInterceptor abhängt
     */
    @Bean
    @Primary
    fun simpleQueryBus(): QueryBus = SimpleQueryBus.builder().build()

    /**
     * Stellt eine vereinfachte Axon-Konfiguration für Tests bereit,
     * die keine zyklischen Abhängigkeiten verursacht
     */
    @Bean
    @Primary
    fun axonConfiguration(
        commandBus: CommandBus,
        eventBus: EventBus,
        queryBus: QueryBus,
        applicationContext: ApplicationContext,
    ): Configuration {
        val config = DefaultConfigurer.defaultConfiguration()
            .configureCommandBus { _ -> commandBus }
            .configureEventBus { _ -> eventBus }
            .configureQueryBus { _ -> queryBus }
            .registerComponent(ApplicationContext::class.java) { _ -> applicationContext }
            .buildConfiguration()

        // Starte die Konfiguration direkt
        config.start()

        return config
    }
}
