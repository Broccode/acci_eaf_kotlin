package com.acci.eaf.core.api

import java.util.UUID
import org.axonframework.modelling.command.TargetAggregateIdentifier

/**
 * Ein einfaches Testkommando, das als Beispiel für die Axon-Integration dient.
 * 
 * @property messageId Die eindeutige ID des Kommandos, dient gleichzeitig als Target-Aggregate-Identifier
 */
public data class PingCommand(
    @TargetAggregateIdentifier val messageId: UUID = UUID.randomUUID()
) 