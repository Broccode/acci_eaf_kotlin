package com.acci.eaf.core.api

import java.time.Instant
import java.util.UUID

/**
 * Ein einfaches Testereignis, das als Antwort auf ein PingCommand erzeugt wird.
 *
 * @property messageId Die eindeutige ID des Events, entspricht der ID des auslösenden PingCommand
 * @property timestamp Der Zeitpunkt, zu dem das Event erzeugt wurde
 */
public data class PongEvent(val messageId: UUID, val timestamp: Instant = Instant.now())
