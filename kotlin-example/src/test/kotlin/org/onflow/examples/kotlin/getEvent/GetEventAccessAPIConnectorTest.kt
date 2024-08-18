package org.onflow.examples.kotlin.getEvent

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.onflow.examples.kotlin.AccessAPIConnector
import org.onflow.flow.common.test.*
import org.onflow.flow.sdk.*
import org.onflow.flow.sdk.crypto.Crypto

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
class GetEventsAccessAPIConnectorTest {
    @FlowServiceAccountCredentials
    lateinit var serviceAccount: TestAccount

    @FlowTestAccount
    lateinit var testAccount: TestAccount

    @FlowTestClient
    lateinit var accessAPI: FlowAccessApi

    private lateinit var connector: GetEventAccessAPIConnector
    private lateinit var accessAPIConnector: AccessAPIConnector

    private lateinit var txID: FlowId

    @BeforeEach
    fun setup() {
        accessAPIConnector = AccessAPIConnector(serviceAccount.privateKey, accessAPI)
        connector = GetEventAccessAPIConnector(accessAPI)

        // Send a sample transaction to create an account and capture the transaction ID
        val publicKey = Crypto.generateKeyPair(SignatureAlgorithm.ECDSA_P256).public
        txID = accessAPIConnector.sendSampleTransaction(
            serviceAccount.flowAddress,
            publicKey
        )
    }

    @Test
    fun testGetEventsForHeightRange() = runBlocking {
        val events = connector.getEventsForHeightRange("flow.AccountCreated", 0, 30)
        assertNotNull(events, "Events should not be null")
        assert(events.isNotEmpty()) { "Expected account created events but found none." }
        assert(events.size == 3) { "Expected 3 account created events." }
    }

    @Test
    fun testGetEventsForBlockIds() = runBlocking {
        val latestBlock = when (val latestBlockResponse = accessAPI.getLatestBlockHeader()) {
            is FlowAccessApi.AccessApiCallResponse.Success -> latestBlockResponse.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw RuntimeException(latestBlockResponse.message, latestBlockResponse.throwable)
        }

        val blockIds = listOf(latestBlock.id)
        val events = connector.getEventsForBlockIds("flow.AccountCreated", blockIds)

        assertNotNull(events, "Events should not be null")
        assert(events.isNotEmpty()) { "Expected events for the provided block IDs but found none." }
        assert(events.size == 1) { "Expected 1 account created event." }
    }

    @Test
    fun testAccountCreatedEvents() = runBlocking {
        val events = connector.getAccountCreatedEvents(0, 30)
        assertNotNull(events, "Events should not be null")
        assert(events.isNotEmpty()) { "Expected account created events but found none." }
        events.forEach { block ->
            block.events.forEach { event ->
                assertEquals("flow.AccountCreated", event.type)
            }
        }
    }

    @Test
    fun testTransactionResultEvents() = runBlocking {
        val txResult = connector.getTransactionResult(txID)
        assertNotNull(txResult, "Transaction result should not be null")
        assert(txResult.events.isNotEmpty()) { "Expected events in transaction result but found none." }

        val expectedEventTypes = setOf("Withdrawn", "TokensDeposited", "Deposited", "AccountCreated", "AccountKeyAdded", "TokensWithdrawn", "StorageCapabilityControllerIssued", "CapabilityPublished")

        txResult.events.forEach { event ->
            val eventType = event.type.split(".").last()
            assertTrue(expectedEventTypes.contains(eventType), "Unexpected event type: $eventType")
        }
    }
}
