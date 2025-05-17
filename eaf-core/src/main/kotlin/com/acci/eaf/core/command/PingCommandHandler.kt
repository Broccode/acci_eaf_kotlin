package com.acci.eaf.core.command

import com.acci.eaf.core.api.PingCommand
import com.acci.eaf.core.api.PongEvent
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate
import org.slf4j.LoggerFactory
import java.util.UUID

/**
 * Ein einfaches Aggregat, das PingCommands verarbeitet und PongEvents erzeugt.
 * Dient als Beispiel für die Axon-Integration.
 */
@Aggregate
public class PingCommandHandler {

    private val logger = LoggerFactory.getLogger(PingCommandHandler::class.java)

    @AggregateIdentifier
    private lateinit var messageId: UUID

    /**
     * Standardkonstruktor für das Framework.
     */
    public constructor()

    /**
     * Verarbeitet ein PingCommand und erzeugt ein entsprechendes PongEvent.
     * 
     * @param command Das zu verarbeitende PingCommand
     */
    @CommandHandler
    public constructor(command: PingCommand) {
        logger.info("Handling PingCommand with messageId: ${command.messageId}")
        AggregateLifecycle.apply(PongEvent(command.messageId))
    }

    /**
     * Verarbeitet ein PongEvent und aktualisiert den Zustand des Aggregats.
     * 
     * @param event Das zu verarbeitende PongEvent
     */
    @EventSourcingHandler
    public fun on(event: PongEvent) {
        messageId = event.messageId
        logger.info("Applied PongEvent with messageId: ${event.messageId}, timestamp: ${event.timestamp}")
    }
} 