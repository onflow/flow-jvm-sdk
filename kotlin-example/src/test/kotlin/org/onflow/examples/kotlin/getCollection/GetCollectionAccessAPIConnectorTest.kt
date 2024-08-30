package org.onflow.examples.kotlin.getCollection

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.onflow.flow.common.test.FlowEmulatorProjectTest
import org.onflow.flow.common.test.FlowTestClient
import org.onflow.flow.sdk.FlowAccessApi
import org.onflow.flow.sdk.FlowId

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
class GetCollectionAccessAPIConnectorTest {
    @FlowTestClient
    lateinit var accessAPI: FlowAccessApi

    private lateinit var connector: GetCollectionAccessAPIConnector

    private lateinit var collectionId: FlowId

    @BeforeEach
    fun setup() {
        connector = GetCollectionAccessAPIConnector(accessAPI)

        val block = when (val response = accessAPI.getLatestBlock()) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
        }
        collectionId = block.collectionGuarantees.first().id
    }

//    @Test
//    fun `Can fetch collection by ID`() {
//        val collection: FlowCollection = connector.getCollectionById(collectionId)
//
//        assertNotNull(collection, "Collection should not be null")
//        assertEquals(collectionId, collection.id, "Collection ID should match the fetched collection ID")
//    }
}
