package com.nftco.flow.sdk

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class JsonCadenceTest {
    // previous transaction hash: 5e6ef76c524dd131bbab5f9965493b7830bb784561ca6391b320ec60fa5c395e. could not be found - need to investigate
    @Test
    fun `Can parse new JSON Cadence`() {
        val flow = TestUtils.newMainnetAccessApi()
        val tx = flow.getTransactionResultById(FlowId("543722360de830b4dfbec83c2cb28c68d6fc4dfbab8f6594369ef91f28eba5d3"))!!
        val events = tx.events.map { it.payload.jsonCadence }
        Assertions.assertThat(events).hasSize(12)
    }

    @Test
    fun `Can parse JSON Cadence Restriction Type`() {
        val flow = TestUtils.newMainnetAccessApi()
        val tx = flow.getTransactionResultById(FlowId("a47ebd7480b22f8f27cd9c5b28280bd994a93ed88054c0b7bd824633841ab04b"))!!
        val events = tx.events.map { it.payload.jsonCadence }
        Assertions.assertThat(events).hasSize(4)
    }
}
