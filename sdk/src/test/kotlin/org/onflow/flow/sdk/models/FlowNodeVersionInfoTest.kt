package org.onflow.flow.sdk.models

import com.google.protobuf.UnsafeByteOperations
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.onflow.flow.sdk.FlowCompatibleRange
import org.onflow.flow.sdk.FlowNodeVersionInfo
import org.onflow.protobuf.entities.NodeVersionInfoOuterClass

class FlowNodeVersionInfoTest {
    @Test
    fun `FlowNodeVersionInfo builder`() {
        val sporkId = byteArrayOf(0x01, 0x02)
        val versionInfo = FlowNodeVersionInfo(
            semver = "1.0.0",
            commit = "commit1",
            sporkId = sporkId,
            protocolVersion = 1L,
            sporkRootBlockHeight = 100L,
            nodeRootBlockHeight = 200L,
            compatibleRange = null
        )

        // Test builder function
        val builder = versionInfo.builder()
        assertEquals(versionInfo.semver, builder.semver)
        assertEquals(versionInfo.commit, builder.commit)
        assertArrayEquals(versionInfo.sporkId, builder.sporkId.toByteArray())
        assertEquals(versionInfo.protocolVersion, builder.protocolVersion)
        assertEquals(versionInfo.sporkRootBlockHeight, builder.sporkRootBlockHeight)
        assertEquals(versionInfo.nodeRootBlockHeight, builder.nodeRootBlockHeight)
    }

    @Test
    fun `FlowNodeVersionInfo of method`() {
        val sporkId = byteArrayOf(0x01, 0x02)
        val protoVersionInfo = NodeVersionInfoOuterClass.NodeVersionInfo.newBuilder()
            .setSemver("1.0.0")
            .setCommit("commit1")
            .setSporkId(UnsafeByteOperations.unsafeWrap(sporkId))
            .setProtocolVersion(1L)
            .setSporkRootBlockHeight(100L)
            .setNodeRootBlockHeight(200L)
            .build()

        // Test of() method
        val versionInfo = FlowNodeVersionInfo.of(protoVersionInfo)

        assertEquals("1.0.0", versionInfo.semver)
        assertEquals("commit1", versionInfo.commit)
        assertArrayEquals(sporkId, versionInfo.sporkId)
        assertEquals(1L, versionInfo.protocolVersion)
        assertEquals(100L, versionInfo.sporkRootBlockHeight)
        assertEquals(200L, versionInfo.nodeRootBlockHeight)
        assertNull(versionInfo.compatibleRange)
    }

    @Test
    fun `FlowCompatibleRange equality and hashcode`() {
        val range1 = FlowCompatibleRange(startHeight = 1L, endHeight = 10L)
        val range2 = FlowCompatibleRange(startHeight = 1L, endHeight = 10L)
        val range3 = FlowCompatibleRange(startHeight = 2L, endHeight = 15L)

        // Test equality
        assertEquals(range1, range2)
        assertNotEquals(range1, range3)

        // Test hashcode
        assertEquals(range1.hashCode(), range2.hashCode())
        assertNotEquals(range1.hashCode(), range3.hashCode())
    }

    @Test
    fun `FlowNodeVersionInfo equality and hashcode`() {
        val sporkId1 = byteArrayOf(0x01, 0x02)
        val sporkId2 = byteArrayOf(0x01, 0x02)
        val sporkId3 = byteArrayOf(0x03, 0x04)

        val versionInfo1 = FlowNodeVersionInfo(
            semver = "1.0.0",
            commit = "commit1",
            sporkId = sporkId1,
            protocolVersion = 1L,
            sporkRootBlockHeight = 100L,
            nodeRootBlockHeight = 200L,
            compatibleRange = FlowCompatibleRange(startHeight = 1L, endHeight = 10L)
        )

        val versionInfo2 = FlowNodeVersionInfo(
            semver = "1.0.0",
            commit = "commit1",
            sporkId = sporkId2,
            protocolVersion = 1L,
            sporkRootBlockHeight = 100L,
            nodeRootBlockHeight = 200L,
            compatibleRange = FlowCompatibleRange(startHeight = 1L, endHeight = 10L)
        )

        val versionInfo3 = FlowNodeVersionInfo(
            semver = "2.0.0",
            commit = "commit2",
            sporkId = sporkId3,
            protocolVersion = 2L,
            sporkRootBlockHeight = 150L,
            nodeRootBlockHeight = 250L,
            compatibleRange = null
        )

        // Test equality
        assertEquals(versionInfo1, versionInfo2)
        assertNotEquals(versionInfo1, versionInfo3)

        // Test hashcode
        assertEquals(versionInfo1.hashCode(), versionInfo2.hashCode())
        assertNotEquals(versionInfo1.hashCode(), versionInfo3.hashCode())
    }
}
