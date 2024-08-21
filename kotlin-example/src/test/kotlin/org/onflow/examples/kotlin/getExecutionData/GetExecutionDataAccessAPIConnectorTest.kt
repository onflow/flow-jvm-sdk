package org.onflow.examples.kotlin.getExecutionData

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.onflow.flow.common.test.FlowEmulatorProjectTest
import org.onflow.flow.common.test.FlowTestClient
import org.onflow.flow.sdk.*

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
class GetExecutionDataAccessAPIConnectorTest {
    @FlowTestClient
    lateinit var accessAPI: FlowAccessApi

    private lateinit var connector: GetExecutionDataAccessAPIConnector
    private lateinit var blockId: FlowId

    @BeforeEach
    fun setup() {
        connector = GetExecutionDataAccessAPIConnector(accessAPI)

        // Set a specific block ID
        val block = when (val response = accessAPI.getLatestBlock()) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
        }
        blockId = block.id
    }

    @Test
    fun `Can fetch execution data by block ID`() {
        val executionData: FlowExecutionResult = connector.getExecutionDataByBlockId(blockId)

        assertNotNull(executionData, "Execution data should not be null")

        assertTrue(executionData.chunks.isNotEmpty(), "Execution data should contain chunks")

        executionData.chunks.forEachIndexed { chunkNo, chunk ->
            assertTrue(chunk.numberOfTransactions > 0, "Chunk $chunkNo should contain transactions")
        }
    }
}
