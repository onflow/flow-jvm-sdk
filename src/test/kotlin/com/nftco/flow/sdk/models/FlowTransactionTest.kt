package com.nftco.flow.sdk.models

import com.nftco.flow.sdk.*
import com.nftco.flow.sdk.cadence.StringField
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FlowTransactionTest {
    @Test
    fun `Test builder`() {
        val flowTransaction = createSampleFlowTransaction()

        val builder = flowTransaction.builder()
        val builtTransaction = builder.build()

        print(builtTransaction.toBuilder().toString())

        assertEquals("sample script", builtTransaction.script.toString(Charsets.UTF_8))
        assertEquals(FlowArgument(StringField("argument")).byteStringValue, builtTransaction.argumentsList[0])
        assertEquals(FlowId("0x1234").byteStringValue, builtTransaction.referenceBlockId)
        assertEquals(1000L, builtTransaction.gasLimit)
        assertEquals(true, FlowAddress.of("0x01".hexToBytes()).bytes.contentEquals(builtTransaction.proposalKey.address.toByteArray()))
        assertEquals(EXPECTED_PROPOSAL_KEY_INDEX, builtTransaction.proposalKey.keyId)
        assertEquals(12345L, builtTransaction.proposalKey.sequenceNumber)
        assertEquals(true, FlowAddress.of("0x02".hexToBytes()).bytes.contentEquals(builtTransaction.payer.toByteArray()))
        assertEquals(true, FlowAddress.of("0x03".hexToBytes()).bytes.contentEquals(builtTransaction.authorizersList[0].toByteArray()))
    }

    @Test
    fun `Test canonicalPayload`() {
        val flowTransaction = createSampleFlowTransaction()

        val canonicalPayload = flowTransaction.canonicalPayload

        assertEquals(true, canonicalPayload.isNotEmpty())
        assertEquals(122, canonicalPayload.size)
    }

    @Test
    fun `Test canonicalAuthorizationEnvelope`() {
        val flowTransaction = createSampleFlowTransaction()

        val canonicalAuthorizationEnvelope = flowTransaction.canonicalAuthorizationEnvelope

        assertEquals(true, canonicalAuthorizationEnvelope.isNotEmpty())
        assertEquals(125, canonicalAuthorizationEnvelope.size)
    }

    @Test
    fun `Test canonicalPaymentEnvelope`() {
        val flowTransaction = createSampleFlowTransaction()

        val canonicalPaymentEnvelope = flowTransaction.canonicalPaymentEnvelope

        assertEquals(true, canonicalPaymentEnvelope.isNotEmpty())
        assertEquals(128, canonicalPaymentEnvelope.size)
    }

    @Test
    fun `Test canonicalTransaction`() {
        val flowTransaction = createSampleFlowTransaction()

        val canonicalTransaction = flowTransaction.canonicalTransaction

        assertEquals(true, canonicalTransaction.isNotEmpty())
        assertEquals(126, canonicalTransaction.size)
    }

    @Test
    fun `Test id`() {
        val flowTransaction = createSampleFlowTransaction()
        val id = flowTransaction.id

        assertEquals(true, id.toString().isNotEmpty())
        assertEquals(32, id.bytes.size)
    }

    @Test
    fun `Test signerList`() {
        val flowTransaction = createSampleFlowTransaction()

        val signerList = flowTransaction.signerList

        assertEquals(EXPECTED_SIGNER_LIST_SIZE, signerList.size)
        assertTrue(flowTransaction.proposalKey.address in signerList)
        assertTrue(flowTransaction.payerAddress in signerList)
        flowTransaction.authorizers.forEach { assertTrue(it in signerList) }
    }

    @Test
    fun `Test signerMap`() {
        val flowTransaction = createSampleFlowTransaction()

        val signerMap = flowTransaction.signerMap

        assertEquals(EXPECTED_SIGNER_MAP_SIZE, signerMap.size)
        assertEquals(EXPECTED_PROPOSAL_KEY_INDEX, signerMap[flowTransaction.proposalKey.address])
        assertEquals(EXPECTED_PAYER_INDEX, signerMap[flowTransaction.payerAddress])
        flowTransaction.authorizers.forEachIndexed { index, authorizer -> assertEquals(index + AUTHORIZER_INDEX_OFFSET, signerMap[authorizer]) }
    }

    companion object {
        private const val EXPECTED_SIGNER_LIST_SIZE = 3
        private const val EXPECTED_SIGNER_MAP_SIZE = 3
        private const val EXPECTED_PROPOSAL_KEY_INDEX = 0
        private const val EXPECTED_PAYER_INDEX = 1
        private const val AUTHORIZER_INDEX_OFFSET = 2

        private fun createSampleFlowTransaction(): FlowTransaction {
            return FlowTransaction(
                FlowScript("sample script"),
                listOf(FlowArgument(StringField("argument"))),
                FlowId("0x1234"),
                1000L,
                FlowTransactionProposalKey(FlowAddress.of("0x01".hexToBytes()), 0, 12345L),
                FlowAddress.of("0x02".hexToBytes()),
                listOf(FlowAddress.of("0x03".hexToBytes()))
            )
        }
    }
}
