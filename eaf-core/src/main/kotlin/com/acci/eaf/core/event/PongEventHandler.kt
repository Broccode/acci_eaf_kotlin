package com.acci.eaf.core.event

import com.acci.eaf.core.api.PongEvent
import org.axonframework.eventhandling.EventHandler
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Ein einfacher Event-Handler, der PongEvents verarbeitet.
 * Dient als Beispiel für die Axon-Integration.
 */
@Component
public class PongEventHandler {

    private val logger = LoggerFactory.getLogger(PongEventHandler::class.java)

    /**
     * Verarbeitet ein PongEvent.
     * 
     * @param event Das zu verarbeitende PongEvent
     */
    @EventHandler
    public fun handle(event: PongEvent) {
        logger.info("Received PongEvent with messageId: ${event.messageId}")
        logger.info("Event timestamp: ${event.timestamp}")
        // In einer realen Anwendung würde hier weitere Verarbeitungslogik implementiert werden
    }
} 