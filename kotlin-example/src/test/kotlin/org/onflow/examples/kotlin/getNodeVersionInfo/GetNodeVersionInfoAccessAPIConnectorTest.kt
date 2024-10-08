package org.onflow.examples.kotlin.getNodeVersionInfo

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.onflow.flow.common.test.FlowEmulatorProjectTest
import org.onflow.flow.common.test.FlowTestClient
import org.onflow.flow.sdk.FlowAccessApi
import org.onflow.flow.sdk.FlowChainId
import org.onflow.flow.sdk.FlowNodeVersionInfo

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
internal class GetNodeVersionInfoAccessAPIConnectorTest {
    @FlowTestClient
    lateinit var accessAPI: FlowAccessApi

    private lateinit var nodeVersionInfoConnector: GetNodeVersionInfoAccessAPIConnector

    @BeforeEach
    fun setup() {
        nodeVersionInfoConnector = GetNodeVersionInfoAccessAPIConnector(accessAPI)
    }

    @Test
    fun `Can fetch network parameters`() {
        val nodeVersionInfo: FlowNodeVersionInfo = nodeVersionInfoConnector.getNodeVersionInfo()
        assertNotNull(nodeVersionInfo, "Node version info should not be null")
        assertEquals(nodeVersionInfo.protocolVersion, 0)
        assertEquals(nodeVersionInfo.sporkRootBlockHeight, 0)
        assertEquals(nodeVersionInfo.nodeRootBlockHeight, 0)
        assertEquals(nodeVersionInfo.compatibleRange, null)
    }
}
