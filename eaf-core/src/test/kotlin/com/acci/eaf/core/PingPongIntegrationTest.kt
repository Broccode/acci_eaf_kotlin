package com.acci.eaf.core

import com.acci.eaf.core.api.PingCommand
import com.acci.eaf.core.api.PongEvent
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.EventMessage
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.stereotype.Component
import org.springframework.test.context.ActiveProfiles
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit

@SpringBootTest
@ActiveProfiles("test")
public class PingPongIntegrationTest {
    
    private val logger = LoggerFactory.getLogger(PingPongIntegrationTest::class.java)
    
    @Autowired
    private lateinit var commandGateway: CommandGateway
    
    @Autowired
    private lateinit var testEventHandler: TestEventHandler
    
    @Test
    public fun `ping command should trigger pong event`() {
        // Given
        val pingCommand = PingCommand()
        val messageId = pingCommand.messageId
        logger.info("Sending PingCommand with ID: $messageId")
        
        // When
        val result = commandGateway.sendAndWait<UUID>(pingCommand, 5, TimeUnit.SECONDS)
        
        // Then
        assertEquals(messageId, result)
        
        // Wait for event processing
        Thread.sleep(1000)
        
        // Verify event was received
        val events = testEventHandler.getReceivedEvents()
        logger.info("Received events: $events")
        
        assertNotNull(events.find { it.payload is PongEvent && (it.payload as PongEvent).messageId == messageId })
    }
    
    @Component
    public class TestEventHandler {
        private val receivedEvents = CopyOnWriteArrayList<EventMessage<*>>()
        
        @EventHandler
        public fun handle(event: EventMessage<*>) {
            receivedEvents.add(event)
        }
        
        public fun getReceivedEvents(): List<EventMessage<*>> = receivedEvents.toList()
    }
} 