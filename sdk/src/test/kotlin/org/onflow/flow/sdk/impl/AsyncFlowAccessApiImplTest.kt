package org.onflow.flow.sdk.impl

import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import com.google.protobuf.ByteString
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.onflow.flow.sdk.*
import org.onflow.protobuf.access.Access
import org.onflow.protobuf.access.AccessAPIGrpc
import org.onflow.protobuf.entities.AccountOuterClass
import org.onflow.protobuf.entities.ExecutionResultOuterClass
import org.onflow.protobuf.entities.NodeVersionInfoOuterClass
import org.onflow.protobuf.entities.TransactionOuterClass
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class AsyncFlowAccessApiImplTest {
    private val api = mock(AccessAPIGrpc.AccessAPIFutureStub::class.java)
    private val asyncFlowAccessApi = AsyncFlowAccessApiImpl(api)

    companion object {
        val BLOCK_ID_BYTES = byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1)
        val PARENT_ID_BYTES = byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2)

        const val HEIGHT = 123L

        val mockBlockHeader = FlowBlockHeader(
            id = FlowId.of(BLOCK_ID_BYTES),
            parentId = FlowId.of(PARENT_ID_BYTES),
            height = 123L,
            timestamp = LocalDateTime.of(2024, 10, 15, 18, 33, 12),
            payloadHash = ByteArray(32) { 0 },
            view = 1L,
            parentVoterSigData = ByteArray(32) { 0 },
            proposerId = FlowId.of(PARENT_ID_BYTES),
            proposerSigData = ByteArray(32) { 0 },
            chainId = FlowChainId.MAINNET,
            parentVoterIndices = ByteArray(32) { 0 },
            lastViewTc = FlowTimeoutCertificate(
                view = 1L,
                highQcViews = emptyList(),
                highestQc = FlowQuorumCertificate(
                    view = 1L,
                    blockId = FlowId.of(BLOCK_ID_BYTES),
                    signerIndices = ByteArray(32) { 0 },
                    sigData = ByteArray(32) { 0 }
                ),
                signerIndices = ByteArray(32) { 0 },
                sigData = ByteArray(32) { 0 }
            ),
            parentView = 1L
        )
    }

    private fun <T> setupFutureMock(response: T): ListenableFuture<T> {
        val future: ListenableFuture<T> = SettableFuture.create()
        (future as SettableFuture<T>).set(response)
        return future
    }

    @Test
    fun `test ping`() {
        val pingResponse = Access.PingResponse.newBuilder().build()
        `when`(api.ping(any())).thenReturn(setupFutureMock(pingResponse))

        val result = asyncFlowAccessApi.ping().get()
        assert(result is FlowAccessApi.AccessApiCallResponse.Success)
    }

    @Test
    fun `test getAccountKeyAtLatestBlock success`() {
        val flowAddress = FlowAddress("01")
        val keyIndex = 0
        val mockAccountKey = FlowAccountKey.of(AccountOuterClass.AccountKey.getDefaultInstance())
        val response = Access.AccountKeyResponse.newBuilder().setAccountKey(mockAccountKey.builder().build()).build()

        `when`(api.getAccountKeyAtLatestBlock(any())).thenReturn(setupFutureMock(response))

        val result = asyncFlowAccessApi.getAccountKeyAtLatestBlock(flowAddress, keyIndex).get()
        assert(result is FlowAccessApi.AccessApiCallResponse.Success)
        result as FlowAccessApi.AccessApiCallResponse.Success
        assertEquals(mockAccountKey, result.data)
    }

    @Test
    fun `test getAccountKeyAtLatestBlock failure`() {
        val flowAddress = FlowAddress("01")
        val keyIndex = 0
        val exception = RuntimeException("Test exception")

        `when`(api.getAccountKeyAtLatestBlock(any())).thenThrow(exception)

        val result = asyncFlowAccessApi.getAccountKeyAtLatestBlock(flowAddress, keyIndex).get()
        assert(result is FlowAccessApi.AccessApiCallResponse.Error)
        result as FlowAccessApi.AccessApiCallResponse.Error
        assertEquals("Failed to get account key at latest block", result.message)
        assertEquals(exception, result.throwable)
    }

    @Test
    fun `test getAccountKeyAtBlockHeight success`() {
        val flowAddress = FlowAddress("01")
        val keyIndex = 0
        val blockHeight = HEIGHT
        val mockAccountKey = FlowAccountKey.of(AccountOuterClass.AccountKey.getDefaultInstance())
        val response = Access.AccountKeyResponse.newBuilder().setAccountKey(mockAccountKey.builder().build()).build()

        `when`(api.getAccountKeyAtBlockHeight(any())).thenReturn(setupFutureMock(response))

        val result = asyncFlowAccessApi.getAccountKeyAtBlockHeight(flowAddress, keyIndex, blockHeight).get()
        assert(result is FlowAccessApi.AccessApiCallResponse.Success)
        result as FlowAccessApi.AccessApiCallResponse.Success
        assertEquals(mockAccountKey, result.data)
    }

    @Test
    fun `test getAccountKeyAtBlockHeight failure`() {
        val flowAddress = FlowAddress("01")
        val keyIndex = 0
        val blockHeight = HEIGHT
        val exception = RuntimeException("Test exception")

        `when`(api.getAccountKeyAtBlockHeight(any())).thenThrow(exception)

        val result = asyncFlowAccessApi.getAccountKeyAtBlockHeight(flowAddress, keyIndex, blockHeight).get()
        assert(result is FlowAccessApi.AccessApiCallResponse.Error)
        result as FlowAccessApi.AccessApiCallResponse.Error
        assertEquals("Failed to get account key at block height", result.message)
        assertEquals(exception, result.throwable)
    }

    @Test
    fun `test getAccountKeysAtLatestBlock success`() {
        val flowAddress = FlowAddress("01")
        val mockAccountKeys = listOf(
            FlowAccountKey.of(AccountOuterClass.AccountKey.getDefaultInstance()),
            FlowAccountKey.of(AccountOuterClass.AccountKey.getDefaultInstance())
        )
        val response = Access.AccountKeysResponse.newBuilder()
            .addAllAccountKeys(mockAccountKeys.map { it.builder().build() })
            .build()

        `when`(api.getAccountKeysAtLatestBlock(any())).thenReturn(setupFutureMock(response))

        val result = asyncFlowAccessApi.getAccountKeysAtLatestBlock(flowAddress).get()
        assert(result is FlowAccessApi.AccessApiCallResponse.Success)
        result as FlowAccessApi.AccessApiCallResponse.Success
        assertEquals(mockAccountKeys, result.data)
    }

    @Test
    fun `test getAccountKeysAtLatestBlock failure`() {
        val flowAddress = FlowAddress("01")
        val exception = RuntimeException("Test exception")

        `when`(api.getAccountKeysAtLatestBlock(any())).thenThrow(exception)

        val result = asyncFlowAccessApi.getAccountKeysAtLatestBlock(flowAddress).get()
        assert(result is FlowAccessApi.AccessApiCallResponse.Error)
        result as FlowAccessApi.AccessApiCallResponse.Error
        assertEquals("Failed to get account keys at latest block", result.message)
        assertEquals(exception, result.throwable)
    }

    @Test
    fun `test getAccountKeysAtBlockHeight success`() {
        val flowAddress = FlowAddress("01")
        val blockHeight = HEIGHT
        val mockAccountKeys = listOf(
            FlowAccountKey.of(AccountOuterClass.AccountKey.getDefaultInstance()),
            FlowAccountKey.of(AccountOuterClass.AccountKey.getDefaultInstance())
        )
        val response = Access.AccountKeysResponse.newBuilder()
            .addAllAccountKeys(mockAccountKeys.map { it.builder().build() })
            .build()

        `when`(api.getAccountKeysAtBlockHeight(any())).thenReturn(setupFutureMock(response))

        val result = asyncFlowAccessApi.getAccountKeysAtBlockHeight(flowAddress, blockHeight).get()
        assert(result is FlowAccessApi.AccessApiCallResponse.Success)
        result as FlowAccessApi.AccessApiCallResponse.Success
        assertEquals(mockAccountKeys, result.data)
    }

    @Test
    fun `test getAccountKeysAtBlockHeight failure`() {
        val flowAddress = FlowAddress("01")
        val blockHeight = HEIGHT
        val exception = RuntimeException("Test exception")

        `when`(api.getAccountKeysAtBlockHeight(any())).thenThrow(exception)

        val result = asyncFlowAccessApi.getAccountKeysAtBlockHeight(flowAddress, blockHeight).get()
        assert(result is FlowAccessApi.AccessApiCallResponse.Error)
        result as FlowAccessApi.AccessApiCallResponse.Error
        assertEquals("Failed to get account keys at block height", result.message)
        assertEquals(exception, result.throwable)
    }

    @Test
    fun `test getLatestBlockHeader`() {
        val mockBlockHeader = mockBlockHeader
        val blockHeaderResponse = Access.BlockHeaderResponse
            .newBuilder()
            .setBlock(mockBlockHeader.builder().build())
            .build()
        `when`(api.getLatestBlockHeader(any())).thenReturn(setupFutureMock(blockHeaderResponse))

        val result = asyncFlowAccessApi.getLatestBlockHeader(true).get()
        assert(result is FlowAccessApi.AccessApiCallResponse.Success)
        result as FlowAccessApi.AccessApiCallResponse.Success
        assertEquals(mockBlockHeader, result.data)
    }

    @Test
    fun `test getBlockHeaderById`() {
        val blockId = FlowId.of(BLOCK_ID_BYTES)
        val mockBlockHeader = mockBlockHeader
        val blockHeaderResponse = Access.BlockHeaderResponse
            .newBuilder()
            .setBlock(mockBlockHeader.builder().build())
            .build()
        `when`(api.getBlockHeaderByID(any())).thenReturn(setupFutureMock(blockHeaderResponse))

        val result = asyncFlowAccessApi.getBlockHeaderById(blockId).get()
        assert(result is FlowAccessApi.AccessApiCallResponse.Success)
        result as FlowAccessApi.AccessApiCallResponse.Success
        assertEquals(mockBlockHeader, result.data)
    }

    @Test
    fun `test getBlockHeaderByHeight`() {
        val height = HEIGHT
        val mockBlockHeader = mockBlockHeader
        val blockHeaderResponse = Access.BlockHeaderResponse
            .newBuilder()
            .setBlock(mockBlockHeader.builder().build())
            .build()
        `when`(api.getBlockHeaderByHeight(any())).thenReturn(setupFutureMock(blockHeaderResponse))

        val result = asyncFlowAccessApi.getBlockHeaderByHeight(height).get()
        assert(result is FlowAccessApi.AccessApiCallResponse.Success)
        result as FlowAccessApi.AccessApiCallResponse.Success
        assertEquals(mockBlockHeader, result.data)
    }

    @Test
    fun `test getLatestBlock`() {
        val mockBlock = FlowBlock(
            id = FlowId.of(BLOCK_ID_BYTES),
            parentId = FlowId.of(PARENT_ID_BYTES),
            height = 123L,
            timestamp = LocalDateTime.now(),
            collectionGuarantees = emptyList(),
            blockSeals = emptyList(),
            signatures = emptyList(),
            executionReceiptMetaList = emptyList(),
            executionResultList = emptyList(),
            blockHeader = mockBlockHeader,
            protocolStateId = FlowId.of(ByteArray(32))
        )
        val blockResponse = Access.BlockResponse
            .newBuilder()
            .setBlock(mockBlock.builder().build())
            .build()
        `when`(api.getLatestBlock(any())).thenReturn(setupFutureMock(blockResponse))

        val result = asyncFlowAccessApi.getLatestBlock(true).get()
        assert(result is FlowAccessApi.AccessApiCallResponse.Success)
        result as FlowAccessApi.AccessApiCallResponse.Success
        assertEquals(mockBlock, result.data)
    }

    @Test
    fun `test getBlockById`() {
        val blockId = FlowId.of(BLOCK_ID_BYTES)
        val mockBlock = FlowBlock(
            id = blockId,
            parentId = FlowId.of(PARENT_ID_BYTES),
            height = 123L,
            timestamp = LocalDateTime.now(),
            collectionGuarantees = emptyList(),
            blockSeals = emptyList(),
            signatures = emptyList(),
            executionReceiptMetaList = emptyList(),
            executionResultList = emptyList(),
            blockHeader = mockBlockHeader,
            protocolStateId = FlowId.of(ByteArray(32))
        )
        val blockResponse = Access.BlockResponse
            .newBuilder()
            .setBlock(mockBlock.builder().build())
            .build()
        `when`(api.getBlockByID(any())).thenReturn(setupFutureMock(blockResponse))

        val result = asyncFlowAccessApi.getBlockById(blockId).get()
        assert(result is FlowAccessApi.AccessApiCallResponse.Success)
        result as FlowAccessApi.AccessApiCallResponse.Success
        assertEquals(mockBlock, result.data)
    }

    @Test
    fun `test getBlockByHeight`() {
        val height = HEIGHT
        val mockBlock = FlowBlock(
            FlowId("01"),
            FlowId("01"),
            height,
            LocalDateTime.now(),
            emptyList(),
            emptyList(),
            emptyList(),
            emptyList(),
            emptyList(),
            mockBlockHeader,
            FlowId.of(ByteArray(32))
        )
        val blockResponse = Access.BlockResponse
            .newBuilder()
            .setBlock(mockBlock.builder().build())
            .build()
        `when`(api.getBlockByHeight(any())).thenReturn(setupFutureMock(blockResponse))

        val result = asyncFlowAccessApi.getBlockByHeight(height).get()
        assert(result is FlowAccessApi.AccessApiCallResponse.Success)
        result as FlowAccessApi.AccessApiCallResponse.Success
        assertEquals(mockBlock, result.data)
    }

    @Test
    fun `test getAccountBalanceAtLatestBlock success`() {
        val flowAddress = FlowAddress("01")
        val expectedBalance = 1000L
        val balanceResponse = Access.AccountBalanceResponse
            .newBuilder()
            .setBalance(expectedBalance)
            .build()

        `when`(api.getAccountBalanceAtLatestBlock(any())).thenReturn(setupFutureMock(balanceResponse))

        val result = asyncFlowAccessApi.getAccountBalanceAtLatestBlock(flowAddress).get()
        assert(result is FlowAccessApi.AccessApiCallResponse.Success)
        result as FlowAccessApi.AccessApiCallResponse.Success
        assertEquals(expectedBalance, result.data)
    }

    @Test
    fun `test getAccountBalanceAtLatestBlock failure`() {
        val flowAddress = FlowAddress("01")
        val exception = RuntimeException("Test exception")

        `when`(api.getAccountBalanceAtLatestBlock(any())).thenThrow(exception)

        val result = asyncFlowAccessApi.getAccountBalanceAtLatestBlock(flowAddress).get()
        assert(result is FlowAccessApi.AccessApiCallResponse.Error)
        result as FlowAccessApi.AccessApiCallResponse.Error
        assertEquals("Failed to get account balance at latest block", result.message)
        assertEquals(exception, result.throwable)
    }

    @Test
    fun `test getAccountBalanceAtBlockHeight success`() {
        val flowAddress = FlowAddress("01")
        val blockHeight = 123L
        val expectedBalance = 1000L
        val balanceResponse = Access.AccountBalanceResponse
            .newBuilder()
            .setBalance(expectedBalance)
            .build()

        `when`(api.getAccountBalanceAtBlockHeight(any())).thenReturn(setupFutureMock(balanceResponse))

        val result = asyncFlowAccessApi.getAccountBalanceAtBlockHeight(flowAddress, blockHeight).get()
        assert(result is FlowAccessApi.AccessApiCallResponse.Success)
        result as FlowAccessApi.AccessApiCallResponse.Success
        assertEquals(expectedBalance, result.data)
    }

    @Test
    fun `test getAccountBalanceAtBlockHeight failure`() {
        val flowAddress = FlowAddress("01")
        val blockHeight = 123L
        val exception = RuntimeException("Test exception")

        `when`(api.getAccountBalanceAtBlockHeight(any())).thenThrow(exception)

        val result = asyncFlowAccessApi.getAccountBalanceAtBlockHeight(flowAddress, blockHeight).get()
        assert(result is FlowAccessApi.AccessApiCallResponse.Error)
        result as FlowAccessApi.AccessApiCallResponse.Error
        assertEquals("Failed to get account balance at block height", result.message)
        assertEquals(exception, result.throwable)
    }

    @Test
    fun `test getCollectionById`() {
        val collectionId = FlowId("01")
        val mockCollection = FlowCollection(collectionId, emptyList())
        val collectionResponse = Access.CollectionResponse
            .newBuilder()
            .setCollection(mockCollection.builder().build())
            .build()
        `when`(api.getCollectionByID(any())).thenReturn(setupFutureMock(collectionResponse))

        val result = asyncFlowAccessApi.getCollectionById(collectionId).get()
        assert(result is FlowAccessApi.AccessApiCallResponse.Success)
        result as FlowAccessApi.AccessApiCallResponse.Success
        assertEquals(mockCollection, result.data)
    }

    @Test
    fun `test sendTransaction`() {
        val mockTransaction = FlowTransaction(FlowScript("script"), emptyList(), FlowId.of("01".toByteArray()), 123L, FlowTransactionProposalKey(FlowAddress("02"), 1, 123L), FlowAddress("02"), emptyList())

        val transactionResponse = Access.SendTransactionResponse
            .newBuilder()
            .setId(ByteString.copyFromUtf8("01"))
            .build()
        `when`(api.sendTransaction(any())).thenReturn(setupFutureMock(transactionResponse))

        val result = asyncFlowAccessApi.sendTransaction(mockTransaction).get()
        assert(result is FlowAccessApi.AccessApiCallResponse.Success)
        result as FlowAccessApi.AccessApiCallResponse.Success
        assertEquals(FlowId.of("01".toByteArray()), result.data)
    }

    @Test
    fun `test getTransactionById`() {
        val flowId = FlowId("01")
        val flowTransaction = FlowTransaction(FlowScript("script"), emptyList(), flowId, 123L, FlowTransactionProposalKey(FlowAddress("02"), 1, 123L), FlowAddress("02"), emptyList())
        val transactionResponse = Access.TransactionResponse
            .newBuilder()
            .setTransaction(flowTransaction.builder().build())
            .build()
        `when`(api.getTransaction(any())).thenReturn(setupFutureMock(transactionResponse))

        val result = asyncFlowAccessApi.getTransactionById(flowId).get()
        assert(result is FlowAccessApi.AccessApiCallResponse.Success)
        result as FlowAccessApi.AccessApiCallResponse.Success
        assertEquals(flowTransaction, result.data)
    }

    @Test
    fun `test getTransactionResultById`() {
        val flowId = FlowId.of("id".toByteArray())
        val flowTransactionResult = FlowTransactionResult(FlowTransactionStatus.SEALED, 1, "message", emptyList(), flowId, 1L, flowId, flowId, 1L)

        val transactionResultResponse = Access.TransactionResultResponse
            .newBuilder()
            .setStatus(TransactionOuterClass.TransactionStatus.SEALED)
            .setStatusCode(1)
            .setErrorMessage("message")
            .setBlockId(ByteString.copyFromUtf8("id"))
            .setBlockHeight(1L)
            .setTransactionId(ByteString.copyFromUtf8("id"))
            .setCollectionId(ByteString.copyFromUtf8("id"))
            .setComputationUsage(1L)
            .build()

        `when`(api.getTransactionResult(any())).thenReturn(setupFutureMock(transactionResultResponse))

        val result = asyncFlowAccessApi.getTransactionResultById(flowId).get()
        assert(result is FlowAccessApi.AccessApiCallResponse.Success)
        result as FlowAccessApi.AccessApiCallResponse.Success
        assertEquals(flowTransactionResult, result.data)
    }

    @Test
    fun `test getAccountByAddress`() {
        val flowAddress = FlowAddress("01")
        val flowAccount = FlowAccount(flowAddress, BigDecimal.ONE, FlowCode("code".toByteArray()), emptyList(), emptyMap())
        val accountResponse = Access.GetAccountResponse
            .newBuilder()
            .setAccount(flowAccount.builder().build())
            .build()
        `when`(api.getAccount(any())).thenReturn(setupFutureMock(accountResponse))

        val result = asyncFlowAccessApi.getAccountByAddress(flowAddress).get()
        assert(result is FlowAccessApi.AccessApiCallResponse.Success)
        result as FlowAccessApi.AccessApiCallResponse.Success
        assertEquals(flowAccount.address, result.data.address)
        assertEquals(flowAccount.balance.stripTrailingZeros(), result.data.balance.stripTrailingZeros())
        assertEquals(flowAccount.keys, result.data.keys)
        assertEquals(flowAccount.contracts, result.data.contracts)
    }

    @Test
    fun `test getAccountAtLatestBlock`() {
        val flowAddress = FlowAddress("01")
        val flowAccount = FlowAccount(flowAddress, BigDecimal.ONE, FlowCode("code".toByteArray()), emptyList(), emptyMap())
        val accountResponse = Access.AccountResponse
            .newBuilder()
            .setAccount(flowAccount.builder().build())
            .build()
        `when`(api.getAccountAtLatestBlock(any())).thenReturn(setupFutureMock(accountResponse))

        val result = asyncFlowAccessApi.getAccountAtLatestBlock(flowAddress).get()
        assert(result is FlowAccessApi.AccessApiCallResponse.Success)
        result as FlowAccessApi.AccessApiCallResponse.Success
        assertEquals(flowAccount.address, result.data.address)
        assertEquals(flowAccount.balance.stripTrailingZeros(), result.data.balance.stripTrailingZeros())
        assertEquals(flowAccount.keys, result.data.keys)
        assertEquals(flowAccount.contracts, result.data.contracts)
    }

    @Test
    fun `test getAccountByBlockHeight`() {
        val flowAddress = FlowAddress("01")
        val flowAccount = FlowAccount(flowAddress, BigDecimal.ONE, FlowCode("code".toByteArray()), emptyList(), emptyMap())
        val height = 123L
        val accountResponse = Access.AccountResponse
            .newBuilder()
            .setAccount(flowAccount.builder().build())
            .build()
        `when`(api.getAccountAtBlockHeight(any())).thenReturn(setupFutureMock(accountResponse))

        val result = asyncFlowAccessApi.getAccountByBlockHeight(flowAddress, height).get()
        assert(result is FlowAccessApi.AccessApiCallResponse.Success)
        result as FlowAccessApi.AccessApiCallResponse.Success
        assertEquals(flowAccount.address, result.data.address)
        assertEquals(flowAccount.balance.stripTrailingZeros(), result.data.balance.stripTrailingZeros())
        assertEquals(flowAccount.keys, result.data.keys)
        assertEquals(flowAccount.contracts, result.data.contracts)
    }

    @Test
    fun `test executeScriptAtLatestBlock`() {
        val script = FlowScript("script".toByteArray())
        val arguments = listOf(ByteString.copyFromUtf8("argument1"), ByteString.copyFromUtf8("argument2"))
        val scriptResponse = Access.ExecuteScriptResponse
            .newBuilder()
            .setValue(ByteString.copyFromUtf8("response_value"))
            .build()
        `when`(api.executeScriptAtLatestBlock(any())).thenReturn(setupFutureMock(scriptResponse))

        val result = asyncFlowAccessApi.executeScriptAtLatestBlock(script, arguments).get()
        assert(result is FlowAccessApi.AccessApiCallResponse.Success)
        result as FlowAccessApi.AccessApiCallResponse.Success
        assertEquals("response_value", result.data.stringValue)
    }

    @Test
    fun `test executeScriptAtBlockId`() {
        val script = FlowScript("some_script")
        val blockId = FlowId("01")
        val arguments = listOf(ByteString.copyFromUtf8("argument1"), ByteString.copyFromUtf8("argument2"))
        val scriptResponse = Access.ExecuteScriptResponse
            .newBuilder()
            .setValue(ByteString.copyFromUtf8("response_value"))
            .build()
        `when`(api.executeScriptAtBlockID(any())).thenReturn(setupFutureMock(scriptResponse))

        val result = asyncFlowAccessApi.executeScriptAtBlockId(script, blockId, arguments).get()
        assert(result is FlowAccessApi.AccessApiCallResponse.Success)
        result as FlowAccessApi.AccessApiCallResponse.Success
        assertEquals("response_value", result.data.stringValue)
    }

    @Test
    fun `test executeScriptAtBlockHeight`() {
        val script = FlowScript("some_script")
        val height = 123L
        val arguments = listOf(ByteString.copyFromUtf8("argument1"), ByteString.copyFromUtf8("argument2"))
        val scriptResponse = Access.ExecuteScriptResponse
            .newBuilder()
            .setValue(ByteString.copyFromUtf8("response_value"))
            .build()
        `when`(api.executeScriptAtBlockHeight(any())).thenReturn(setupFutureMock(scriptResponse))

        val result = asyncFlowAccessApi.executeScriptAtBlockHeight(script, height, arguments).get()
        assert(result is FlowAccessApi.AccessApiCallResponse.Success)
        result as FlowAccessApi.AccessApiCallResponse.Success
        assertEquals("response_value", result.data.stringValue)
    }

    @Test
    fun `test getEventsForHeightRange`() {
        val type = "event_type"
        val range = 1L..10L
        val eventResult1 = Access.EventsResponse.Result
            .newBuilder()
            .build()
        val eventResult2 = Access.EventsResponse.Result
            .newBuilder()
            .build()
        val response = Access.EventsResponse
            .newBuilder()
            .addResults(eventResult1)
            .addResults(eventResult2)
            .build()
        `when`(api.getEventsForHeightRange(any())).thenReturn(setupFutureMock(response))

        val result = asyncFlowAccessApi.getEventsForHeightRange(type, range).get()
        assert(result is FlowAccessApi.AccessApiCallResponse.Success)
        result as FlowAccessApi.AccessApiCallResponse.Success
        assertEquals(2, result.data.size)
    }

    @Test
    fun `test getEventsForBlockIds`() {
        val type = "event_type"
        val blockIds = setOf(FlowId("01"), FlowId("02"))
        val eventResult1 = Access.EventsResponse.Result
            .newBuilder()
            .build()
        val eventResult2 = Access.EventsResponse.Result
            .newBuilder()
            .build()
        val response = Access.EventsResponse
            .newBuilder()
            .addResults(eventResult1)
            .addResults(eventResult2)
            .build()
        `when`(api.getEventsForBlockIDs(any())).thenReturn(setupFutureMock(response))

        val result = asyncFlowAccessApi.getEventsForBlockIds(type, blockIds).get()
        assert(result is FlowAccessApi.AccessApiCallResponse.Success)
        result as FlowAccessApi.AccessApiCallResponse.Success
        assertEquals(2, result.data.size)
    }

    @Test
    fun `test getNetworkParameters`() {
        val mockFlowChainId = FlowChainId.of("test_chain_id")
        val networkParametersResponse = Access.GetNetworkParametersResponse
            .newBuilder()
            .setChainId("test_chain_id")
            .build()
        `when`(api.getNetworkParameters(any())).thenReturn(setupFutureMock(networkParametersResponse))

        val result = asyncFlowAccessApi.getNetworkParameters().get()
        assert(result is FlowAccessApi.AccessApiCallResponse.Success)
        result as FlowAccessApi.AccessApiCallResponse.Success
        assertEquals(mockFlowChainId, result.data)
    }

    @Test
    fun `test getLatestProtocolStateSnapshot`() {
        val mockFlowSnapshot = FlowSnapshot("test_serialized_snapshot".toByteArray())
        val protocolStateSnapshotResponse = Access.ProtocolStateSnapshotResponse
            .newBuilder()
            .setSerializedSnapshot(ByteString.copyFromUtf8("test_serialized_snapshot"))
            .build()
        `when`(api.getLatestProtocolStateSnapshot(any())).thenReturn(setupFutureMock(protocolStateSnapshotResponse))

        val result = asyncFlowAccessApi.getLatestProtocolStateSnapshot().get()
        assert(result is FlowAccessApi.AccessApiCallResponse.Success)
        result as FlowAccessApi.AccessApiCallResponse.Success
        assertEquals(mockFlowSnapshot, result.data)
    }

    @Test
    fun `test getNodeVersionInfo`() {
        val mockNodeVersionInfo = Access.GetNodeVersionInfoResponse
            .newBuilder()
            .setInfo(
                NodeVersionInfoOuterClass.NodeVersionInfo
                    .newBuilder()
                    .setSemver("v0.0.1")
                    .setCommit("123456")
                    .setSporkId(ByteString.copyFromUtf8("sporkId"))
                    .setProtocolVersion(5)
                    .setSporkRootBlockHeight(1000)
                    .setNodeRootBlockHeight(1001)
                    .setCompatibleRange(
                        NodeVersionInfoOuterClass.CompatibleRange
                            .newBuilder()
                            .setStartHeight(100)
                            .setEndHeight(200)
                            .build()
                    ).build()
            ).build()

        `when`(api.getNodeVersionInfo(any())).thenReturn(setupFutureMock(mockNodeVersionInfo))

        val result = asyncFlowAccessApi.getNodeVersionInfo().get()

        assert(result is FlowAccessApi.AccessApiCallResponse.Success)
        result as FlowAccessApi.AccessApiCallResponse.Success
        val nodeVersionInfo = result.data

        assertEquals("v0.0.1", nodeVersionInfo.semver)
        assertEquals("123456", nodeVersionInfo.commit)
        assertArrayEquals("sporkId".toByteArray(), nodeVersionInfo.sporkId)
        assertEquals(5, nodeVersionInfo.protocolVersion)
        assertEquals(1000L, nodeVersionInfo.sporkRootBlockHeight)
        assertEquals(1001L, nodeVersionInfo.nodeRootBlockHeight)
        assertEquals(100L, nodeVersionInfo.compatibleRange?.startHeight)
        assertEquals(200L, nodeVersionInfo.compatibleRange?.endHeight)
    }

    @Test
    fun `test getTransactionsByBlockId`() {
        val blockId = FlowId("01")
        val transactions = listOf(FlowTransaction.of(TransactionOuterClass.Transaction.getDefaultInstance()))
        val response = Access.TransactionsResponse
            .newBuilder()
            .addAllTransactions(transactions.map { it.builder().build() })
            .build()
        `when`(api.getTransactionsByBlockID(any())).thenReturn(setupFutureMock(response))

        val result = asyncFlowAccessApi.getTransactionsByBlockId(blockId).get()
        assert(result is FlowAccessApi.AccessApiCallResponse.Success)
        result as FlowAccessApi.AccessApiCallResponse.Success
        assertEquals(transactions, result.data)
    }

    @Test
    fun `test getTransactionsByBlockId with multiple results`() {
        val blockId = FlowId("01")
        val transaction1 = FlowTransaction.of(TransactionOuterClass.Transaction.getDefaultInstance())
        val transaction2 = FlowTransaction.of(
            TransactionOuterClass.Transaction
                .newBuilder()
                .setReferenceBlockId(ByteString.copyFromUtf8("02"))
                .build()
        )
        val transactions = listOf(transaction1, transaction2)
        val response = Access.TransactionsResponse
            .newBuilder()
            .addAllTransactions(transactions.map { it.builder().build() })
            .build()
        `when`(api.getTransactionsByBlockID(any())).thenReturn(setupFutureMock(response))

        val result = asyncFlowAccessApi.getTransactionsByBlockId(blockId).get()
        assert(result is FlowAccessApi.AccessApiCallResponse.Success)
        result as FlowAccessApi.AccessApiCallResponse.Success
        assertEquals(2, result.data.size)
        assertEquals(transaction1, result.data[0])
        assertEquals(transaction2, result.data[1])
    }

    @Test
    fun `test getTransactionResultsByBlockId`() {
        val blockId = FlowId("01")
        val transactionResults = listOf(FlowTransactionResult.of(Access.TransactionResultResponse.getDefaultInstance()))
        val response = Access.TransactionResultsResponse
            .newBuilder()
            .addAllTransactionResults(transactionResults.map { it.builder().build() })
            .build()
        `when`(api.getTransactionResultsByBlockID(any())).thenReturn(setupFutureMock(response))

        val result = asyncFlowAccessApi.getTransactionResultsByBlockId(blockId).get()
        assert(result is FlowAccessApi.AccessApiCallResponse.Success)
        result as FlowAccessApi.AccessApiCallResponse.Success
        assertEquals(transactionResults, result.data)
    }

    @Test
    fun `test getTransactionResultsByBlockId with multiple results`() {
        val blockId = FlowId("01")
        val transactionResult1 = FlowTransactionResult.of(
            Access.TransactionResultResponse
                .newBuilder()
                .setStatus(TransactionOuterClass.TransactionStatus.SEALED)
                .setStatusCode(1)
                .setErrorMessage("message1")
                .build()
        )
        val transactionResult2 = FlowTransactionResult.of(
            Access.TransactionResultResponse
                .newBuilder()
                .setStatus(TransactionOuterClass.TransactionStatus.SEALED)
                .setStatusCode(2)
                .setErrorMessage("message2")
                .build()
        )
        val transactionResults = listOf(transactionResult1, transactionResult2)
        val response = Access.TransactionResultsResponse
            .newBuilder()
            .addAllTransactionResults(transactionResults.map { it.builder().build() })
            .build()
        `when`(api.getTransactionResultsByBlockID(any())).thenReturn(setupFutureMock(response))

        val result = asyncFlowAccessApi.getTransactionResultsByBlockId(blockId).get()
        assert(result is FlowAccessApi.AccessApiCallResponse.Success)
        result as FlowAccessApi.AccessApiCallResponse.Success
        assertEquals(2, result.data.size)
        assertEquals(transactionResult1, result.data[0])
        assertEquals(transactionResult2, result.data[1])
    }

    @Test
    fun `test getExecutionResultByBlockId`() {
        val blockId = FlowId("01")

        val chunks = listOf(FlowChunk(collectionIndex = 1, startState = ByteArray(0), eventCollection = ByteArray(0), blockId = FlowId("01"), totalComputationUsed = 1000L, numberOfTransactions = 10, index = 1L, endState = ByteArray(0), executionDataId = FlowId("02"), stateDeltaCommitment = ByteArray(0)))

        val serviceEvents = listOf(FlowServiceEvent(type = "ServiceEventType", payload = ByteArray(0)))

        val executionResult = FlowExecutionResult(blockId = FlowId("01"), previousResultId = FlowId("02"), chunks = chunks, serviceEvents = serviceEvents)

        val grpcChunks = chunks.map {
            ExecutionResultOuterClass.Chunk
                .newBuilder()
                .setCollectionIndex(it.collectionIndex)
                .setStartState(ByteString.copyFrom(it.startState))
                .setEventCollection(ByteString.copyFrom(it.eventCollection))
                .setBlockId(ByteString.copyFrom(it.blockId.bytes))
                .setTotalComputationUsed(it.totalComputationUsed)
                .setNumberOfTransactions(it.numberOfTransactions)
                .setIndex(it.index)
                .setEndState(ByteString.copyFrom(it.endState))
                .setExecutionDataId(ByteString.copyFrom(it.executionDataId.bytes))
                .setStateDeltaCommitment(ByteString.copyFrom(it.stateDeltaCommitment))
                .build()
        }

        val grpcServiceEvents = serviceEvents.map {
            ExecutionResultOuterClass.ServiceEvent
                .newBuilder()
                .setType(it.type)
                .setPayload(ByteString.copyFrom(it.payload))
                .build()
        }

        val response = Access.ExecutionResultByIDResponse
            .newBuilder()
            .setExecutionResult(
                ExecutionResultOuterClass.ExecutionResult
                    .newBuilder()
                    .setBlockId(ByteString.copyFrom(BLOCK_ID_BYTES))
                    .setPreviousResultId(ByteString.copyFrom(PARENT_ID_BYTES))
                    .addAllChunks(grpcChunks)
                    .addAllServiceEvents(grpcServiceEvents)
                    .build()
            ).build()

        `when`(api.getExecutionResultByID(any())).thenReturn(setupFutureMock(response))

        val result = asyncFlowAccessApi.getExecutionResultByBlockId(blockId).get()
        assert(result is FlowAccessApi.AccessApiCallResponse.Success)
        result as FlowAccessApi.AccessApiCallResponse.Success
        assertEquals(executionResult, result.data)
    }

    @Test
    fun `test getTransactionsByBlockId timeout exception`() {
        val blockId = FlowId("01")
        val future: ListenableFuture<Access.TransactionsResponse> = SettableFuture.create()
        `when`(api.getTransactionsByBlockID(any())).thenReturn(future)

        val executor = Executors.newSingleThreadExecutor()
        executor.submit {
            assertThrows<TimeoutException> {
                asyncFlowAccessApi.getTransactionsByBlockId(blockId).get(1, TimeUnit.SECONDS)
            }
        }

        executor.shutdown()
        executor.awaitTermination(2, TimeUnit.SECONDS)
    }
}
