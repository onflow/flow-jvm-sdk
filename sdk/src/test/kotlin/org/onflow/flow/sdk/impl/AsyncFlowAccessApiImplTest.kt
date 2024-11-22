package org.onflow.flow.sdk.impl

import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import com.google.protobuf.ByteString
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.onflow.flow.sdk.*
import org.onflow.flow.sdk.impl.FlowAccessApiImplTest.Companion.createMockAccount
import org.onflow.protobuf.access.Access
import org.onflow.protobuf.access.AccessAPIGrpc
import org.onflow.protobuf.entities.AccountOuterClass
import org.onflow.protobuf.entities.NodeVersionInfoOuterClass
import org.onflow.protobuf.entities.TransactionOuterClass
import java.time.LocalDateTime
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class AsyncFlowAccessApiImplTest {
    companion object {
        private val api = mock(AccessAPIGrpc.AccessAPIFutureStub::class.java)
        private val asyncFlowAccessApi = AsyncFlowAccessApiImpl(api)

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

        val flowId = FlowId.of("id".toByteArray())
        val blockId = FlowId("01")

        val mockAccountKey = FlowAccountKey.of(AccountOuterClass.AccountKey.getDefaultInstance())
        val mockAccountKeys = listOf(
            mockAccountKey,
            mockAccountKey
        )
        val testException = RuntimeException("Test exception")

        fun createMockNodeVersionInfo(): Access.GetNodeVersionInfoResponse =
            Access.GetNodeVersionInfoResponse
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

        fun createTransactionsResponse(transactions: List<FlowTransaction>): Access.TransactionsResponse =
            Access.TransactionsResponse
                .newBuilder()
                .addAllTransactions(transactions.map { it.builder().build() })
                .build()
    }

    private fun <T> setupFutureMock(response: T): ListenableFuture<T> {
        val future: ListenableFuture<T> = SettableFuture.create()
        (future as SettableFuture<T>).set(response)
        return future
    }

    object MockResponseFactory {
        fun accountResponse(mockAccount: FlowAccount) = Access.AccountResponse
            .newBuilder()
            .setAccount(mockAccount.builder().build())
            .build()

        fun getAccountResponse(mockAccount: FlowAccount) = Access.GetAccountResponse
            .newBuilder()
            .setAccount(mockAccount.builder().build())
            .build()
        fun accountKeyResponse(accountKey: FlowAccountKey) = Access.AccountKeyResponse
            .newBuilder()
            .setAccountKey(accountKey.builder().build())
            .build()

        fun accountKeysResponse(accountKeys: List<FlowAccountKey>) = Access.AccountKeysResponse
            .newBuilder()
            .addAllAccountKeys(accountKeys.map { it.builder().build() })
            .build()

        fun accountBalanceResponse(balance: Long) = Access.AccountBalanceResponse
            .newBuilder()
            .setBalance(balance)
            .build()

        fun blockHeaderResponse(mockBlockHeader: FlowBlockHeader) = Access.BlockHeaderResponse
            .newBuilder()
            .setBlock(mockBlockHeader.builder().build())
            .build()

        fun blockResponse(mockBlock: FlowBlock) = Access.BlockResponse
            .newBuilder()
            .setBlock(mockBlock.builder().build())
            .build()

        fun collectionResponse(mockCollection: FlowCollection) = Access.CollectionResponse
            .newBuilder()
            .setCollection(mockCollection.builder().build())
            .build()

        fun fullCollectionResponse(transactions: List<FlowTransaction>) = Access.FullCollectionResponse
            .newBuilder()
            .addAllTransactions(transactions.map { it.builder().build() })
            .build()

        fun executionResultResponse(mockExecutionResult: FlowExecutionResult) = Access.ExecutionResultByIDResponse
            .newBuilder()
            .setExecutionResult(mockExecutionResult.builder().build())
            .build()

        fun transactionResponse(mockTransaction: FlowTransaction) = Access.TransactionResponse
            .newBuilder()
            .setTransaction(mockTransaction.builder().build())
            .build()
        fun transactionResultResponse() = Access.TransactionResultResponse
            .newBuilder()
            .setStatus(TransactionOuterClass.TransactionStatus.SEALED)
            .setStatusCode(1)
            .setErrorMessage("message")
            .setBlockId(ByteString.copyFromUtf8("id"))
            .setBlockHeight(HEIGHT)
            .setTransactionId(ByteString.copyFromUtf8("id"))
            .setCollectionId(ByteString.copyFromUtf8("id"))
            .setComputationUsage(1L)
            .build()

        fun networkParametersResponse() = Access.GetNetworkParametersResponse
            .newBuilder()
            .setChainId("test_chain_id")
            .build()

        fun protocolStateSnapshotResponse() = Access.ProtocolStateSnapshotResponse
            .newBuilder()
            .setSerializedSnapshot(ByteString.copyFromUtf8("test_serialized_snapshot"))
            .build()

        fun scriptResponse() = Access.ExecuteScriptResponse
            .newBuilder()
            .setValue(ByteString.copyFromUtf8("response_value"))
            .build()

        fun sendTransactionResponse() = Access.SendTransactionResponse
            .newBuilder()
            .setId(ByteString.copyFromUtf8("01"))
            .build()
    }
    // Helper functions for mocking objects and assertions

    private fun createMockEventsResponse(resultsCount: Int): Access.EventsResponse {
        val results = List(resultsCount) {
            Access.EventsResponse.Result
                .newBuilder()
                .build()
        }
        return Access.EventsResponse
            .newBuilder()
            .addAllResults(results)
            .build()
    }

    private fun createMockExecutionResult(blockId: FlowId): FlowExecutionResult {
        val chunks = listOf(FlowChunk(collectionIndex = 1, startState = ByteArray(0), eventCollection = ByteArray(0), blockId = FlowId("01"), totalComputationUsed = 1000L, numberOfTransactions = 10, index = 1L, endState = ByteArray(0), executionDataId = FlowId("02"), stateDeltaCommitment = ByteArray(0)))
        val serviceEvents = listOf(FlowServiceEvent(type = "ServiceEventType", payload = ByteArray(0)))

        return FlowExecutionResult(blockId = blockId, previousResultId = FlowId("02"), chunks = chunks, serviceEvents = serviceEvents)
    }

    private fun createMockTransaction(referenceBlockId: String = "01"): FlowTransaction =
        FlowTransaction.of(
            TransactionOuterClass.Transaction
                .newBuilder()
                .setReferenceBlockId(ByteString.copyFromUtf8(referenceBlockId))
                .build()
        )

    private fun <T> assertSuccess(result: FlowAccessApi.AccessApiCallResponse<T>, expected: T) {
        assert(result is FlowAccessApi.AccessApiCallResponse.Success)
        assertEquals(expected, (result as FlowAccessApi.AccessApiCallResponse.Success).data)
    }

    private fun assertFailure(result: FlowAccessApi.AccessApiCallResponse<*>, expectedMessage: String, expectedThrowable: Throwable) {
        assert(result is FlowAccessApi.AccessApiCallResponse.Error)
        result as FlowAccessApi.AccessApiCallResponse.Error
        assertEquals(expectedMessage, result.message)
        assertEquals(expectedThrowable, result.throwable)
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
        `when`(api.getAccountKeyAtLatestBlock(any())).thenReturn(setupFutureMock(MockResponseFactory.accountKeyResponse(mockAccountKey)))

        val result = asyncFlowAccessApi.getAccountKeyAtLatestBlock(FlowAddress("01"), 0).get()
        assertSuccess(result, mockAccountKey)
    }

    @Test
    fun `test getAccountKeyAtLatestBlock failure`() {
        `when`(api.getAccountKeyAtLatestBlock(any())).thenThrow(testException)

        val result = asyncFlowAccessApi.getAccountKeyAtLatestBlock(FlowAddress("01"), 0).get()
        assertFailure(result, "Failed to get account key at latest block", testException)
    }

    @Test
    fun `test getAccountKeyAtBlockHeight success`() {
        val flowAddress = FlowAddress("01")
        val keyIndex = 0
        val blockHeight = HEIGHT
        val mockAccountKey = FlowAccountKey.of(AccountOuterClass.AccountKey.getDefaultInstance())
        `when`(api.getAccountKeyAtBlockHeight(any())).thenReturn(setupFutureMock(MockResponseFactory.accountKeyResponse(mockAccountKey)))

        val result = asyncFlowAccessApi.getAccountKeyAtBlockHeight(flowAddress, keyIndex, blockHeight).get()
        assertSuccess(result, mockAccountKey)
    }

    @Test
    fun `test getAccountKeyAtBlockHeight failure`() {
        val flowAddress = FlowAddress("01")
        val keyIndex = 0
        val blockHeight = HEIGHT

        `when`(api.getAccountKeyAtBlockHeight(any())).thenThrow(testException)

        val result = asyncFlowAccessApi.getAccountKeyAtBlockHeight(flowAddress, keyIndex, blockHeight).get()
        assertFailure(result, "Failed to get account key at block height", testException)
    }

    @Test
    fun `test getAccountKeysAtLatestBlock success`() {
        `when`(api.getAccountKeysAtLatestBlock(any())).thenReturn(setupFutureMock(MockResponseFactory.accountKeysResponse(mockAccountKeys)))

        val result = asyncFlowAccessApi.getAccountKeysAtLatestBlock(FlowAddress("01")).get()
        assertSuccess(result, mockAccountKeys)
    }

    @Test
    fun `test getAccountKeysAtLatestBlock failure`() {
        `when`(api.getAccountKeysAtLatestBlock(any())).thenThrow(testException)

        val result = asyncFlowAccessApi.getAccountKeysAtLatestBlock(FlowAddress("01")).get()
        assertFailure(result, "Failed to get account keys at latest block", testException)
    }

    @Test
    fun `test getAccountKeysAtBlockHeight success`() {
        val flowAddress = FlowAddress("01")
        val blockHeight = HEIGHT

        `when`(api.getAccountKeysAtBlockHeight(any())).thenReturn(setupFutureMock(MockResponseFactory.accountKeysResponse(mockAccountKeys)))

        val result = asyncFlowAccessApi.getAccountKeysAtBlockHeight(flowAddress, blockHeight).get()
        assertSuccess(result, mockAccountKeys)
    }

    @Test
    fun `test getAccountKeysAtBlockHeight failure`() {
        val flowAddress = FlowAddress("01")
        val blockHeight = HEIGHT

        `when`(api.getAccountKeysAtBlockHeight(any())).thenThrow(testException)

        val result = asyncFlowAccessApi.getAccountKeysAtBlockHeight(flowAddress, blockHeight).get()
        assertFailure(result, "Failed to get account keys at block height", testException)
    }

    @Test
    fun `test getLatestBlockHeader`() {
        `when`(api.getLatestBlockHeader(any())).thenReturn(setupFutureMock(MockResponseFactory.blockHeaderResponse(mockBlockHeader)))

        val result = asyncFlowAccessApi.getLatestBlockHeader(true).get()
        assertSuccess(result, mockBlockHeader)
    }

    @Test
    fun `test getBlockHeaderById`() {
        `when`(api.getBlockHeaderByID(any())).thenReturn(setupFutureMock(MockResponseFactory.blockHeaderResponse(mockBlockHeader)))

        val result = asyncFlowAccessApi.getBlockHeaderById(blockId).get()
        assertSuccess(result, mockBlockHeader)
    }

    @Test
    fun `test getBlockHeaderByHeight`() {
        `when`(api.getBlockHeaderByHeight(any())).thenReturn(setupFutureMock(MockResponseFactory.blockHeaderResponse(mockBlockHeader)))

        val result = asyncFlowAccessApi.getBlockHeaderByHeight(HEIGHT).get()
        assertSuccess(result, mockBlockHeader)
    }

    @Test
    fun `test getLatestBlock`() {
        `when`(api.getLatestBlock(any())).thenReturn(setupFutureMock(MockResponseFactory.blockResponse(mockBlock)))

        val result = asyncFlowAccessApi.getLatestBlock(true).get()
        assertSuccess(result, mockBlock)
    }

    @Test
    fun `test getBlockById`() {
        `when`(api.getBlockByID(any())).thenReturn(setupFutureMock(MockResponseFactory.blockResponse(mockBlock)))

        val result = asyncFlowAccessApi.getBlockById(blockId).get()
        assertSuccess(result, mockBlock)
    }

    @Test
    fun `test getBlockByHeight`() {
        `when`(api.getBlockByHeight(any())).thenReturn(setupFutureMock(MockResponseFactory.blockResponse(mockBlock)))

        val result = asyncFlowAccessApi.getBlockByHeight(HEIGHT).get()
        assertSuccess(result, mockBlock)
    }

    @Test
    fun `test getAccountBalanceAtLatestBlock success`() {
        val expectedBalance = 1000L
        `when`(api.getAccountBalanceAtLatestBlock(any())).thenReturn(setupFutureMock(MockResponseFactory.accountBalanceResponse(expectedBalance)))

        val result = asyncFlowAccessApi.getAccountBalanceAtLatestBlock(FlowAddress("01")).get()
        assertSuccess(result, expectedBalance)
    }

    @Test
    fun `test getAccountBalanceAtLatestBlock failure`() {
        `when`(api.getAccountBalanceAtLatestBlock(any())).thenThrow(testException)

        val result = asyncFlowAccessApi.getAccountBalanceAtLatestBlock(FlowAddress("01")).get()
        assertFailure(result, "Failed to get account balance at latest block", testException)
    }

    @Test
    fun `test getAccountBalanceAtBlockHeight success`() {
        val flowAddress = FlowAddress("01")
        val expectedBalance = 1000L

        `when`(api.getAccountBalanceAtBlockHeight(any())).thenReturn(setupFutureMock(MockResponseFactory.accountBalanceResponse(expectedBalance)))

        val result = asyncFlowAccessApi.getAccountBalanceAtBlockHeight(flowAddress, HEIGHT).get()
        assertSuccess(result, expectedBalance)
    }

    @Test
    fun `test getCollectionById`() {
        val collectionId = FlowId("01")
        val mockCollection = FlowCollection(collectionId, emptyList())
        `when`(api.getCollectionByID(any())).thenReturn(setupFutureMock(MockResponseFactory.collectionResponse(mockCollection)))

        val result = asyncFlowAccessApi.getCollectionById(collectionId).get()
        assertSuccess(result, mockCollection)
    }

    @Test
    fun `test getFullCollectionById`() {
        val collectionId = FlowId("01")
        val mockTransaction = FlowTransaction(FlowScript("script"), emptyList(), FlowId.of("01".toByteArray()), 123L, FlowTransactionProposalKey(FlowAddress("02"), 1, 123L), FlowAddress("02"), emptyList())
        `when`(api.getFullCollectionByID(any())).thenReturn(setupFutureMock(MockResponseFactory.fullCollectionResponse(listOf(mockTransaction))))

        val result = asyncFlowAccessApi.getFullCollectionById(collectionId).get()
        assertSuccess(result, listOf(mockTransaction))
    }

    @Test
    fun `test sendTransaction`() {
        val mockTransaction = FlowTransaction(FlowScript("script"), emptyList(), FlowId.of("01".toByteArray()), 123L, FlowTransactionProposalKey(FlowAddress("02"), 1, 123L), FlowAddress("02"), emptyList())
        `when`(api.sendTransaction(any())).thenReturn(setupFutureMock(MockResponseFactory.sendTransactionResponse()))

        val result = asyncFlowAccessApi.sendTransaction(mockTransaction).get()
        assertSuccess(result, FlowId.of("01".toByteArray()))
    }

    @Test
    fun `test getTransactionById`() {
        val flowTransaction = FlowTransaction(FlowScript("script"), emptyList(), flowId, 123L, FlowTransactionProposalKey(FlowAddress("02"), 1, 123L), FlowAddress("02"), emptyList())
        `when`(api.getTransaction(any())).thenReturn(setupFutureMock(MockResponseFactory.transactionResponse(flowTransaction)))

        val result = asyncFlowAccessApi.getTransactionById(flowId).get()
        assertSuccess(result, flowTransaction)
    }

    @Test
    fun `test getTransactionResultById`() {
        val flowTransactionResult = FlowTransactionResult(FlowTransactionStatus.SEALED, 1, "message", emptyList(), flowId, HEIGHT, flowId, flowId, 1L)

        `when`(api.getTransactionResult(any())).thenReturn(setupFutureMock(MockResponseFactory.transactionResultResponse()))

        val result = asyncFlowAccessApi.getTransactionResultById(flowId).get()
        assertSuccess(result, flowTransactionResult)
    }

    @Test
    fun `test getSystemTransaction`() {
        val flowTransaction = FlowTransaction(FlowScript("script"), emptyList(), flowId, 123L, FlowTransactionProposalKey(FlowAddress("02"), 1, 123L), FlowAddress("02"), emptyList())
        `when`(api.getSystemTransaction(any())).thenReturn(setupFutureMock(MockResponseFactory.transactionResponse(flowTransaction)))

        val result = asyncFlowAccessApi.getSystemTransaction(flowId).get()
        assertSuccess(result, flowTransaction)
    }

    @Test
    fun `test getSystemTransaction failure`() {
        `when`(api.getSystemTransaction(any())).thenThrow(testException)

        val result = asyncFlowAccessApi.getSystemTransaction(flowId).get()
        assertFailure(result, "Failed to get system transaction by block ID", testException)
    }

    @Test
    fun `test getSystemTransactionResult`() {
        val flowTransactionResult = FlowTransactionResult(
            FlowTransactionStatus.SEALED,
            1,
            "message",
            emptyList(),
            flowId,
            HEIGHT,
            flowId,
            flowId,
            1L
        )

        val successFlowId = FlowId.of("id_success".toByteArray())
        val successRequest = Access.GetSystemTransactionResultRequest
            .newBuilder()
            .setBlockId(successFlowId.byteStringValue)
            .build()

        `when`(api.getSystemTransactionResult(eq(successRequest)))
            .thenReturn(setupFutureMock(MockResponseFactory.transactionResultResponse()))

        val result = asyncFlowAccessApi.getSystemTransactionResult(successFlowId).get()
        assertSuccess(result, flowTransactionResult)
    }

    @Test
    fun `test getSystemTransactionResult failure`() {
        val failureFlowId = FlowId.of("id_failure".toByteArray())
        val failureRequest = Access.GetSystemTransactionResultRequest
            .newBuilder()
            .setBlockId(failureFlowId.byteStringValue)
            .build()

        `when`(api.getSystemTransactionResult(eq(failureRequest))).thenThrow(testException)

        val result = asyncFlowAccessApi.getSystemTransactionResult(failureFlowId).get()
        assertFailure(result, "Failed to get system transaction result by block ID", testException)
    }

    @Test
    fun `test getTransactionResultByIndex success`() {
        val index = 0
        val flowTransactionResult = FlowTransactionResult(FlowTransactionStatus.SEALED, 1, "message", emptyList(), flowId, HEIGHT, flowId, flowId, 1L)

        `when`(api.getTransactionResultByIndex(any())).thenReturn(setupFutureMock(MockResponseFactory.transactionResultResponse()))

        val result = asyncFlowAccessApi.getTransactionResultByIndex(flowId, index).get()
        assertSuccess(result, flowTransactionResult)
    }

    @Test
    fun `test getTransactionResultByIndex failure`() {
        val index = 0

        `when`(api.getTransactionResultByIndex(any())).thenThrow(testException)

        val result = asyncFlowAccessApi.getTransactionResultByIndex(flowId, index).get()
        assertFailure(result, "Failed to get transaction result by index", testException)
    }

    @Test
    fun `test getAccountByAddress`() {
        val flowAddress = FlowAddress("01")
        val flowAccount = createMockAccount(flowAddress)

        `when`(api.getAccount(any())).thenReturn(setupFutureMock(MockResponseFactory.getAccountResponse(flowAccount)))

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
        val flowAccount = createMockAccount(flowAddress)

        `when`(api.getAccountAtLatestBlock(any())).thenReturn(setupFutureMock(MockResponseFactory.accountResponse(flowAccount)))

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
        val flowAccount = createMockAccount(flowAddress)
        val height = 123L

        `when`(api.getAccountAtBlockHeight(any())).thenReturn(setupFutureMock(MockResponseFactory.accountResponse(flowAccount)))

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

        `when`(api.executeScriptAtLatestBlock(any())).thenReturn(setupFutureMock(MockResponseFactory.scriptResponse()))

        val result = asyncFlowAccessApi.executeScriptAtLatestBlock(script, arguments).get()
        assert(result is FlowAccessApi.AccessApiCallResponse.Success)
        result as FlowAccessApi.AccessApiCallResponse.Success
        assertEquals("response_value", result.data.stringValue)
    }

    @Test
    fun `test executeScriptAtBlockId`() {
        val script = FlowScript("some_script")
        val arguments = listOf(ByteString.copyFromUtf8("argument1"), ByteString.copyFromUtf8("argument2"))

        `when`(api.executeScriptAtBlockID(any())).thenReturn(setupFutureMock(MockResponseFactory.scriptResponse()))

        val result = asyncFlowAccessApi.executeScriptAtBlockId(script, blockId, arguments).get()
        assert(result is FlowAccessApi.AccessApiCallResponse.Success)
        result as FlowAccessApi.AccessApiCallResponse.Success
        assertEquals("response_value", result.data.stringValue)
    }

    @Test
    fun `test executeScriptAtBlockHeight`() {
        val script = FlowScript("some_script")
        val height = HEIGHT
        val arguments = listOf(ByteString.copyFromUtf8("argument1"), ByteString.copyFromUtf8("argument2"))

        `when`(api.executeScriptAtBlockHeight(any())).thenReturn(setupFutureMock(MockResponseFactory.scriptResponse()))

        val result = asyncFlowAccessApi.executeScriptAtBlockHeight(script, height, arguments).get()
        assert(result is FlowAccessApi.AccessApiCallResponse.Success)
        result as FlowAccessApi.AccessApiCallResponse.Success
        assertEquals("response_value", result.data.stringValue)
    }

    @Test
    fun `test getEventsForHeightRange`() {
        val type = "event_type"
        val range = 1L..10L
        `when`(api.getEventsForHeightRange(any())).thenReturn(setupFutureMock(createMockEventsResponse(2)))

        val result = asyncFlowAccessApi.getEventsForHeightRange(type, range).get()
        assert(result is FlowAccessApi.AccessApiCallResponse.Success)
        result as FlowAccessApi.AccessApiCallResponse.Success
        assertEquals(2, result.data.size)
    }

    @Test
    fun `test getEventsForBlockIds`() {
        val type = "event_type"
        val blockIds = setOf(FlowId("01"), FlowId("02"))
        `when`(api.getEventsForBlockIDs(any())).thenReturn(setupFutureMock(createMockEventsResponse(2)))

        val result = asyncFlowAccessApi.getEventsForBlockIds(type, blockIds).get()
        assert(result is FlowAccessApi.AccessApiCallResponse.Success)
        result as FlowAccessApi.AccessApiCallResponse.Success
        assertEquals(2, result.data.size)
    }

    @Test
    fun `test getNetworkParameters`() {
        val mockFlowChainId = FlowChainId.of("test_chain_id")
        `when`(api.getNetworkParameters(any())).thenReturn(setupFutureMock(MockResponseFactory.networkParametersResponse()))

        val result = asyncFlowAccessApi.getNetworkParameters().get()
        assertSuccess(result, mockFlowChainId)
    }

    @Test
    fun `test getLatestProtocolStateSnapshot`() {
        val mockFlowSnapshot = FlowSnapshot("test_serialized_snapshot".toByteArray())
        `when`(api.getLatestProtocolStateSnapshot(any())).thenReturn(setupFutureMock(MockResponseFactory.protocolStateSnapshotResponse()))

        val result = asyncFlowAccessApi.getLatestProtocolStateSnapshot().get()
        assertSuccess(result, mockFlowSnapshot)
    }

    @Test
    fun `test getProtocolStateSnapshotByBlockId`() {
        val mockFlowSnapshot = FlowSnapshot("test_serialized_snapshot".toByteArray())
        `when`(api.getProtocolStateSnapshotByBlockID(any())).thenReturn(setupFutureMock(MockResponseFactory.protocolStateSnapshotResponse()))

        val result = asyncFlowAccessApi.getProtocolStateSnapshotByBlockId(blockId).get()
        assertSuccess(result, mockFlowSnapshot)
    }

    @Test
    fun `test getProtocolStateSnapshotByHeight`() {
        val mockFlowSnapshot = FlowSnapshot("test_serialized_snapshot".toByteArray())
        `when`(api.getProtocolStateSnapshotByHeight(any())).thenReturn(setupFutureMock(MockResponseFactory.protocolStateSnapshotResponse()))

        val result = asyncFlowAccessApi.getProtocolStateSnapshotByHeight(HEIGHT).get()
        assertSuccess(result, mockFlowSnapshot)
    }

    @Test
    fun `test getNodeVersionInfo`() {
        val mockNodeVersionInfo = createMockNodeVersionInfo()
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
    fun `test getTransactionsByBlockId single result`() {
        val transactions = listOf(createMockTransaction())
        `when`(api.getTransactionsByBlockID(any())).thenReturn(setupFutureMock(createTransactionsResponse(transactions)))

        val result = asyncFlowAccessApi.getTransactionsByBlockId(blockId).get()
        assertSuccess(result, transactions)
    }

    @Test
    fun `test getTransactionsByBlockId with multiple results`() {
        val transactions = listOf(createMockTransaction(), createMockTransaction("02"))

        val request = Access.GetTransactionsByBlockIDRequest
            .newBuilder()
            .setBlockId(blockId.byteStringValue)
            .build()

        `when`(api.getTransactionsByBlockID(eq(request))).thenReturn(setupFutureMock(createTransactionsResponse(transactions)))

        val result = asyncFlowAccessApi.getTransactionsByBlockId(blockId).get()
        assertSuccess(result, transactions)
    }

    @Test
    fun `test getExecutionResultByBlockId`() {
        val executionResult = createMockExecutionResult(blockId)

        `when`(api.getExecutionResultByID(any())).thenReturn(setupFutureMock(MockResponseFactory.executionResultResponse(executionResult)))

        val result = asyncFlowAccessApi.getExecutionResultByBlockId(blockId).get()
        assertSuccess(result, executionResult)
    }

    @Test
    fun `test getTransactionsByBlockId timeout exception`() {
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
