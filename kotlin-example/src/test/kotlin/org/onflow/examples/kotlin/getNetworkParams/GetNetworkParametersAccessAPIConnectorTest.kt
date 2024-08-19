package org.onflow.examples.kotlin.getNetworkParams

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.onflow.flow.common.test.FlowEmulatorProjectTest
import org.onflow.flow.common.test.FlowTestClient
import org.onflow.flow.sdk.FlowAccessApi
import org.onflow.flow.sdk.FlowChainId

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
internal class GetNetworkParametersAccessAPIConnectorTest {
    @FlowTestClient
    lateinit var accessAPI: FlowAccessApi

    private lateinit var networkParametersConnector: GetNetworkParametersAccessAPIConnector

    @BeforeEach
    fun setup() {
        networkParametersConnector = GetNetworkParametersAccessAPIConnector(accessAPI)
    }

    @Test
    fun `Can fetch network parameters`() {
        val networkParams: FlowChainId = networkParametersConnector.getNetworkParameters()
        assertNotNull(networkParams, "Network parameters should not be null")
        assertTrue(networkParams.id.isNotEmpty(), "Network parameters should have a valid ID")
        assertEquals(networkParams, FlowChainId.EMULATOR)
        assertEquals(networkParams.id, "flow-emulator")
    }
}
