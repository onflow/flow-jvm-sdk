package org.onflow.examples.kotlin.getCollection

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.onflow.examples.kotlin.AccessAPIConnector
import org.onflow.flow.common.test.FlowEmulatorProjectTest
import org.onflow.flow.common.test.FlowServiceAccountCredentials
import org.onflow.flow.common.test.FlowTestClient
import org.onflow.flow.common.test.TestAccount
import org.onflow.flow.sdk.*
import org.onflow.flow.sdk.crypto.Crypto

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
class GetCollectionAccessAPIConnectorTest {
    @FlowTestClient
    lateinit var accessAPI: FlowAccessApi

    @FlowServiceAccountCredentials
    lateinit var serviceAccount: TestAccount

    private lateinit var connector: GetCollectionAccessAPIConnector
    private lateinit var accessAPIConnector: AccessAPIConnector

    private lateinit var collectionId: FlowId

    @BeforeEach
    fun setup() {
        accessAPIConnector = AccessAPIConnector(serviceAccount.privateKey, accessAPI)
        connector = GetCollectionAccessAPIConnector(accessAPI)

        // Send a sample transaction
        val publicKey = Crypto.generateKeyPair(SignatureAlgorithm.ECDSA_P256).public
        accessAPIConnector.sendSampleTransaction(
            serviceAccount.flowAddress,
            publicKey
        )

        val block = when (val response = accessAPI.getLatestBlock()) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
        }
        collectionId = block.collectionGuarantees.first().id
    }

    @Test
    fun `Can fetch collection by ID`() {
        val collection: FlowCollection = connector.getCollectionById(collectionId)

        assertNotNull(collection, "Collection should not be null")
        assertEquals(collectionId, collection.id, "Collection ID should match the fetched collection ID")
    }

    @Test
    fun `Can fetch full collection by ID`() {
        val collectionTransactions: List<FlowTransaction> = connector.getFullCollectionById(collectionId)

        assertNotNull(collectionTransactions, "Collection transactions should not be null")
        assertTrue(collectionTransactions.isNotEmpty(), "Collection transactions should not be empty")

        val firstTransaction = collectionTransactions.first()
        assertNotNull(firstTransaction.id, "Transaction ID should not be null")
        assertNotNull(firstTransaction.script, "Transaction script should not be null")
    }
}
