package org.onflow.flow.sdk

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

class LoggerProviderTest {

    @Test
    fun testDefaultLogger() {
        val logger = LoggerProvider.logger
        assertEquals("DefaultLogger", logger.name)
    }

    @Test
    fun testSetCustomLogger() {
        val customLogger = LoggerFactory.getLogger("CustomLogger")
        LoggerProvider.logger = customLogger
        assertEquals("CustomLogger", LoggerProvider.logger.name)
    }
}

