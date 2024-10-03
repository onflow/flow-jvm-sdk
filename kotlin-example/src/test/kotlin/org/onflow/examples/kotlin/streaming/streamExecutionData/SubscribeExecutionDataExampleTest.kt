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
        subscribeExecutionDataExample = SubscribeExecutionDataExample(serviceAccount.privateKey, accessAPI)
    }

    @Test
    fun `Can stream execution data`() = runBlocking {
        val testScope = CoroutineScope(Dispatchers.IO + Job())
        val receivedExecutionData = mutableListOf<FlowBlockExecutionData>()

        try {
            val executionDataJob = testScope.launch {
                subscribeExecutionDataExample.streamExecutionData(testScope, receivedExecutionData)
            }

            // Trigger a sample transaction
            val publicKey = Crypto.generateKeyPair(SignatureAlgorithm.ECDSA_P256).public
            accessAPIConnector.sendSampleTransaction(
                serviceAccount.flowAddress,
                publicKey
            )

            delay(3000L)
            testScope.cancel()
            executionDataJob.join()
        } catch (e: CancellationException) {
            println("Test scope cancelled: ${e.message}")
        }

        // Validate that execution data has been received and processed
        assertTrue(receivedExecutionData.isNotEmpty(), "Should have received at least one block execution data")
        receivedExecutionData.forEach { blockExecutionData ->
            assertNotNull(blockExecutionData.blockId, "Block ID should not be null")
            if (blockExecutionData.chunkExecutionData.isNotEmpty()) {
                blockExecutionData.chunkExecutionData.forEach { chunkExecutionData ->
                    if (chunkExecutionData.transactionResults.isNotEmpty()) {
                        chunkExecutionData.transactionResults.forEach { transaction ->
                            assertNotNull(transaction.transactionId, "Transaction ID should not be null")
                        }
                    }
                }
            }
        }
    }
}
