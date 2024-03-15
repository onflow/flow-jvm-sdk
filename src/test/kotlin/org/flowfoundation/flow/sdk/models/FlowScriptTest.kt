package org.flowfoundation.flow.sdk.models

import org.flowfoundation.flow.sdk.FlowScript
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FlowScriptTest {
    @Test
    fun `Test initialization from String`() {
        val script = "sample script"
        val flowScript = FlowScript(script)

        assertEquals(script.encodeToByteArray().contentToString(), flowScript.bytes.contentToString())
    }

    @Test
    fun `Test equals`() {
        val bytes = byteArrayOf(1, 2, 3)
        val flowScript1 = FlowScript(bytes)
        val flowScript2 = FlowScript(bytes)

        assertEquals(flowScript1, flowScript2)
    }

    @Test
    fun `Test hashCode`() {
        val bytes = byteArrayOf(1, 2, 3)
        val flowScript = FlowScript(bytes)

        assertEquals(bytes.contentHashCode(), flowScript.hashCode())
    }
}
