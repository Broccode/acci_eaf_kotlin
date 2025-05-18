package com.acci.eaf.core

import java.util.concurrent.CopyOnWriteArrayList
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.EventMessage
import org.springframework.stereotype.Component

/**
 * Ein Event-Handler für Tests, der alle empfangenen Events protokolliert.
 * Kann in Tests verwendet werden, um zu überprüfen, ob bestimmte Events ausgelöst wurden.
 */
@Component
@ProcessingGroup("testEventHandler")
public class TestEventHandler {
    private val receivedEvents = CopyOnWriteArrayList<EventMessage<*>>()

    @EventHandler
    public fun handle(event: EventMessage<*>) {
        receivedEvents.add(event)
    }

    public fun getReceivedEvents(): List<EventMessage<*>> = receivedEvents.toList()

    public fun clearEvents() {
        receivedEvents.clear()
    }
} 
