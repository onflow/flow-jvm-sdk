package org.onflow.examples.kotlin.executeScript

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.onflow.flow.common.test.FlowEmulatorProjectTest
import org.onflow.flow.common.test.FlowTestClient
import org.onflow.flow.sdk.*

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
        assertEquals(15, result.jsonCadence.decode<Int>())
    }

    @Test
    fun `Can execute complex script`() {
        val result = scriptExecutionExample.executeComplexScript()

        val storageInfoList = result.jsonCadence.decode<List<ExecuteScriptAccessAPIConnector.StorageInfo>>()

        assertNotNull(storageInfoList, "Storage info list should not be null")
        assertEquals(1, storageInfoList.size, "Expected exactly one StorageInfo object")

        val storageInfo = storageInfoList[0]

        assertEquals(1, storageInfo.capacity, "Expected capacity to be 1")
        assertEquals(2, storageInfo.used, "Expected used to be 2")
        assertEquals(3, storageInfo.available, "Expected available to be 3")
    }
}
