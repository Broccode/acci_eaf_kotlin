package com.acci.eaf.core

import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

/**
 * Hauptklasse für die EAF Core Spring Boot Anwendung.
 * Konfiguriert die Spring Boot Anwendung mit Axon Framework Integration.
 */
@SpringBootApplication
public class EafCoreApplication {

    private val logger = LoggerFactory.getLogger(EafCoreApplication::class.java)

    /**
     * Konfiguriert einen in-memory Event Store für Entwicklungs- und Testzwecke.
     * In einer Produktionsumgebung würde dies durch eine persistente Implementierung ersetzt werden.
     */
    @Bean
    public fun eventStore(storageEngine: InMemoryEventStorageEngine): EmbeddedEventStore {
        logger.info("Configuring in-memory event store for development")
        return EmbeddedEventStore.builder()
            .storageEngine(storageEngine)
            .build()
    }

    /**
     * Konfiguriert die in-memory Event Storage Engine für Entwicklungs- und Testzwecke.
     */
    @Bean
    public fun storageEngine(): InMemoryEventStorageEngine {
        return InMemoryEventStorageEngine()
    }

    public companion object {
        /**
         * Haupteinstiegspunkt für die Anwendung.
         */
        @JvmStatic
        public fun main(args: Array<String>) {
            runApplication<EafCoreApplication>(*args) {
                // Anwendungskonfiguration hier
            }
        }
    }
} 
