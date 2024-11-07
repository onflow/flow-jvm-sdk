package org.onflow.flow.sdk.impl

import com.google.protobuf.ByteString
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.test.*
import org.onflow.flow.sdk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.onflow.flow.sdk.impl.AsyncFlowAccessApiImplTest.Companion.HEIGHT
import org.onflow.flow.sdk.impl.AsyncFlowAccessApiImplTest.Companion.blockId
import org.onflow.flow.sdk.impl.AsyncFlowAccessApiImplTest.Companion.createMockNodeVersionInfo
import org.onflow.flow.sdk.impl.AsyncFlowAccessApiImplTest.Companion.createTransactionsResponse
import org.onflow.flow.sdk.impl.AsyncFlowAccessApiImplTest.Companion.mockAccountKey
import org.onflow.flow.sdk.impl.AsyncFlowAccessApiImplTest.Companion.mockAccountKeys
import org.onflow.flow.sdk.impl.AsyncFlowAccessApiImplTest.Companion.mockBlock
import org.onflow.flow.sdk.impl.AsyncFlowAccessApiImplTest.Companion.mockBlockHeader
import org.onflow.flow.sdk.impl.AsyncFlowAccessApiImplTest.Companion.testException
import org.onflow.protobuf.access.Access
import org.onflow.protobuf.access.AccessAPIGrpc
import org.onflow.protobuf.entities.*
import org.onflow.protobuf.executiondata.ExecutionDataAPIGrpc
import org.onflow.protobuf.executiondata.Executiondata
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.math.BigDecimal

@ExperimentalCoroutinesApi
class FlowAccessApiImplTest {
    private lateinit var flowAccessApiImpl: FlowAccessApiImpl
    private lateinit var mockApi: AccessAPIGrpc.AccessAPIBlockingStub
    private lateinit var mockExecutionDataApi: ExecutionDataAPIGrpc.ExecutionDataAPIBlockingStub
    private lateinit var outputStreamCaptor: ByteArrayOutputStream
    private lateinit var originalOut: PrintStream

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    companion object {
        val mockTransactionResponse: Access.TransactionResultResponse = Access.TransactionResultResponse
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

        fun createMockTransaction(flowId: FlowId = FlowId("01")) = FlowTransaction(FlowScript("script"), emptyList(), flowId, 123L, FlowTransactionProposalKey(FlowAddress("02"), 1, 123L), FlowAddress("02"), emptyList())

        fun createMockAccount(flowAddress: FlowAddress) = FlowAccount(flowAddress, BigDecimal.ONE, FlowCode("code".toByteArray()), emptyList(), emptyMap())
    }

    @BeforeEach
    fun setUp() {
        mockApi = mock(AccessAPIGrpc.AccessAPIBlockingStub::class.java)
        mockExecutionDataApi = mock(ExecutionDataAPIGrpc.ExecutionDataAPIBlockingStub::class.java)
        flowAccessApiImpl = FlowAccessApiImpl(mockApi, mockExecutionDataApi)
        outputStreamCaptor = ByteArrayOutputStream()
        originalOut = System.out
        System.setOut(PrintStream(outputStreamCaptor))
    }

    @AfterEach
    fun tearDown() {
        System.setOut(originalOut)
    }

    @Test
    fun `Test ping`() {
        flowAccessApiImpl.ping()
        verify(mockApi).ping(Access.PingRequest.newBuilder().build())
    }

    @Test
    fun `Test getAccountKeyAtLatestBlock`() {
        val flowAddress = FlowAddress("01")
        val keyIndex = 0

        `when`(mockApi.getAccountKeyAtLatestBlock(any())).thenReturn(AsyncFlowAccessApiImplTest.MockResponseFactory.accountKeyResponse(mockAccountKey))

        val result = flowAccessApiImpl.getAccountKeyAtLatestBlock(flowAddress, keyIndex)
        assertResultSuccess(result) { assertEquals(mockAccountKey, it) }

        verify(mockApi).getAccountKeyAtLatestBlock(
            Access.GetAccountKeyAtLatestBlockRequest
                .newBuilder()
                .setAddress(flowAddress.byteStringValue)
                .setIndex(keyIndex)
                .build()
        )
    }

    @Test
    fun `Test getAccountKeyAtLatestBlock error case`() {
        val flowAddress = FlowAddress("01")
        val keyIndex = 0

        `when`(mockApi.getAccountKeyAtLatestBlock(any())).thenThrow(testException)

        val result = flowAccessApiImpl.getAccountKeyAtLatestBlock(flowAddress, keyIndex)

        assertTrue(result is FlowAccessApi.AccessApiCallResponse.Error)
        assertEquals("Failed to get account key at latest block", (result as FlowAccessApi.AccessApiCallResponse.Error).message)
        assertEquals(testException, result.throwable)
    }

    @Test
    fun `Test getAccountKeyAtBlockHeight`() {
        val flowAddress = FlowAddress("01")
        val keyIndex = 0
        val blockHeight = 123L

        `when`(mockApi.getAccountKeyAtBlockHeight(any())).thenReturn(AsyncFlowAccessApiImplTest.MockResponseFactory.accountKeyResponse(mockAccountKey))

        val result = flowAccessApiImpl.getAccountKeyAtBlockHeight(flowAddress, keyIndex, blockHeight)
        assertResultSuccess(result) { assertEquals(mockAccountKey, it) }

        verify(mockApi).getAccountKeyAtBlockHeight(
            Access.GetAccountKeyAtBlockHeightRequest
                .newBuilder()
                .setAddress(flowAddress.byteStringValue)
                .setIndex(keyIndex)
                .setBlockHeight(blockHeight)
                .build()
        )
    }

    @Test
    fun `Test getAccountKeyAtBlockHeight error case`() {
        val flowAddress = FlowAddress("01")
        val keyIndex = 0
        val blockHeight = 123L

        `when`(mockApi.getAccountKeyAtBlockHeight(any())).thenThrow(testException)

        val result = flowAccessApiImpl.getAccountKeyAtBlockHeight(flowAddress, keyIndex, blockHeight)

        assertTrue(result is FlowAccessApi.AccessApiCallResponse.Error)
        assertEquals("Failed to get account key at block height", (result as FlowAccessApi.AccessApiCallResponse.Error).message)
        assertEquals(testException, result.throwable)
    }

    @Test
    fun `Test getAccountKeysAtLatestBlock`() {
        val flowAddress = FlowAddress("01")

        `when`(mockApi.getAccountKeysAtLatestBlock(any())).thenReturn(AsyncFlowAccessApiImplTest.MockResponseFactory.accountKeysResponse(mockAccountKeys))

        val result = flowAccessApiImpl.getAccountKeysAtLatestBlock(flowAddress)
        assertResultSuccess(result) { assertEquals(mockAccountKeys, it) }

        verify(mockApi).getAccountKeysAtLatestBlock(
            Access.GetAccountKeysAtLatestBlockRequest
                .newBuilder()
                .setAddress(flowAddress.byteStringValue)
                .build()
        )
    }

    @Test
    fun `Test getAccountKeysAtLatestBlock error case`() {
        val flowAddress = FlowAddress("01")

        `when`(mockApi.getAccountKeysAtLatestBlock(any())).thenThrow(testException)

        val result = flowAccessApiImpl.getAccountKeysAtLatestBlock(flowAddress)

        assertTrue(result is FlowAccessApi.AccessApiCallResponse.Error)
        assertEquals("Failed to get account keys at latest block", (result as FlowAccessApi.AccessApiCallResponse.Error).message)
        assertEquals(testException, result.throwable)
    }

    @Test
    fun `Test getAccountKeysAtBlockHeight`() {
        val flowAddress = FlowAddress("01")
        val blockHeight = 123L

        `when`(mockApi.getAccountKeysAtBlockHeight(any())).thenReturn(AsyncFlowAccessApiImplTest.MockResponseFactory.accountKeysResponse(mockAccountKeys))

        val result = flowAccessApiImpl.getAccountKeysAtBlockHeight(flowAddress, blockHeight)
        assertResultSuccess(result) { assertEquals(mockAccountKeys, it) }

        verify(mockApi).getAccountKeysAtBlockHeight(
            Access.GetAccountKeysAtBlockHeightRequest
                .newBuilder()
                .setAddress(flowAddress.byteStringValue)
                .setBlockHeight(blockHeight)
                .build()
        )
    }

    @Test
    fun `Test getAccountKeysAtBlockHeight error case`() {
        val flowAddress = FlowAddress("01")

        `when`(mockApi.getAccountKeysAtBlockHeight(any())).thenThrow(testException)

        val result = flowAccessApiImpl.getAccountKeysAtBlockHeight(flowAddress, HEIGHT)

        assertTrue(result is FlowAccessApi.AccessApiCallResponse.Error)
        assertEquals("Failed to get account keys at block height", (result as FlowAccessApi.AccessApiCallResponse.Error).message)
        assertEquals(testException, result.throwable)
    }

    @Test
    fun `Test getLatestBlockHeader`() {
        `when`(mockApi.getLatestBlockHeader(any())).thenReturn(AsyncFlowAccessApiImplTest.MockResponseFactory.blockHeaderResponse(mockBlockHeader))

        val result = flowAccessApiImpl.getLatestBlockHeader(sealed = true)
        assertResultSuccess(result) { assertEquals(mockBlockHeader, it) }
    }

    @Test
    fun `Test getBlockHeaderById`() {
        `when`(mockApi.getBlockHeaderByID(any())).thenReturn(AsyncFlowAccessApiImplTest.MockResponseFactory.blockHeaderResponse(mockBlockHeader))

        val result = flowAccessApiImpl.getBlockHeaderById(blockId)
        assertResultSuccess(result) { assertEquals(mockBlockHeader, it) }
    }

    @Test
    fun `Test getBlockHeaderByHeight`() {
        `when`(mockApi.getBlockHeaderByHeight(any())).thenReturn(AsyncFlowAccessApiImplTest.MockResponseFactory.blockHeaderResponse(mockBlockHeader))

        val result = flowAccessApiImpl.getBlockHeaderByHeight(HEIGHT)
        assertResultSuccess(result) { assertEquals(mockBlockHeader, it) }
    }

    @Test
    fun `Test getLatestBlock`() {
        `when`(mockApi.getLatestBlock(any())).thenReturn(AsyncFlowAccessApiImplTest.MockResponseFactory.blockResponse(mockBlock))

        val result = flowAccessApiImpl.getLatestBlock(sealed = true)
        assertResultSuccess(result) { assertEquals(mockBlock, it) }
    }

    @Test
    fun `Test getBlockById`() {
        `when`(mockApi.getBlockByID(any())).thenReturn(AsyncFlowAccessApiImplTest.MockResponseFactory.blockResponse(mockBlock))

        val result = flowAccessApiImpl.getBlockById(blockId)
        assertResultSuccess(result) { assertEquals(mockBlock, it) }
    }

    @Test
    fun `Test getBlockByHeight`() {
        val height = 123L
        val mockBlock = mockBlock
        val blockProto = Access.BlockResponse
            .newBuilder()
            .setBlock(mockBlock.builder().build())
            .build()

        `when`(mockApi.getBlockByHeight(any())).thenReturn(blockProto)

        val result = flowAccessApiImpl.getBlockByHeight(height)
        assertResultSuccess(result) { assertEquals(mockBlock, it) }
    }

    @Test
    fun `Test getAccountBalanceAtLatestBlock success`() {
        val flowAddress = FlowAddress("01")
        val expectedBalance = 1000L

        `when`(mockApi.getAccountBalanceAtLatestBlock(any())).thenReturn(AsyncFlowAccessApiImplTest.MockResponseFactory.accountBalanceResponse(expectedBalance))

        val result = flowAccessApiImpl.getAccountBalanceAtLatestBlock(flowAddress)
        assertResultSuccess(result) { assertEquals(expectedBalance, it) }

        verify(mockApi).getAccountBalanceAtLatestBlock(
            Access.GetAccountBalanceAtLatestBlockRequest
                .newBuilder()
                .setAddress(flowAddress.byteStringValue)
                .build()
        )
    }

    @Test
    fun `Test getAccountBalanceAtLatestBlock failure`() {
        val flowAddress = FlowAddress("01")

        `when`(mockApi.getAccountBalanceAtLatestBlock(any())).thenThrow(testException)

        val result = flowAccessApiImpl.getAccountBalanceAtLatestBlock(flowAddress)

        assertTrue(result is FlowAccessApi.AccessApiCallResponse.Error)
        assertEquals("Failed to get account balance at latest block", (result as FlowAccessApi.AccessApiCallResponse.Error).message)
        assertEquals(testException, result.throwable)
    }

    @Test
    fun `Test getAccountBalanceAtBlockHeight success`() {
        val flowAddress = FlowAddress("01")
        val expectedBalance = 1000L

        `when`(mockApi.getAccountBalanceAtBlockHeight(any())).thenReturn(AsyncFlowAccessApiImplTest.MockResponseFactory.accountBalanceResponse(expectedBalance))

        val result = flowAccessApiImpl.getAccountBalanceAtBlockHeight(flowAddress, HEIGHT)
        assertResultSuccess(result) { assertEquals(expectedBalance, it) }

        verify(mockApi).getAccountBalanceAtBlockHeight(
            Access.GetAccountBalanceAtBlockHeightRequest
                .newBuilder()
                .setAddress(flowAddress.byteStringValue)
                .setBlockHeight(HEIGHT)
                .build()
        )
    }

    @Test
    fun `Test getAccountBalanceAtBlockHeight failure`() {
        val flowAddress = FlowAddress("01")

        `when`(mockApi.getAccountBalanceAtBlockHeight(any())).thenThrow(testException)

        val result = flowAccessApiImpl.getAccountBalanceAtBlockHeight(flowAddress, HEIGHT)

        assertTrue(result is FlowAccessApi.AccessApiCallResponse.Error)
        assertEquals("Failed to get account balance at block height", (result as FlowAccessApi.AccessApiCallResponse.Error).message)
        assertEquals(testException, result.throwable)
    }

    @Test
    fun `Test getCollectionById`() {
        val collectionId = FlowId("01")
        val mockCollection = FlowCollection(collectionId, emptyList())

        `when`(mockApi.getCollectionByID(any())).thenReturn(AsyncFlowAccessApiImplTest.MockResponseFactory.collectionResponse(mockCollection))

        val result = flowAccessApiImpl.getCollectionById(collectionId)
        assertResultSuccess(result) { assertEquals(mockCollection, it) }
    }

    @Test
    fun `Test getFullCollectionById`() {
        val collectionId = FlowId("01")
        val mockTransaction = createMockTransaction()

        `when`(mockApi.getFullCollectionByID(any())).thenReturn(AsyncFlowAccessApiImplTest.MockResponseFactory.fullCollectionResponse(listOf(mockTransaction)))

        val result = flowAccessApiImpl.getFullCollectionById(collectionId)
        assertResultSuccess(result) { assertEquals(listOf(mockTransaction), it) }
    }

    @Test
    fun `Test sendTransaction`() {
        val mockTransaction = createMockTransaction()

        `when`(mockApi.sendTransaction(any())).thenReturn(AsyncFlowAccessApiImplTest.MockResponseFactory.sendTransactionResponse())

        val result = flowAccessApiImpl.sendTransaction(mockTransaction)
        assertResultSuccess(result) { assertEquals(FlowId.of("01".toByteArray()), it) }
    }

    @Test
    fun `Test getTransactionById`() {
        val flowId = FlowId("01")
        val flowTransaction = createMockTransaction(flowId)

        `when`(mockApi.getTransaction(any())).thenReturn(AsyncFlowAccessApiImplTest.MockResponseFactory.transactionResponse(flowTransaction))

        val result = flowAccessApiImpl.getTransactionById(flowId)
        assertResultSuccess(result) { assertEquals(flowTransaction, it) }
    }

    @Test
    fun `Test getTransactionResultById`() {
        val flowId = FlowId.of("id".toByteArray())
        val flowTransactionResult = FlowTransactionResult(FlowTransactionStatus.SEALED, 1, "message", emptyList(), flowId, 1L, flowId, flowId, 1L)

        `when`(mockApi.getTransactionResult(any())).thenReturn(mockTransactionResponse)

        val result = flowAccessApiImpl.getTransactionResultById(flowId)
        assertResultSuccess(result) { assertEquals(flowTransactionResult, it) }
    }

    @Test
    fun `Test getTransactionResultByIndex`() {
        val index = 0
        val flowId = FlowId.of("id".toByteArray())
        val flowTransactionResult = FlowTransactionResult(FlowTransactionStatus.SEALED, 1, "message", emptyList(), flowId, 1L, flowId, flowId, 1L)

        `when`(mockApi.getTransactionResultByIndex(any())).thenReturn(mockTransactionResponse)

        val result = flowAccessApiImpl.getTransactionResultByIndex(flowId, index)
        assertResultSuccess(result) { assertEquals(flowTransactionResult, it) }
    }

    @Test
    fun `Test getTransactionResultByIndex error case`() {
        val index = 0
        val flowId = FlowId.of("id".toByteArray())

        `when`(mockApi.getTransactionResultByIndex(any())).thenThrow(testException)

        val result = flowAccessApiImpl.getTransactionResultByIndex(flowId, index)

        assertTrue(result is FlowAccessApi.AccessApiCallResponse.Error)
        assertEquals("Failed to get transaction result by index", (result as FlowAccessApi.AccessApiCallResponse.Error).message)
        assertEquals(testException, result.throwable)
    }

    @Test
    fun `Test getAccountByAddress`() {
        val flowAddress = FlowAddress("01")
        val flowAccount = createMockAccount(flowAddress)

        `when`(mockApi.getAccount(any())).thenReturn(AsyncFlowAccessApiImplTest.MockResponseFactory.getAccountResponse(flowAccount))

        val result = flowAccessApiImpl.getAccountByAddress(flowAddress)
        assertResultSuccess(result) {
            assertEquals(flowAccount.address, it.address)
            assertEquals(flowAccount.keys, it.keys)
            assertEquals(flowAccount.contracts, it.contracts)
            assertEquals(flowAccount.balance.stripTrailingZeros(), it.balance.stripTrailingZeros())
        }
    }

    @Test
    fun `Test getAccountAtLatestBlock`() {
        val flowAddress = FlowAddress("01")
        val flowAccount = createMockAccount(flowAddress)

        `when`(mockApi.getAccountAtLatestBlock(any())).thenReturn(AsyncFlowAccessApiImplTest.MockResponseFactory.accountResponse(flowAccount))

        val result = flowAccessApiImpl.getAccountAtLatestBlock(flowAddress)
        assertResultSuccess(result) {
            assertEquals(flowAccount.address, it.address)
            assertEquals(flowAccount.keys, it.keys)
            assertEquals(flowAccount.contracts, it.contracts)
            assertEquals(flowAccount.balance.stripTrailingZeros(), it.balance.stripTrailingZeros())
        }
    }

    @Test
    fun `Test getAccountByBlockHeight`() {
        val flowAddress = FlowAddress("01")
        val flowAccount = createMockAccount(flowAddress)

        `when`(mockApi.getAccountAtBlockHeight(any())).thenReturn(AsyncFlowAccessApiImplTest.MockResponseFactory.accountResponse(flowAccount))

        val result = flowAccessApiImpl.getAccountByBlockHeight(flowAddress, HEIGHT)
        assertResultSuccess(result) {
            assertEquals(flowAccount.address, it.address)
            assertEquals(flowAccount.keys, it.keys)
            assertEquals(flowAccount.contracts, it.contracts)
            assertEquals(flowAccount.balance.stripTrailingZeros(), it.balance.stripTrailingZeros())
        }
    }

    @Test
    fun `Test executeScriptAtLatestBlock`() {
        val script = FlowScript("script".toByteArray())
        val arguments = listOf(ByteString.copyFromUtf8("argument1"), ByteString.copyFromUtf8("argument2"))

        `when`(mockApi.executeScriptAtLatestBlock(any())).thenReturn(AsyncFlowAccessApiImplTest.MockResponseFactory.scriptResponse())

        val result = flowAccessApiImpl.executeScriptAtLatestBlock(script, arguments)
        assertResultSuccess(result) { assertEquals("response_value", it.stringValue) }

        verify(mockApi).executeScriptAtLatestBlock(
            Access.ExecuteScriptAtLatestBlockRequest
                .newBuilder()
                .setScript(script.byteStringValue)
                .addAllArguments(arguments)
                .build()
        )
    }

    @Test
    fun `Test executeScriptAtBlockId`() {
        val script = FlowScript("some_script")
        val arguments = listOf(ByteString.copyFromUtf8("argument1"), ByteString.copyFromUtf8("argument2"))

        `when`(mockApi.executeScriptAtBlockID(any())).thenReturn(AsyncFlowAccessApiImplTest.MockResponseFactory.scriptResponse())

        val result = flowAccessApiImpl.executeScriptAtBlockId(script, blockId, arguments)
        assertResultSuccess(result) { assertEquals("response_value", it.stringValue) }

        verify(mockApi).executeScriptAtBlockID(
            Access.ExecuteScriptAtBlockIDRequest
                .newBuilder()
                .setBlockId(blockId.byteStringValue)
                .setScript(script.byteStringValue)
                .addAllArguments(arguments)
                .build()
        )
    }

    @Test
    fun `Test executeScriptAtBlockHeight`() {
        val script = FlowScript("some_script")
        val arguments = listOf(ByteString.copyFromUtf8("argument1"), ByteString.copyFromUtf8("argument2"))

        `when`(mockApi.executeScriptAtBlockHeight(any())).thenReturn(AsyncFlowAccessApiImplTest.MockResponseFactory.scriptResponse())

        val result = flowAccessApiImpl.executeScriptAtBlockHeight(script, HEIGHT, arguments)
        assertResultSuccess(result) { assertEquals("response_value", it.stringValue) }

        verify(mockApi).executeScriptAtBlockHeight(
            Access.ExecuteScriptAtBlockHeightRequest
                .newBuilder()
                .setBlockHeight(HEIGHT)
                .setScript(script.byteStringValue)
                .addAllArguments(arguments)
                .build()
        )
    }

    @Test
    fun `Test getEventsForHeightRange`() {
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

        `when`(mockApi.getEventsForHeightRange(any())).thenReturn(response)

        val result = flowAccessApiImpl.getEventsForHeightRange(type = type, range = range)
        assertResultSuccess(result) { assertEquals(2, it.size) }

        verify(mockApi).getEventsForHeightRange(
            Access.GetEventsForHeightRangeRequest
                .newBuilder()
                .setType(type)
                .setStartHeight(range.first)
                .setEndHeight(range.last)
                .build()
        )
    }

    @Test
    fun `Test getEventsForBlockIds`() {
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

        `when`(mockApi.getEventsForBlockIDs(any())).thenReturn(response)

        val result = flowAccessApiImpl.getEventsForBlockIds(type, blockIds)
        assertResultSuccess(result) { assertEquals(2, it.size) }
    }

    @Test
    fun `Test getNetworkParameters`() {
        val mockFlowChainId = FlowChainId.of("test_chain_id")

        `when`(mockApi.getNetworkParameters(Access.GetNetworkParametersRequest.newBuilder().build())).thenReturn(AsyncFlowAccessApiImplTest.MockResponseFactory.networkParametersResponse())

        val result = flowAccessApiImpl.getNetworkParameters()
        assertResultSuccess(result) { assertEquals(mockFlowChainId, it) }
    }

    @Test
    fun `Test getLatestProtocolStateSnapshot`() {
        val mockFlowSnapshot = FlowSnapshot("test_serialized_snapshot".toByteArray())

        `when`(mockApi.getLatestProtocolStateSnapshot(Access.GetLatestProtocolStateSnapshotRequest.newBuilder().build())).thenReturn(AsyncFlowAccessApiImplTest.MockResponseFactory.protocolStateSnapshotResponse())

        val result = flowAccessApiImpl.getLatestProtocolStateSnapshot()
        assertResultSuccess(result) { assertEquals(mockFlowSnapshot, it) }
    }

    @Test
    fun `Test getProtocolStateSnapshotByBlockId`() {
        val mockFlowSnapshot = FlowSnapshot("test_serialized_snapshot".toByteArray())

        `when`(
            mockApi.getProtocolStateSnapshotByBlockID(
                Access.GetProtocolStateSnapshotByBlockIDRequest
                    .newBuilder()
                    .setBlockId(blockId.byteStringValue)
                    .build()
            )
        ).thenReturn(AsyncFlowAccessApiImplTest.MockResponseFactory.protocolStateSnapshotResponse())

        val result = flowAccessApiImpl.getProtocolStateSnapshotByBlockId(blockId)
        assertResultSuccess(result) { assertEquals(mockFlowSnapshot, it) }
    }

    @Test
    fun `Test getProtocolStateSnapshotByHeight`() {
        val mockFlowSnapshot = FlowSnapshot("test_serialized_snapshot".toByteArray())

        `when`(
            mockApi.getProtocolStateSnapshotByHeight(
                Access.GetProtocolStateSnapshotByHeightRequest
                    .newBuilder()
                    .setBlockHeight(HEIGHT)
                    .build()
            )
        ).thenReturn(AsyncFlowAccessApiImplTest.MockResponseFactory.protocolStateSnapshotResponse())

        val result = flowAccessApiImpl.getProtocolStateSnapshotByHeight(HEIGHT)
        assertResultSuccess(result) { assertEquals(mockFlowSnapshot, it) }
    }

    @Test
    fun `Test getNodeVersionInfo`() {
        val mockNodeVersionInfo = createMockNodeVersionInfo()

        `when`(mockApi.getNodeVersionInfo(any())).thenReturn(mockNodeVersionInfo)

        val result = flowAccessApiImpl.getNodeVersionInfo()

        assertResultSuccess(result) {
            assertEquals("v0.0.1", it.semver)
            assertEquals("123456", it.commit)
            assertArrayEquals("sporkId".toByteArray(), it.sporkId)
            assertEquals(5, it.protocolVersion)
            assertEquals(1000L, it.sporkRootBlockHeight)
            assertEquals(1001L, it.nodeRootBlockHeight)
            assertEquals(100L, it.compatibleRange?.startHeight)
            assertEquals(200L, it.compatibleRange?.endHeight)
        }
    }

    @Test
    fun `Test getTransactionsByBlockId`() {
        val transactions = listOf(FlowTransaction.of(TransactionOuterClass.Transaction.getDefaultInstance()))
        val response = createTransactionsResponse(transactions)
        `when`(mockApi.getTransactionsByBlockID(any())).thenReturn(response)

        val result = flowAccessApiImpl.getTransactionsByBlockId(blockId)
        assertResultSuccess(result) { assertEquals(transactions, it) }
    }

    @Test
    fun `Test getTransactionsByBlockId with multiple results`() {
        val transaction1 = FlowTransaction.of(TransactionOuterClass.Transaction.getDefaultInstance())
        val transaction2 = FlowTransaction.of(
            TransactionOuterClass.Transaction
                .newBuilder()
                .setReferenceBlockId(ByteString.copyFromUtf8("02"))
                .build()
        )
        val transactions = listOf(transaction1, transaction2)
        val response = createTransactionsResponse(transactions)

        `when`(mockApi.getTransactionsByBlockID(any())).thenReturn(response)

        val result = flowAccessApiImpl.getTransactionsByBlockId(blockId)
        assertResultSuccess(result) {
            assertEquals(2, it.size)
            assertEquals(transaction1, it[0])
            assertEquals(transaction2, it[1])
        }
    }

    @Test
    fun `Test getTransactionResultsByBlockId`() {
        val transactionResults = listOf(
            FlowTransactionResult.of(
                Access.TransactionResultResponse
                    .getDefaultInstance()
            )
        )
        val response = Access.TransactionResultsResponse
            .newBuilder()
            .addAllTransactionResults(transactionResults.map { it.builder().build() })
            .build()

        `when`(mockApi.getTransactionResultsByBlockID(any())).thenReturn(response)

        val result = flowAccessApiImpl.getTransactionResultsByBlockId(blockId)
        assertResultSuccess(result) { assertEquals(transactionResults, it) }
    }

    @Test
    fun `Test getTransactionResultsByBlockId with multiple results`() {
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

        `when`(mockApi.getTransactionResultsByBlockID(any())).thenReturn(response)

        val result = flowAccessApiImpl.getTransactionResultsByBlockId(blockId)
        assertResultSuccess(result) {
            assertEquals(2, it.size)
            assertEquals(transactionResult1, it[0])
            assertEquals(transactionResult2, it[1])
        }
    }

    @Test
    fun `Test getExecutionResultByBlockId`() {
        val grpcExecutionResult = ExecutionResultOuterClass.ExecutionResult
            .newBuilder()
            .setBlockId(ByteString.copyFromUtf8("01"))
            .setPreviousResultId(ByteString.copyFromUtf8("02"))
            .addChunks(ExecutionResultOuterClass.Chunk.newBuilder().build())
            .addServiceEvents(ExecutionResultOuterClass.ServiceEvent.newBuilder().build())
            .build()
        val response = Access.ExecutionResultByIDResponse
            .newBuilder()
            .setExecutionResult(grpcExecutionResult)
            .build()

        `when`(mockApi.getExecutionResultByID(any())).thenReturn(response)

        val result = flowAccessApiImpl.getExecutionResultByBlockId(blockId)
        assertResultSuccess(result) {
            assertEquals(FlowExecutionResult.of(response), it)
        }
    }

    @Test
    fun `Test subscribeExecutionDataByBlockHeight success case`() = runTest {
        val blockHeight = 100L
        val expectedExecutionDataProto = BlockExecutionDataOuterClass.BlockExecutionData.getDefaultInstance()
        val expectedExecutionData = FlowBlockExecutionData.of(expectedExecutionDataProto)

        val responseIterator = mock(Iterator::class.java) as Iterator<Executiondata.SubscribeExecutionDataResponse>
        `when`(responseIterator.hasNext()).thenReturn(true, false)
        `when`(responseIterator.next()).thenReturn(
            Executiondata.SubscribeExecutionDataResponse
                .newBuilder()
                .setBlockExecutionData(expectedExecutionDataProto)
                .build()
        )

        `when`(mockExecutionDataApi.subscribeExecutionDataFromStartBlockHeight(any())).thenReturn(responseIterator)

        val (responseChannel, _) = flowAccessApiImpl.subscribeExecutionDataByBlockHeight(testScope, blockHeight)
        backgroundScope.launch {
            responseChannel.consumeEach { executionData ->
                assertEquals(expectedExecutionData, executionData)
            }
        }
    }

    @Test
    fun `Test subscribeExecutionDataByBlockHeight error case`() = runTest {
        val blockHeight = 100L

        `when`(mockExecutionDataApi.subscribeExecutionDataFromStartBlockHeight(any()))
            .thenAnswer { throw testException }

        val (_, errorChannel) = flowAccessApiImpl.subscribeExecutionDataByBlockHeight(this, blockHeight)

        var receivedException: Throwable? = null
        val job = launch {
            receivedException = errorChannel.receiveCatching().getOrNull()
        }
        advanceUntilIdle()
        job.join()

        if (receivedException != null) {
            assertEquals(testException.message, receivedException!!.message)
        } else {
            fail("Expected error but got success")
        }

        errorChannel.cancel()
    }

    @Test
    fun `Test subscribeEventsByBlockId error case`() = runTest {
        `when`(mockExecutionDataApi.subscribeEventsFromStartBlockID(any()))
            .thenThrow(testException)

        val (_, errorChannel) = flowAccessApiImpl.subscribeEventsByBlockId(this, blockId)

        var receivedException: Throwable? = null

        val job = launch {
            receivedException = errorChannel.receiveCatching().getOrNull()
        }
        advanceUntilIdle()
        job.join()

        assertEquals(testException.message, receivedException?.message ?: "Exception not propagated correctly")

        errorChannel.cancel()
    }

    @Test
    fun `Test subscribeEventsByBlockHeight success case`() = runTest {
        val blockHeight = 100L
        val expectedEventsProto = EventOuterClass.Event.getDefaultInstance()
        val expectedEvents = listOf(FlowEvent.of(expectedEventsProto))

        val responseIterator = mock(Iterator::class.java) as Iterator<Executiondata.SubscribeEventsResponse>
        `when`(responseIterator.hasNext()).thenReturn(true, false)
        `when`(responseIterator.next()).thenReturn(
            Executiondata.SubscribeEventsResponse
                .newBuilder()
                .addAllEvents(listOf(expectedEventsProto))
                .build()
        )

        `when`(mockExecutionDataApi.subscribeEventsFromStartHeight(any())).thenReturn(responseIterator)

        val (responseChannel, _) = flowAccessApiImpl.subscribeEventsByBlockHeight(testScope, blockHeight)
        backgroundScope.launch {
            responseChannel.consumeEach { events ->
                assertEquals(expectedEvents, events)
            }
        }
    }

    @Test
    fun `Test subscribeEventsByBlockHeight error case`() = runTest {
        val blockHeight = 100L

        `when`(mockExecutionDataApi.subscribeEventsFromStartHeight(any())).thenThrow(testException)

        val (_, errorChannel) = flowAccessApiImpl.subscribeEventsByBlockHeight(this, blockHeight)

        var receivedException: Throwable? = null
        val job = launch {
            receivedException = errorChannel.receiveCatching().getOrNull()
        }
        advanceUntilIdle()
        job.join()

        if (receivedException != null) {
            assertEquals(testException.message, receivedException!!.message)
        } else {
            fail("Expected error but got success")
        }

        errorChannel.cancel()
    }

    @Test
    fun `Test subscribeExecutionDataByBlockId success case`() = runTest {
        val blockId = FlowId("01")
        val expectedExecutionDataProto = BlockExecutionDataOuterClass.BlockExecutionData.getDefaultInstance()
        val expectedExecutionData = FlowBlockExecutionData.of(expectedExecutionDataProto)

        val responseIterator = mock(Iterator::class.java) as Iterator<Executiondata.SubscribeExecutionDataResponse>
        `when`(responseIterator.hasNext()).thenReturn(true, false)
        `when`(responseIterator.next()).thenReturn(
            Executiondata.SubscribeExecutionDataResponse
                .newBuilder()
                .setBlockExecutionData(expectedExecutionDataProto)
                .build()
        )

        `when`(mockExecutionDataApi.subscribeExecutionDataFromStartBlockID(any())).thenReturn(responseIterator)

        val (responseChannel, _) = flowAccessApiImpl.subscribeExecutionDataByBlockId(testScope, blockId)
        backgroundScope.launch {
            responseChannel.consumeEach { executionData ->
                assertEquals(expectedExecutionData, executionData)
            }
        }
    }

    @Test
    fun `Test subscribeExecutionDataByBlockId error case`() = runTest {
        val blockId = FlowId("01")

        `when`(mockExecutionDataApi.subscribeExecutionDataFromStartBlockID(any())).thenThrow(testException)

        val (_, errorChannel) = flowAccessApiImpl.subscribeExecutionDataByBlockId(this, blockId)

        var receivedException: Throwable? = null
        val job = launch {
            receivedException = errorChannel.receiveCatching().getOrNull()
        }
        advanceUntilIdle()
        job.join()

        if (receivedException != null) {
            assertEquals(testException.message, receivedException!!.message)
        } else {
            fail("Expected error but got success")
        }

        errorChannel.cancel()
    }

    private fun <T> assertResultSuccess(result: FlowAccessApi.AccessApiCallResponse<T>, assertions: (T) -> Unit) {
        when (result) {
            is FlowAccessApi.AccessApiCallResponse.Success -> assertions(result.data)
            is FlowAccessApi.AccessApiCallResponse.Error -> throw IllegalStateException("Request failed: ${result.message}", result.throwable)
        }
    }
}
