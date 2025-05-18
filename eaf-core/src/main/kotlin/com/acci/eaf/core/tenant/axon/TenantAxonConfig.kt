package com.acci.eaf.core.tenant.axon

import jakarta.annotation.PostConstruct
import org.axonframework.commandhandling.CommandBus
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.commandhandling.gateway.DefaultCommandGateway
import org.axonframework.config.Configurer
import org.axonframework.config.ConfigurerModule
import org.axonframework.eventhandling.EventBus
import org.axonframework.messaging.Message
import org.axonframework.queryhandling.DefaultQueryGateway
import org.axonframework.queryhandling.QueryBus
import org.axonframework.queryhandling.QueryGateway
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Konfiguration für die Integration des Tenant-Kontexts in Axon Framework.
 *
 * Diese Konfiguration registriert die notwendigen Interceptors, um die Tenant-ID
 * in Axon-Nachrichten zu propagieren und zu extrahieren.
 */
@Configuration
class TenantAxonConfig {

    @Autowired
    private lateinit var eventBus: EventBus

    @Autowired
    private lateinit var tenantMessageDispatchInterceptor: TenantMessageDispatchInterceptor<Message<*>>

    @Autowired
    private lateinit var tenantMessageHandlerInterceptor: TenantMessageHandlerInterceptor

    /**
     * Registriert die Interceptors beim EventBus.
     * Diese Methode wird nach der Initialisierung aller benötigten Beans aufgerufen.
     */
    @PostConstruct
    fun configureEventBus() {
        // Registriere den Handler-Interceptor für den EventBus, wenn die Methode verfügbar ist
        try {
            val messageHandlerInterceptorClass = Class.forName("org.axonframework.messaging.MessageHandlerInterceptor")
            val method = eventBus.javaClass.getMethod(
                "registerHandlerInterceptor",
                messageHandlerInterceptorClass
            )
            method.invoke(eventBus, tenantMessageHandlerInterceptor)
        } catch (e: Exception) {
            // EventBus unterstützt möglicherweise keine Handler-Interceptors
            // In diesem Fall verwenden wir nur den Dispatch-Interceptor
        }

        eventBus.registerDispatchInterceptor(tenantMessageDispatchInterceptor)
    }

    /**
     * Erstellt ein ConfigurerModule, das die MessageHandlerInterceptors für
     * Command-, Event- und Query-Busse registriert.
     *
     * @param tenantMessageHandlerInterceptor Der Interceptor für eingehende Nachrichten
     * @return Ein ConfigurerModule zur Registrierung der Interceptors
     */
    @Bean
    fun tenantMessageHandlerInterceptorConfigurer(tenantMessageHandlerInterceptor: TenantMessageHandlerInterceptor): ConfigurerModule =
        ConfigurerModule { configurer: Configurer ->
            // Registriere den Interceptor für alle Message-Handler
            // Da die direkten Methoden nicht verfügbar sind, verwenden wir einen alternativen Ansatz
            // über das Bean im Spring-Kontext. Die Interceptors werden in den entsprechenden
            // CommandBus/EventBus/QueryBus-Konfigurationen registriert.
        }

    /**
     * Konfiguriert den CommandGateway mit dem TenantMessageDispatchInterceptor.
     *
     * @param commandBus Der CommandBus
     * @param tenantMessageDispatchInterceptor Der Interceptor für ausgehende Nachrichten
     * @param tenantMessageHandlerInterceptor Der Interceptor für eingehende Nachrichten
     * @return Ein konfigurierter CommandGateway
     */
    @Bean
    fun commandGateway(
        commandBus: CommandBus,
        tenantMessageDispatchInterceptor: TenantMessageDispatchInterceptor<Message<*>>,
        tenantMessageHandlerInterceptor: TenantMessageHandlerInterceptor,
    ): CommandGateway {
        // Registriere den Handler-Interceptor für den CommandBus
        commandBus.registerHandlerInterceptor(tenantMessageHandlerInterceptor)

        return DefaultCommandGateway.builder()
            .commandBus(commandBus)
            .dispatchInterceptors(tenantMessageDispatchInterceptor)
            .build()
    }

    /**
     * Konfiguriert den QueryGateway mit dem TenantMessageDispatchInterceptor.
     *
     * @param queryBus Der QueryBus
     * @param tenantMessageDispatchInterceptor Der Interceptor für ausgehende Nachrichten
     * @param tenantMessageHandlerInterceptor Der Interceptor für eingehende Nachrichten
     * @return Ein konfigurierter QueryGateway
     */
    @Bean
    fun queryGateway(
        queryBus: QueryBus,
        tenantMessageDispatchInterceptor: TenantMessageDispatchInterceptor<Message<*>>,
        tenantMessageHandlerInterceptor: TenantMessageHandlerInterceptor,
    ): QueryGateway {
        // Registriere den Handler-Interceptor für den QueryBus
        queryBus.registerHandlerInterceptor(tenantMessageHandlerInterceptor)

        return DefaultQueryGateway.builder()
            .queryBus(queryBus)
            .dispatchInterceptors(tenantMessageDispatchInterceptor)
            .build()
    }
}
