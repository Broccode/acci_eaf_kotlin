package com.acci.eaf.core

import com.acci.eaf.core.api.PingCommand
import com.acci.eaf.core.api.PongEvent
import org.axonframework.commandhandling.gateway.CommandGateway
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.UUID
import java.util.concurrent.TimeUnit
import org.awaitility.Awaitility
import org.awaitility.core.ConditionTimeoutException
import java.time.Duration

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
        
        // Ensure the test starts with a clean event list
        testEventHandler.clearEvents()
        
        // When
        val result = commandGateway.sendAndWait<UUID>(pingCommand, 10, TimeUnit.SECONDS)
        
        // Then
        assertEquals(messageId, result)
        
        try {
            // Wait for the event with retry mechanism
            Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(100))
                .until {
                    val events = testEventHandler.getReceivedEvents()
                    logger.debug("Current events: $events")
                    events.any { it.payload is PongEvent && (it.payload as PongEvent).messageId == messageId }
                }
            
            // Final verification after successful waiting
            val events = testEventHandler.getReceivedEvents()
            logger.info("Received events: $events")
            
            assertNotNull(events.find { it.payload is PongEvent && (it.payload as PongEvent).messageId == messageId })
        } catch (e: ConditionTimeoutException) {
            logger.error("Timeout waiting for PongEvent. Current events: ${testEventHandler.getReceivedEvents()}")
            throw e
        }
    }
} 