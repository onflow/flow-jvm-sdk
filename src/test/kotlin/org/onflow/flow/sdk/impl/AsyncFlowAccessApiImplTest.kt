package org.onflow.flow.sdk.impl

import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import com.google.protobuf.ByteString
import org.onflow.flow.sdk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.onflow.protobuf.access.Access
import org.onflow.protobuf.access.AccessAPIGrpc
import org.mockito.ArgumentMatchers.any
import org.onflow.protobuf.entities.TransactionOuterClass
import java.math.BigDecimal
import java.time.LocalDateTime

class AsyncFlowAccessApiImplTest {
    private val api = mock(AccessAPIGrpc.AccessAPIFutureStub::class.java)
    private val asyncFlowAccessApi = AsyncFlowAccessApiImpl(api)

    @Test
    fun `test ping`() {
        val pingResponse = Access.PingResponse.newBuilder().build()
        val future: ListenableFuture<Access.PingResponse> = SettableFuture.create()
        (future as SettableFuture<Access.PingResponse>).set(pingResponse)

        `when`(api.ping(any())).thenReturn(future)

        assertEquals(Unit, asyncFlowAccessApi.ping().get())
    }

    @Test
    fun `test getLatestBlockHeader`() {
        val mockBlockHeader = FlowBlockHeader(FlowId("01"), FlowId("01"), 123L)

        val blockHeaderResponse = Access.BlockHeaderResponse.newBuilder().setBlock(mockBlockHeader.builder().build()).build()

        val future: ListenableFuture<Access.BlockHeaderResponse> = SettableFuture.create()
        (future as SettableFuture<Access.BlockHeaderResponse>).set(blockHeaderResponse)

        `when`(api.getLatestBlockHeader(any())).thenReturn(future)

        val result = asyncFlowAccessApi.getLatestBlockHeader(true).get()

        assertEquals(mockBlockHeader, result)
    }

    @Test
    fun `test getBlockHeaderById`() {
        val blockId = FlowId("01")
        val mockBlockHeader = FlowBlockHeader(blockId, FlowId("01"), 123L)

        val blockHeaderResponse = Access.BlockHeaderResponse.newBuilder().setBlock(mockBlockHeader.builder().build()).build()

        val future: ListenableFuture<Access.BlockHeaderResponse> = SettableFuture.create()
        (future as SettableFuture<Access.BlockHeaderResponse>).set(blockHeaderResponse)

        `when`(api.getBlockHeaderByID(any())).thenReturn(future)

        val result = asyncFlowAccessApi.getBlockHeaderById(blockId).get()

        assertEquals(mockBlockHeader, result)
    }

    @Test
    fun `test getBlockHeaderByHeight`() {
        val height = 123L
        val mockBlockHeader = FlowBlockHeader(FlowId("01"), FlowId("01"), height)

        val blockHeaderResponse = Access.BlockHeaderResponse.newBuilder().setBlock(mockBlockHeader.builder().build()).build()

        val future: ListenableFuture<Access.BlockHeaderResponse> = SettableFuture.create()
        (future as SettableFuture<Access.BlockHeaderResponse>).set(blockHeaderResponse)

        `when`(api.getBlockHeaderByHeight(any())).thenReturn(future)

        val result = asyncFlowAccessApi.getBlockHeaderByHeight(height).get()

        assertEquals(mockBlockHeader, result)
    }

    @Test
    fun `test getLatestBlock`() {
        val mockBlock = FlowBlock(FlowId("01"), FlowId("01"), 123L, LocalDateTime.now(), emptyList(), emptyList(), emptyList())

        val blockResponse = Access.BlockResponse.newBuilder().setBlock(mockBlock.builder().build()).build()

        val future: ListenableFuture<Access.BlockResponse> = SettableFuture.create()
        (future as SettableFuture<Access.BlockResponse>).set(blockResponse)

        `when`(api.getLatestBlock(any())).thenReturn(future)

        val result = asyncFlowAccessApi.getLatestBlock(true).get()

        assertEquals(mockBlock, result)
    }

    @Test
    fun `test getBlockById`() {
        val blockId = FlowId("01")
        val mockBlock = FlowBlock(blockId, FlowId("01"), 123L, LocalDateTime.now(), emptyList(), emptyList(), emptyList())

        val blockResponse = Access.BlockResponse.newBuilder().setBlock(mockBlock.builder().build()).build()

        val future: ListenableFuture<Access.BlockResponse> = SettableFuture.create()
        (future as SettableFuture<Access.BlockResponse>).set(blockResponse)

        `when`(api.getBlockByID(any())).thenReturn(future)

        val result = asyncFlowAccessApi.getBlockById(blockId).get()

        assertEquals(mockBlock, result)
    }

    @Test
    fun `test getBlockByHeight`() {
        val height = 123L
        val mockBlock = FlowBlock(FlowId("01"), FlowId("01"), height, LocalDateTime.now(), emptyList(), emptyList(), emptyList())

        val blockResponse = Access.BlockResponse.newBuilder()
            .setBlock(mockBlock.builder().build())
            .build()

        val future: ListenableFuture<Access.BlockResponse> = SettableFuture.create()
        (future as SettableFuture<Access.BlockResponse>).set(blockResponse)

        `when`(api.getBlockByHeight(any())).thenReturn(future)

        val result = asyncFlowAccessApi.getBlockByHeight(10).get()

        assertEquals(mockBlock, result)
    }

    @Test
    fun `test getCollectionById`() {
        val collectionId = FlowId("01")
        val mockCollection = FlowCollection(collectionId, emptyList())

        val collectionResponse = Access.CollectionResponse.newBuilder()
            .setCollection(mockCollection.builder().build())
            .build()

        val future: ListenableFuture<Access.CollectionResponse> = SettableFuture.create()
        (future as SettableFuture<Access.CollectionResponse>).set(collectionResponse)

        `when`(api.getCollectionByID(any())).thenReturn(future)

        val result = asyncFlowAccessApi.getCollectionById(collectionId).get()

        assertEquals(mockCollection, result)
    }

    @Test
    fun `test sendTransaction`() {
        val mockTransaction = FlowTransaction(
            FlowScript("script"),
            emptyList(),
            FlowId.of("01".toByteArray()),
            123L,
            FlowTransactionProposalKey(FlowAddress("02"), 1, 123L),
            FlowAddress("02"),
            emptyList()
        )

        val transactionResponse = Access.SendTransactionResponse.newBuilder()
            .setId(ByteString.copyFromUtf8("01"))
            .build()

        val future: ListenableFuture<Access.SendTransactionResponse> = SettableFuture.create()
        (future as SettableFuture<Access.SendTransactionResponse>).set(transactionResponse)

        `when`(api.sendTransaction(any())).thenReturn(future)

        val result = asyncFlowAccessApi.sendTransaction(mockTransaction).get()

        assertEquals(FlowId.of("01".toByteArray()), result)
    }

    @Test
    fun `test getTransactionById`() {
        val flowId = FlowId("01")

        val flowTransaction = FlowTransaction(
            FlowScript("script"),
            emptyList(),
            flowId,
            123L,
            FlowTransactionProposalKey(FlowAddress("02"), 1, 123L),
            FlowAddress("02"),
            emptyList()
        )
        val transactionResponse = Access.TransactionResponse.newBuilder()
            .setTransaction(flowTransaction.builder().build())
            .build()

        val future: ListenableFuture<Access.TransactionResponse> = SettableFuture.create()
        (future as SettableFuture<Access.TransactionResponse>).set(transactionResponse)

        `when`(api.getTransaction(any())).thenReturn(future)

        val result = asyncFlowAccessApi.getTransactionById(flowId).get()

        assertEquals(flowTransaction, result)
    }

    @Test
    fun `test getTransactionResultById`() {
        val flowId = FlowId.of("id".toByteArray())
        val flowTransactionResult = FlowTransactionResult(FlowTransactionStatus.SEALED, 1, "message", emptyList())

        val transactionResultResponse = Access.TransactionResultResponse.newBuilder()
            .setStatus(TransactionOuterClass.TransactionStatus.SEALED)
            .setStatusCode(1)
            .setErrorMessage("message")
            .setBlockId(ByteString.copyFromUtf8("id"))
            .build()

        val future: ListenableFuture<Access.TransactionResultResponse> = SettableFuture.create()
        (future as SettableFuture<Access.TransactionResultResponse>).set(transactionResultResponse)

        `when`(api.getTransactionResult(any())).thenReturn(future)

        val result = asyncFlowAccessApi.getTransactionResultById(flowId).get()

        assertEquals(flowTransactionResult, result)
    }

    @Test
    fun `test getAccountByAddress`() {
        val flowAddress = FlowAddress("01")
        val flowAccount = FlowAccount(flowAddress, BigDecimal.ONE, FlowCode("code".toByteArray()), emptyList(), emptyMap())

        val accountResponse = Access.GetAccountResponse.newBuilder()
            .setAccount(flowAccount.builder().build())
            .build()

        val future: ListenableFuture<Access.GetAccountResponse> = SettableFuture.create()
        (future as SettableFuture<Access.GetAccountResponse>).set(accountResponse)

        `when`(api.getAccount(any())).thenReturn(future)

        val result = asyncFlowAccessApi.getAccountByAddress(flowAddress).get()

        assertEquals(flowAccount.address, result?.address)
        assertEquals(flowAccount.balance.stripTrailingZeros(), result?.balance?.stripTrailingZeros())
        assertEquals(flowAccount.keys, result?.keys)
        assertEquals(flowAccount.contracts, result?.contracts)
    }

    @Test
    fun `test getAccountAtLatestBlock`() {
        val flowAddress = FlowAddress("01")
        val flowAccount = FlowAccount(flowAddress, BigDecimal.ONE, FlowCode("code".toByteArray()), emptyList(), emptyMap())

        val accountResponse = Access.AccountResponse.newBuilder()
            .setAccount(flowAccount.builder().build())
            .build()

        val future: ListenableFuture<Access.AccountResponse> = SettableFuture.create()
        (future as SettableFuture<Access.AccountResponse>).set(accountResponse)

        `when`(api.getAccountAtLatestBlock(any())).thenReturn(future)

        val result = asyncFlowAccessApi.getAccountAtLatestBlock(flowAddress).get()

        assertEquals(flowAccount.address, result?.address)
        assertEquals(flowAccount.balance.stripTrailingZeros(), result?.balance?.stripTrailingZeros())
        assertEquals(flowAccount.keys, result?.keys)
        assertEquals(flowAccount.contracts, result?.contracts)
    }

    @Test
    fun `test getAccountByBlockHeight`() {
        val flowAddress = FlowAddress("01")
        val flowAccount = FlowAccount(flowAddress, BigDecimal.ONE, FlowCode("code".toByteArray()), emptyList(), emptyMap())

        val height = 123L

        val accountResponse = Access.AccountResponse.newBuilder()
            .setAccount(flowAccount.builder().build())
            .build()

        val future: ListenableFuture<Access.AccountResponse> = SettableFuture.create()
        (future as SettableFuture<Access.AccountResponse>).set(accountResponse)

        `when`(api.getAccountAtBlockHeight(any())).thenReturn(future)

        val result = asyncFlowAccessApi.getAccountByBlockHeight(flowAddress, height).get()

        assertEquals(flowAccount.address, result?.address)
        assertEquals(flowAccount.balance.stripTrailingZeros(), result?.balance?.stripTrailingZeros())
        assertEquals(flowAccount.keys, result?.keys)
        assertEquals(flowAccount.contracts, result?.contracts)
    }

    @Test
    fun `test executeScriptAtLatestBlock`() {
        val script = FlowScript("script".toByteArray())
        val arguments = listOf(ByteString.copyFromUtf8("argument1"), ByteString.copyFromUtf8("argument2"))

        val scriptResponse = Access.ExecuteScriptResponse.newBuilder()
            .setValue(ByteString.copyFromUtf8("response_value"))
            .build()

        val future: ListenableFuture<Access.ExecuteScriptResponse> = SettableFuture.create()
        (future as SettableFuture<Access.ExecuteScriptResponse>).set(scriptResponse)

        `when`(api.executeScriptAtLatestBlock(any())).thenReturn(future)

        val result = asyncFlowAccessApi.executeScriptAtLatestBlock(script, arguments).get()

        assertEquals("response_value", result.stringValue)
    }

    @Test
    fun `test executeScriptAtBlockId`() {
        val script = FlowScript("some_script")
        val blockId = FlowId("01")
        val arguments = listOf(ByteString.copyFromUtf8("argument1"), ByteString.copyFromUtf8("argument2"))

        val scriptResponse = Access.ExecuteScriptResponse.newBuilder()
            .setValue(ByteString.copyFromUtf8("response_value"))
            .build()
        val future: ListenableFuture<Access.ExecuteScriptResponse> = SettableFuture.create()
        (future as SettableFuture<Access.ExecuteScriptResponse>).set(scriptResponse)

        `when`(api.executeScriptAtBlockID(any())).thenReturn(future)

        val result = asyncFlowAccessApi.executeScriptAtBlockId(script, blockId, arguments).get()

        assertEquals("response_value", result.stringValue)
    }

    @Test
    fun `test executeScriptAtBlockHeight`() {
        val script = FlowScript("some_script")
        val height = 123L
        val arguments = listOf(ByteString.copyFromUtf8("argument1"), ByteString.copyFromUtf8("argument2"))

        val scriptResponse = Access.ExecuteScriptResponse.newBuilder()
            .setValue(ByteString.copyFromUtf8("response_value"))
            .build()

        val future: ListenableFuture<Access.ExecuteScriptResponse> = SettableFuture.create()
        (future as SettableFuture<Access.ExecuteScriptResponse>).set(scriptResponse)

        `when`(api.executeScriptAtBlockHeight(any())).thenReturn(future)

        val result = asyncFlowAccessApi.executeScriptAtBlockHeight(script, height, arguments).get()

        assertEquals("response_value", result.stringValue)
    }

    @Test
    fun `test getEventsForHeightRange`() {
        val type = "event_type"
        val range = 1L..10L

        val eventResult1 = Access.EventsResponse.Result.newBuilder().build()
        val eventResult2 = Access.EventsResponse.Result.newBuilder().build()
        val response = Access.EventsResponse.newBuilder()
            .addResults(eventResult1)
            .addResults(eventResult2)
            .build()

        val future: ListenableFuture<Access.EventsResponse> = SettableFuture.create()
        (future as SettableFuture<Access.EventsResponse>).set(response)

        `when`(api.getEventsForHeightRange(any())).thenReturn(future)

        val result = asyncFlowAccessApi.getEventsForHeightRange(type, range).get()

        assertEquals(2, result.size)
    }

    @Test
    fun `test getEventsForBlockIds`() {
        val type = "event_type"
        val blockIds = setOf(FlowId("01"), FlowId("02"))

        val eventResult1 = Access.EventsResponse.Result.newBuilder().build()
        val eventResult2 = Access.EventsResponse.Result.newBuilder().build()
        val response = Access.EventsResponse.newBuilder()
            .addResults(eventResult1)
            .addResults(eventResult2)
            .build()

        val future: ListenableFuture<Access.EventsResponse> = SettableFuture.create()
        (future as SettableFuture<Access.EventsResponse>).set(response)

        `when`(api.getEventsForBlockIDs(any())).thenReturn(future)

        val result = asyncFlowAccessApi.getEventsForBlockIds(type, blockIds).get()

        assertEquals(2, result.size)
    }

    @Test
    fun `test getNetworkParameters`() {
        val mockFlowChainId = FlowChainId.of("test_chain_id")

        val networkParametersResponse = Access.GetNetworkParametersResponse.newBuilder()
            .setChainId("test_chain_id")
            .build()

        val future: ListenableFuture<Access.GetNetworkParametersResponse> = SettableFuture.create()
        (future as SettableFuture<Access.GetNetworkParametersResponse>).set(networkParametersResponse)

        `when`(api.getNetworkParameters(any())).thenReturn(future)

        val result = asyncFlowAccessApi.getNetworkParameters().get()

        assertEquals(mockFlowChainId, result)
    }

    @Test
    fun `test getLatestProtocolStateSnapshot`() {
        val mockFlowSnapshot = FlowSnapshot("test_serialized_snapshot".toByteArray())

        val protocolStateSnapshotResponse = Access.ProtocolStateSnapshotResponse.newBuilder()
            .setSerializedSnapshot(ByteString.copyFromUtf8("test_serialized_snapshot"))
            .build()

        val future: ListenableFuture<Access.ProtocolStateSnapshotResponse> = SettableFuture.create()
        (future as SettableFuture<Access.ProtocolStateSnapshotResponse>).set(protocolStateSnapshotResponse)

        `when`(api.getLatestProtocolStateSnapshot(any())).thenReturn(future)

        val result = asyncFlowAccessApi.getLatestProtocolStateSnapshot().get()

        assertEquals(mockFlowSnapshot, result)
    }
}
