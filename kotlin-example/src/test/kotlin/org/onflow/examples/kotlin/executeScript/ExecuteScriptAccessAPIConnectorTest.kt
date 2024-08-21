package org.onflow.examples.kotlin.executeScript

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.onflow.flow.common.test.FlowEmulatorProjectTest
import org.onflow.flow.common.test.FlowTestClient
import org.onflow.flow.sdk.*
import java.math.BigDecimal

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
class ScriptExecutionExampleTest {

    @FlowTestClient
    lateinit var accessAPI: FlowAccessApi

    private lateinit var scriptExecutionExample: ExecuteScriptAccessAPIConnector

    @BeforeEach
    fun setup() {
        scriptExecutionExample = ExecuteScriptAccessAPIConnector(accessAPI)
    }

    @Test
    fun `Can execute simple script`() {
        val result = scriptExecutionExample.executeSimpleScript()

        assertNotNull(result, "Result should not be null")
        assertEquals(15, result.jsonCadence)
    }

    @Test
    fun `Can execute complex script`() {
        val user = scriptExecutionExample.executeComplexScript()

        assertNotNull(user, "User should not be null")
        assertEquals("Dete", user.name)
        assertEquals("0x1", user.address.base16Value)
        assertEquals(BigDecimal("10.0"), user.balance)
    }
}
