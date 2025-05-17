package com.acci.eaf.core

import java.time.LocalDateTime
import org.slf4j.LoggerFactory

/**
 * Hauptklasse, die den Zugriff auf die Core-Funktionalität des EAF bietet.
 * Implementiert das Singleton-Pattern, um sicherzustellen, dass es nur eine Instanz gibt.
 */
public class EafCore private constructor() {
    private val logger = LoggerFactory.getLogger(EafCore::class.java)
    private val startupTime: LocalDateTime = LocalDateTime.now()

    /**
     * Enthält grundlegende Informationen über diese Instanz des EAF Core.
     */
    public data class Info(
        val version: String = VERSION,
        val buildTimestamp: String = BUILD_TIMESTAMP,
        val startupTime: LocalDateTime,
    )

    /**
     * Liefert Informationen über diese EAF Core-Instanz.
     *
     * @return [Info] Objekt mit Versions- und Zeitstempelinformationen
     */
    public fun getInfo(): Info {
        return Info(startupTime = startupTime)
    }

    /**
     * Initialisiert diese EAF Core-Instanz.
     */
    public fun initialize() {
        logger.info("Initializing EAF Core v$VERSION")
        // Zusätzliche Initialisierungslogik hier
    }

    public companion object {
        /**
         * Die aktuelle Version des EAF Core.
         */
        public const val VERSION: String = "0.1.0"

        /**
         * Der Build-Zeitstempel für diese Version.
         */
        public const val BUILD_TIMESTAMP: String = "2023-03-24T00:00:00Z"

        private val INSTANCE = EafCore()

        /**
         * Gibt die Singleton-Instanz von EafCore zurück.
         */
        @JvmStatic
        public fun getInstance(): EafCore = INSTANCE
    }
}
