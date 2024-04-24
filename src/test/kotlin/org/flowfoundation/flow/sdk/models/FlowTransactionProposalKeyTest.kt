package org.onflow.flow.sdk.models

import com.google.protobuf.ByteString
import org.onflow.flow.sdk.FlowAddress
import org.onflow.flow.sdk.FlowTransactionProposalKey
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.onflow.protobuf.entities.TransactionOuterClass

class FlowTransactionProposalKeyTest {
    @Test
    fun `Test initialization from Transaction Proposal Key`() {
        val addressString = "0x123456"
        val keyIndex = 1
        val sequenceNumber = 12345L

        val proposalKeyBuilder = TransactionOuterClass.Transaction.ProposalKey.newBuilder()
            .setAddress(ByteString.copyFromUtf8(addressString))
            .setKeyId(keyIndex)
            .setSequenceNumber(sequenceNumber)

        val flowProposalKey = FlowTransactionProposalKey.of(proposalKeyBuilder.build())

        assertEquals(FlowAddress.of(addressString.toByteArray()), flowProposalKey.address)
        assertEquals(keyIndex, flowProposalKey.keyIndex)
        assertEquals(sequenceNumber, flowProposalKey.sequenceNumber)
    }

    @Test
    fun `Test builder`() {
        val address = FlowAddress("0x123456")
        val keyIndex = 1
        val sequenceNumber = 12345L

        val flowProposalKey = FlowTransactionProposalKey(address, keyIndex, sequenceNumber)

        val proposalKeyBuilder = flowProposalKey.builder()

        assertEquals(address.byteStringValue, proposalKeyBuilder.address)
        assertEquals(keyIndex, proposalKeyBuilder.keyId)
        assertEquals(sequenceNumber, proposalKeyBuilder.sequenceNumber)
    }
}
