package org.onflow.examples.kotlin.streaming.streamExecutionData

import kotlinx.coroutines.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.onflow.examples.kotlin.AccessAPIConnector
import org.onflow.flow.common.test.*
import org.onflow.flow.sdk.*
import org.onflow.flow.sdk.crypto.Crypto

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
internal class SubscribeExecutionDataExampleTest {

    @FlowServiceAccountCredentials
    lateinit var serviceAccount: TestAccount

    @FlowTestClient
    lateinit var accessAPI: FlowAccessApi

    private lateinit var subscribeExecutionDataExample: SubscribeExecutionDataExample
    private lateinit var accessAPIConnector: AccessAPIConnector

    @BeforeEach
    fun setup() {
        accessAPIConnector = AccessAPIConnector(serviceAccount.privateKey, accessAPI)
        subscribeExecutionDataExample = SubscribeExecutionDataExample(accessAPI)
    }

    @Test
    fun `Can stream execution data`() = runBlocking {
        val scope = this
        val receivedExecutionData = mutableListOf<FlowBlockExecutionData>()

        val executionDataJob = launch {
            subscribeExecutionDataExample.streamExecutionData(scope, receivedExecutionData)
        }

        // Simulate a delay to allow execution data stream to start
        delay(5000L)

        // Trigger a sample transaction to generate execution data
        val publicKey = Crypto.generateKeyPair(SignatureAlgorithm.ECDSA_P256).public
        accessAPIConnector.sendSampleTransaction(
            serviceAccount.flowAddress,
            publicKey
        )

        delay(5000L)
        executionDataJob.cancelAndJoin()

        // Validate that execution data has been received and processed
        assertTrue(receivedExecutionData.isNotEmpty(), "Should have received at least one block execution data")
        receivedExecutionData.forEach { blockExecutionData ->
            assertNotNull(blockExecutionData.blockId, "Block ID should not be null")
            assertTrue(blockExecutionData.chunkExecutionData.isNotEmpty(), "Chunk execution data should not be empty")
            blockExecutionData.chunkExecutionData.forEach { chunkExecutionData ->
                assertTrue(chunkExecutionData.transactionResults.isNotEmpty(), "Transactions should not be empty")
                chunkExecutionData.transactionResults.forEach { transaction ->
                    assertNotNull(transaction.transactionId, "Transaction ID should not be null")
                }
            }
        }
    }
}
