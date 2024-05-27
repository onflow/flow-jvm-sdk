package org.onflow.flow.sdk

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

class SdkConfigTest {

    @Test
    fun testSetLogger() {
        val customLogger = LoggerFactory.getLogger("CustomLogger")
        val sdkConfig = SdkConfig()
        sdkConfig.setLogger(customLogger)
        assertEquals("CustomLogger", LoggerProvider.logger.name)
    }
}

