package com.acci.eaf.core

import org.axonframework.config.EventProcessingConfigurer
import org.axonframework.eventhandling.TrackingEventProcessorConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

/**
 * Testkonfiguration für Axon Framework-Integration.
 * Wird nur im Test-Profil aktiviert und sorgt für eine korrekte
 * Konfiguration des Event-Processings für Testzwecke.
 */
@Configuration
@Profile("test")
public class TestConfiguration {

    /**
     * Konfiguriert das Event-Processing für Tests.
     * Stellt sicher, dass der Event-Handler für Tests im subscribing mode läuft.
     */
    @Autowired
    public fun configureEventProcessing(configurer: EventProcessingConfigurer) {
        // Konfiguriere den testEventHandler als Subscribing Processor für direktes Event-Handling
        configurer.registerSubscribingEventProcessor("testEventHandler")

        // Konfiguriere alle anderen Processors als Tracking mit kleiner Batch-Größe für Tests
        configurer.registerTrackingEventProcessorConfiguration("default") {
            TrackingEventProcessorConfiguration.forSingleThreadedProcessing()
                .andBatchSize(1)
        }
    }
}
