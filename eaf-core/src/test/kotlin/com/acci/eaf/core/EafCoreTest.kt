package com.acci.eaf.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

/**
 * Tests für die [EafCore]-Klasse.
 */
internal class EafCoreTest {

    @Test
    internal fun `getInstance should return the singleton instance`() {
        val instance1 = EafCore.getInstance()
        val instance2 = EafCore.getInstance()

        assertEquals(instance1, instance2, "getInstance sollte immer die gleiche Instanz zurückgeben")
    }

    @Test
    internal fun `getInfo should return valid information`() {
        val eafCore = EafCore.getInstance()
        val info = eafCore.getInfo()

        assertEquals(EafCore.VERSION, info.version)
        assertEquals(EafCore.BUILD_TIMESTAMP, info.buildTimestamp)
        assertNotNull(info.startupTime)
    }

    @Test
    internal fun `initialize should not throw exceptions`() {
        val eafCore = EafCore.getInstance()

        // Sollte keine Exception werfen
        eafCore.initialize()
    }
}
