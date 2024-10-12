package org.onflow.flow.sdk.models

import org.onflow.flow.sdk.FlowId
import org.onflow.flow.sdk.hexToBytes
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.onflow.flow.sdk.fixedSize
import org.junit.jupiter.api.Test

class FlowIdTest {
    @Test
    fun `Can create FlowId from a hex string`() {
        Assertions.assertThat(FlowId("0x01").bytes).isEqualTo("0000000000000000000000000000000000000000000000000000000000000001".hexToBytes())
        Assertions.assertThat(FlowId("01").base16Value).isEqualTo("0000000000000000000000000000000000000000000000000000000000000001")
        Assertions.assertThat(FlowId("00").base16Value).isEqualTo("0000000000000000000000000000000000000000000000000000000000000000")
        Assertions.assertThat(FlowId("01").base16Value).isEqualTo("0000000000000000000000000000000000000000000000000000000000000001")
        Assertions.assertThat(FlowId("10").base16Value).isEqualTo("0000000000000000000000000000000000000000000000000000000000000010")
        Assertions.assertThat(FlowId("5e6ef76c524dd131bbab5f9965493b7830bb784561ca6391b320ec60fa5c395e").base16Value).isEqualTo("5e6ef76c524dd131bbab5f9965493b7830bb784561ca6391b320ec60fa5c395e")
    }

    @Test
    fun `Can create FlowId from a byte array`() {
        Assertions.assertThat(FlowId.of("01".hexToBytes()).bytes).isEqualTo("0000000000000000000000000000000000000000000000000000000000000001".hexToBytes())
        Assertions.assertThat(FlowId.of("0x01".hexToBytes()).base16Value).isEqualTo("0000000000000000000000000000000000000000000000000000000000000001")
        Assertions.assertThat(FlowId.of(byteArrayOf()).base16Value).isEqualTo("0000000000000000000000000000000000000000000000000000000000000000")
        Assertions.assertThat(FlowId.of("01".hexToBytes()).base16Value).isEqualTo("0000000000000000000000000000000000000000000000000000000000000001")
        Assertions.assertThat(FlowId.of("10".hexToBytes()).base16Value).isEqualTo("0000000000000000000000000000000000000000000000000000000000000010")
        Assertions
            .assertThat(FlowId.of("5e6ef76c524dd131bbab5f9965493b7830bb784561ca6391b320ec60fa5c395e".hexToBytes()).base16Value)
            .isEqualTo("5e6ef76c524dd131bbab5f9965493b7830bb784561ca6391b320ec60fa5c395e")
    }

    @Test
    fun `Throws error creating FlowId from invalid input`() {
        Assertions.assertThatThrownBy { FlowId.of("0x1".hexToBytes()).base16Value }
        Assertions.assertThatThrownBy { FlowId.of("x".hexToBytes()).base16Value }
        Assertions.assertThatThrownBy { FlowId.of("1".hexToBytes()).base16Value }
        Assertions.assertThatThrownBy { FlowId.of("0k".hexToBytes()).base16Value }
        Assertions.assertThatThrownBy { FlowId.of("0".hexToBytes()).base16Value }
        Assertions.assertThatThrownBy { FlowId.of("0000000000000000000000000000000000000000000000000000000000000001234".hexToBytes()).bytes }
        Assertions.assertThatThrownBy { FlowId("0x1").base16Value }
        Assertions.assertThatThrownBy { FlowId("x").base16Value }
        Assertions.assertThatThrownBy { FlowId("1").base16Value }
        Assertions.assertThatThrownBy { FlowId("0").base16Value }
        Assertions.assertThatThrownBy { FlowId("0000000000000000000000000000000000000000000000000000000000000001234").bytes }
    }

    @Test
    fun `Test equality`() {
        val bytes1 = byteArrayOf(0, 1, 2, 3, 4, 5, 6, 7)
        val bytes2 = byteArrayOf(0, 1, 2, 3, 4, 5, 6, 7)
        val flowId1 = FlowId.of(bytes1)
        val flowId2 = FlowId.of(bytes2)
        assertEquals(flowId1, flowId2)
    }

    @Test
    fun `Test inequality`() {
        val bytes1 = byteArrayOf(0, 1, 2, 3, 4, 5, 6, 7)
        val bytes2 = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8)
        val flowId1 = FlowId.of(bytes1)
        val flowId2 = FlowId.of(bytes2)
        assertEquals(false, flowId1 == flowId2)
    }

    @Test
    fun `Test hashCode with no padding of 32 byte array`() {
        val bytes = byteArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 0, 1, 2, 3, 4, 5, 6, 7, 0, 1, 2, 3, 4, 5, 6, 7, 0, 1, 2, 3, 4, 5, 6, 7)
        val flowId = FlowId.of(bytes)
        assertEquals(bytes.contentHashCode(), flowId.hashCode())
    }

    @Test
    fun `Test hashCode with padding`() {
        val bytes = byteArrayOf(0, 1, 2, 3, 4, 5, 6, 7)
        val flowId = FlowId.of(bytes)
        assertEquals(fixedSize(bytes, 32).contentHashCode(), flowId.hashCode())
    }
}
