package com.nftco.flow.sdk

import com.google.protobuf.ByteString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.onflow.protobuf.access.Access
import org.onflow.protobuf.entities.AccountOuterClass
import org.onflow.protobuf.entities.BlockHeaderOuterClass
import org.onflow.protobuf.entities.BlockOuterClass
import org.onflow.protobuf.entities.TransactionOuterClass

class FlowAccessApiTest {
    @Test
    fun `Test getLatestBlockHeader`() {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val latestBlockHeader = FlowBlockHeader.of(BlockHeaderOuterClass.BlockHeader.getDefaultInstance())
        `when`(flowAccessApi.getLatestBlockHeader(true)).thenReturn(latestBlockHeader)

        val result = flowAccessApi.getLatestBlockHeader()

        assertEquals(latestBlockHeader, result)
    }

    @Test
    fun `Test getBlockHeaderById`() {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val blockId = FlowId("01")
        val blockHeader = FlowBlockHeader.of(BlockHeaderOuterClass.BlockHeader.getDefaultInstance())
        `when`(flowAccessApi.getBlockHeaderById(blockId)).thenReturn(blockHeader)

        val result = flowAccessApi.getBlockHeaderById(blockId)

        assertEquals(blockHeader, result)
    }

    @Test
    fun `Test getBlockHeaderByHeight`() {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val height = 123L
        val blockHeader = FlowBlockHeader.of(BlockHeaderOuterClass.BlockHeader.getDefaultInstance())
        `when`(flowAccessApi.getBlockHeaderByHeight(height)).thenReturn(blockHeader)

        val result = flowAccessApi.getBlockHeaderByHeight(height)

        assertEquals(blockHeader, result)
    }

    @Test
    fun `Test getLatestBlock`() {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val latestBlock = FlowBlock.of(BlockOuterClass.Block.getDefaultInstance())
        `when`(flowAccessApi.getLatestBlock(true)).thenReturn(latestBlock)

        val result = flowAccessApi.getLatestBlock()

        assertEquals(latestBlock, result)
    }

    @Test
    fun `Test getBlockById`() {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val blockId = FlowId("01")
        val block = FlowBlock.of(BlockOuterClass.Block.getDefaultInstance())
        `when`(flowAccessApi.getBlockById(blockId)).thenReturn(block)

        val result = flowAccessApi.getBlockById(blockId)

        assertEquals(block, result)
    }

    @Test
    fun `Test getBlockByHeight`() {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val height = 123L
        val block = FlowBlock.of(BlockOuterClass.Block.getDefaultInstance())
        `when`(flowAccessApi.getBlockByHeight(height)).thenReturn(block)

        val result = flowAccessApi.getBlockByHeight(height)

        assertEquals(block, result)
    }

    @Test
    fun `Test getCollectionById`() {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val flowId = FlowId("01")
        val flowCollection = FlowCollection(flowId, emptyList())
        `when`(flowAccessApi.getCollectionById(flowId)).thenReturn(flowCollection)

        val result = flowAccessApi.getCollectionById(flowId)

        assertEquals(flowCollection, result)
    }

    @Test
    fun `Test sendTransaction`() {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val flowTransaction = FlowTransaction.of(TransactionOuterClass.Transaction.getDefaultInstance())
        val flowId = FlowId("01")
        `when`(flowAccessApi.sendTransaction(flowTransaction)).thenReturn(flowId)

        val result = flowAccessApi.sendTransaction(flowTransaction)

        assertEquals(flowId, result)
    }

    @Test
    fun `Test getTransactionById`() {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val flowId = FlowId("01")
        val flowTransaction = FlowTransaction.of(TransactionOuterClass.Transaction.getDefaultInstance())
        `when`(flowAccessApi.getTransactionById(flowId)).thenReturn(flowTransaction)

        val result = flowAccessApi.getTransactionById(flowId)

        assertEquals(flowTransaction, result)
    }

    @Test
    fun `Test getTransactionResultById`() {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val flowId = FlowId("01")
        val flowTransactionResult = FlowTransactionResult.of(Access.TransactionResultResponse.getDefaultInstance())
        `when`(flowAccessApi.getTransactionResultById(flowId)).thenReturn(flowTransactionResult)

        val result = flowAccessApi.getTransactionResultById(flowId)

        assertEquals(flowTransactionResult, result)
    }

    @Test
    fun `Test getAccountAtLatestBlock`() {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val flowAddress = FlowAddress("01")
        val flowAccount = FlowAccount.of(AccountOuterClass.Account.getDefaultInstance())
        `when`(flowAccessApi.getAccountAtLatestBlock(flowAddress)).thenReturn(flowAccount)

        val result = flowAccessApi.getAccountAtLatestBlock(flowAddress)

        assertEquals(flowAccount, result)
    }

    @Test
    fun `Test getAccountByBlockHeight`() {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val flowAddress = FlowAddress("01")
        val height = 123L
        val flowAccount = FlowAccount.of(AccountOuterClass.Account.getDefaultInstance())
        `when`(flowAccessApi.getAccountByBlockHeight(flowAddress, height)).thenReturn(flowAccount)

        val result = flowAccessApi.getAccountByBlockHeight(flowAddress, height)

        assertEquals(flowAccount, result)
    }

    @Test
    fun `Test executeScriptAtLatestBlock`() {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val script = FlowScript("script")
        val arguments = listOf(ByteString.copyFromUtf8("argument1"))
        val response = FlowScriptResponse("response".toByteArray())
        `when`(flowAccessApi.executeScriptAtLatestBlock(script, arguments)).thenReturn(response)

        val result = flowAccessApi.executeScriptAtLatestBlock(script, arguments)

        assertEquals(response, result)
    }

    @Test
    fun `Test executeScriptAtBlockId`() {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val script = FlowScript("script")
        val blockId = FlowId("01")
        val arguments = listOf(ByteString.copyFromUtf8("argument1"))
        val response = FlowScriptResponse("response".toByteArray())
        `when`(flowAccessApi.executeScriptAtBlockId(script, blockId, arguments)).thenReturn(response)

        val result = flowAccessApi.executeScriptAtBlockId(script, blockId, arguments)

        assertEquals(response, result)
    }

    @Test
    fun `Test executeScriptAtBlockHeight`() {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val script = FlowScript("script")
        val height = 123L
        val arguments = listOf(ByteString.copyFromUtf8("argument1"))
        val response = FlowScriptResponse("response".toByteArray())
        `when`(flowAccessApi.executeScriptAtBlockHeight(script, height, arguments)).thenReturn(response)

        val result = flowAccessApi.executeScriptAtBlockHeight(script, height, arguments)

        assertEquals(response, result)
    }

    @Test
    fun `Test getEventsForHeightRange`() {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val type = "eventType"
        val range = 100L..200L
        val eventResults = listOf(FlowEventResult.of(Access.EventsResponse.Result.getDefaultInstance()), FlowEventResult.of(Access.EventsResponse.Result.getDefaultInstance()))
        `when`(flowAccessApi.getEventsForHeightRange(type, range)).thenReturn(eventResults)

        val result = flowAccessApi.getEventsForHeightRange(type, range)

        assertEquals(eventResults, result)
    }

    @Test
    fun `Test getEventsForBlockIds`() {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val type = "eventType"
        val ids = setOf(FlowId("01"), FlowId("02"))
        val eventResults = listOf(FlowEventResult.of(Access.EventsResponse.Result.getDefaultInstance()), FlowEventResult.of(Access.EventsResponse.Result.getDefaultInstance()))
        `when`(flowAccessApi.getEventsForBlockIds(type, ids)).thenReturn(eventResults)

        val result = flowAccessApi.getEventsForBlockIds(type, ids)

        assertEquals(eventResults, result)
    }

    @Test
    fun `Test getNetworkParameters`() {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val chainId = FlowChainId.TESTNET
        `when`(flowAccessApi.getNetworkParameters()).thenReturn(chainId)

        val result = flowAccessApi.getNetworkParameters()

        assertEquals(chainId, result)
    }

    @Test
    fun `Test getLatestProtocolStateSnapshot`() {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val snapshot = FlowSnapshot("snapshot".toByteArray())
        `when`(flowAccessApi.getLatestProtocolStateSnapshot()).thenReturn(snapshot)

        val result = flowAccessApi.getLatestProtocolStateSnapshot()

        assertEquals(snapshot, result)
    }
}
