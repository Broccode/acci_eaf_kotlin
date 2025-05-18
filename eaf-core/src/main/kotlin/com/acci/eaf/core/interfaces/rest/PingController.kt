package com.acci.eaf.core.interfaces.rest

import com.acci.eaf.core.api.PingCommand
import java.util.UUID
import java.util.concurrent.CompletableFuture
import org.axonframework.commandhandling.gateway.CommandGateway
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST-Controller für Ping-Anfragen.
 * Bietet einen Endpunkt zum Testen der Axon-Integration.
 */
@RestController
@RequestMapping("/ping")
public class PingController(private val commandGateway: CommandGateway) {

    private val logger = LoggerFactory.getLogger(PingController::class.java)

    /**
     * Verarbeitet eine Ping-Anfrage, indem ein PingCommand gesendet wird.
     *
     * @return Die ID des gesendeten Befehls
     */
    @PostMapping
    public fun ping(): CompletableFuture<ResponseEntity<Map<String, String>>> {
        val command = PingCommand()
        logger.info("Sending PingCommand with ID: ${command.messageId}")

        return commandGateway.send<UUID>(command)
            .thenApply { messageId ->
                ResponseEntity.ok(
                    mapOf(
                        "message" to "Ping command sent successfully",
                        "commandId" to messageId.toString()
                    )
                )
            }
    }
} 
