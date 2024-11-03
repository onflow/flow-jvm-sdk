package org.onflow.examples.kotlin.getProtocolState

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.onflow.flow.common.test.FlowEmulatorProjectTest
import org.onflow.flow.common.test.FlowTestClient
import org.onflow.flow.sdk.FlowAccessApi
import org.onflow.flow.sdk.FlowBlock
import org.onflow.flow.sdk.FlowSnapshot

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
internal class GetProtocolStateAccessAPIConnectorTest {
    @FlowTestClient
    lateinit var accessAPI: FlowAccessApi
    lateinit var block: FlowBlock

    private lateinit var protocolStateConnector: GetProtocolStateAccessAPIConnector

    @BeforeEach
    fun setup() {
        protocolStateConnector = GetProtocolStateAccessAPIConnector(accessAPI)
    }

    @Test
    fun `Can get latest protocol state snapshot`() {
        val latestSnapshot: FlowSnapshot = protocolStateConnector.getLatestProtocolStateSnapshot()
        assertNotNull(latestSnapshot, "Latest snapshot should not be null")
    }

    @Test
    fun `Can get protocol state snapshot by blockId`() {
        block = when (val response = accessAPI.getLatestBlock()) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
        }

        val latestSnapshot: FlowSnapshot = protocolStateConnector.getProtocolStateSnapshotByBlockId(block.id)
        assertNotNull(latestSnapshot, ("Snapshot should not be null"))
    }

    @Test
    fun `Can get protocol state snapshot by height`() {
        block = when (val response = accessAPI.getLatestBlock()) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
        }

        val latestSnapshot: FlowSnapshot = protocolStateConnector.getProtocolStateSnapshotByHeight(block.height)
        assertNotNull(latestSnapshot, ("Snapshot should not be null"))
    }
}
