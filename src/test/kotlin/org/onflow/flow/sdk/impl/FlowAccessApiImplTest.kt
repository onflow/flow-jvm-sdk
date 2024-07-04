package org.onflow.flow.sdk.impl

import com.google.protobuf.ByteString
import org.onflow.flow.sdk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.onflow.protobuf.access.Access
import org.onflow.protobuf.access.AccessAPIGrpc
import org.onflow.protobuf.entities.ExecutionResultOuterClass
import org.onflow.protobuf.entities.TransactionOuterClass
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.math.BigDecimal
import java.time.LocalDateTime

class FlowAccessApiImplTest {
    private lateinit var flowAccessApiImpl: FlowAccessApiImpl
    private lateinit var mockApi: AccessAPIGrpc.AccessAPIBlockingStub
    private lateinit var outputStreamCaptor: ByteArrayOutputStream
    private lateinit var originalOut: PrintStream

    @BeforeEach
    fun setUp() {
        mockApi = mock(AccessAPIGrpc.AccessAPIBlockingStub::class.java)
        flowAccessApiImpl = FlowAccessApiImpl(mockApi)
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
    fun `Test getLatestBlockHeader`() {
        val mockBlockHeader = FlowBlockHeader(FlowId("01"), FlowId("01"), 123L)
        val blockHeaderProto = Access.BlockHeaderResponse.newBuilder().setBlock(mockBlockHeader.builder().build()).build()

        `when`(mockApi.getLatestBlockHeader(any())).thenReturn(blockHeaderProto)

        val result = flowAccessApiImpl.getLatestBlockHeader(sealed = true)
        assertResultSuccess(result) { assertEquals(mockBlockHeader, it) }
    }

    @Test
    fun `Test getBlockHeaderById`() {
        val blockId = FlowId("01")
        val mockBlockHeader = FlowBlockHeader(blockId, FlowId("01"), 123L)
        val blockHeaderProto = Access.BlockHeaderResponse.newBuilder().setBlock(mockBlockHeader.builder().build()).build()

        `when`(mockApi.getBlockHeaderByID(any())).thenReturn(blockHeaderProto)

        val result = flowAccessApiImpl.getBlockHeaderById(blockId)
        assertResultSuccess(result) { assertEquals(mockBlockHeader, it) }
    }

    @Test
    fun `Test getBlockHeaderByHeight`() {
        val height = 123L
        val mockBlockHeader = FlowBlockHeader(FlowId("01"), FlowId("01"), height)
        val blockHeaderProto = Access.BlockHeaderResponse.newBuilder().setBlock(mockBlockHeader.builder().build()).build()

        `when`(mockApi.getBlockHeaderByHeight(any())).thenReturn(blockHeaderProto)

        val result = flowAccessApiImpl.getBlockHeaderByHeight(height)
        assertResultSuccess(result) { assertEquals(mockBlockHeader, it) }
    }

    @Test
    fun `Test getLatestBlock`() {
        val mockBlock = FlowBlock(FlowId("01"), FlowId("01"), 123L, LocalDateTime.now(), emptyList(), emptyList(), emptyList())
        val blockProto = Access.BlockResponse.newBuilder().setBlock(mockBlock.builder().build()).build()

        `when`(mockApi.getLatestBlock(any())).thenReturn(blockProto)

        val result = flowAccessApiImpl.getLatestBlock(sealed = true)
        assertResultSuccess(result) { assertEquals(mockBlock, it) }
    }

    @Test
    fun `Test getBlockById`() {
        val blockId = FlowId("01")
        val mockBlock = FlowBlock(blockId, FlowId("01"), 123L, LocalDateTime.now(), emptyList(), emptyList(), emptyList())
        val blockProto = Access.BlockResponse.newBuilder().setBlock(mockBlock.builder().build()).build()

        `when`(mockApi.getBlockByID(any())).thenReturn(blockProto)

        val result = flowAccessApiImpl.getBlockById(blockId)
        assertResultSuccess(result) { assertEquals(mockBlock, it) }
    }

    @Test
    fun `Test getBlockByHeight`() {
        val height = 123L
        val mockBlock = FlowBlock(FlowId("01"), FlowId("01"), height, LocalDateTime.now(), emptyList(), emptyList(), emptyList())
        val blockProto = Access.BlockResponse.newBuilder().setBlock(mockBlock.builder().build()).build()

        `when`(mockApi.getBlockByHeight(any())).thenReturn(blockProto)

        val result = flowAccessApiImpl.getBlockByHeight(height)
        assertResultSuccess(result) { assertEquals(mockBlock, it) }
    }

    @Test
    fun `Test getCollectionById`() {
        val collectionId = FlowId("01")
        val mockCollection = FlowCollection(collectionId, emptyList())
        val collectionProto = Access.CollectionResponse.newBuilder().setCollection(mockCollection.builder().build()).build()

        `when`(mockApi.getCollectionByID(any())).thenReturn(collectionProto)

        val result = flowAccessApiImpl.getCollectionById(collectionId)
        assertResultSuccess(result) { assertEquals(mockCollection, it) }
    }

    @Test
    fun `Test sendTransaction`() {
        val mockTransaction = createMockTransaction()
        val transactionProto = Access.SendTransactionResponse.newBuilder().setId(ByteString.copyFromUtf8("01")).build()

        `when`(mockApi.sendTransaction(any())).thenReturn(transactionProto)

        val result = flowAccessApiImpl.sendTransaction(mockTransaction)
        assertResultSuccess(result) { assertEquals(FlowId.of("01".toByteArray()), it) }
    }

    @Test
    fun `Test getTransactionById`() {
        val flowId = FlowId("01")
        val flowTransaction = createMockTransaction(flowId)
        val transactionProto = Access.TransactionResponse.newBuilder().setTransaction(flowTransaction.builder().build()).build()

        `when`(mockApi.getTransaction(any())).thenReturn(transactionProto)

        val result = flowAccessApiImpl.getTransactionById(flowId)
        assertResultSuccess(result) { assertEquals(flowTransaction, it) }
    }

    @Test
    fun `Test getTransactionResultById`() {
        val flowId = FlowId.of("id".toByteArray())
        val flowTransactionResult = FlowTransactionResult(FlowTransactionStatus.SEALED, 1, "message", emptyList())
        val response = Access.TransactionResultResponse.newBuilder().setStatus(TransactionOuterClass.TransactionStatus.SEALED).setStatusCode(1).setErrorMessage("message").setBlockId(ByteString.copyFromUtf8("id")).build()

        `when`(mockApi.getTransactionResult(any())).thenReturn(response)

        val result = flowAccessApiImpl.getTransactionResultById(flowId)
        assertResultSuccess(result) { assertEquals(flowTransactionResult, it) }
    }

    @Test
    fun `Test getAccountByAddress`() {
        val flowAddress = FlowAddress("01")
        val flowAccount = createMockAccount(flowAddress)
        val accountProto = Access.GetAccountResponse.newBuilder().setAccount(flowAccount.builder().build()).build()

        `when`(mockApi.getAccount(any())).thenReturn(accountProto)

        val result = flowAccessApiImpl.getAccountByAddress(flowAddress)
        assertResultSuccess(result) {
            assertEquals(flowAccount.address, it.address)
            assertEquals(flowAccount.code, it.code)
            assertEquals(flowAccount.keys, it.keys)
            assertEquals(flowAccount.contracts, it.contracts)
            assertEquals(flowAccount.balance.stripTrailingZeros(), it.balance.stripTrailingZeros())
        }
    }

    @Test
    fun `Test getAccountAtLatestBlock`() {
        val flowAddress = FlowAddress("01")
        val flowAccount = createMockAccount(flowAddress)
        val accountProto = Access.AccountResponse.newBuilder().setAccount(flowAccount.builder().build()).build()

        `when`(mockApi.getAccountAtLatestBlock(any())).thenReturn(accountProto)

        val result = flowAccessApiImpl.getAccountAtLatestBlock(flowAddress)
        assertResultSuccess(result) {
            assertEquals(flowAccount.address, it.address)
            assertEquals(flowAccount.code, it.code)
            assertEquals(flowAccount.keys, it.keys)
            assertEquals(flowAccount.contracts, it.contracts)
            assertEquals(flowAccount.balance.stripTrailingZeros(), it.balance.stripTrailingZeros())
        }
    }

    @Test
    fun `Test getAccountByBlockHeight`() {
        val flowAddress = FlowAddress("01")
        val flowAccount = createMockAccount(flowAddress)
        val height = 123L
        val accountProto = Access.AccountResponse.newBuilder().setAccount(flowAccount.builder().build()).build()

        `when`(mockApi.getAccountAtBlockHeight(any())).thenReturn(accountProto)

        val result = flowAccessApiImpl.getAccountByBlockHeight(flowAddress, height)
        assertResultSuccess(result) {
            assertEquals(flowAccount.address, it.address)
            assertEquals(flowAccount.code, it.code)
            assertEquals(flowAccount.keys, it.keys)
            assertEquals(flowAccount.contracts, it.contracts)
            assertEquals(flowAccount.balance.stripTrailingZeros(), it.balance.stripTrailingZeros())
        }
    }

    @Test
    fun `Test executeScriptAtLatestBlock`() {
        val script = FlowScript("script".toByteArray())
        val arguments = listOf(ByteString.copyFromUtf8("argument1"), ByteString.copyFromUtf8("argument2"))
        val response = Access.ExecuteScriptResponse.newBuilder().setValue(ByteString.copyFromUtf8("response_value")).build()

        `when`(mockApi.executeScriptAtLatestBlock(any())).thenReturn(response)

        val result = flowAccessApiImpl.executeScriptAtLatestBlock(script, arguments)
        assertResultSuccess(result) { assertEquals("response_value", it.stringValue) }

        verify(mockApi).executeScriptAtLatestBlock(Access.ExecuteScriptAtLatestBlockRequest.newBuilder().setScript(script.byteStringValue).addAllArguments(arguments).build())
    }

    @Test
    fun `Test executeScriptAtBlockId`() {
        val script = FlowScript("some_script")
        val blockId = FlowId("01")
        val arguments = listOf(ByteString.copyFromUtf8("argument1"), ByteString.copyFromUtf8("argument2"))
        val response = Access.ExecuteScriptResponse.newBuilder().setValue(ByteString.copyFromUtf8("response_value")).build()

        `when`(mockApi.executeScriptAtBlockID(any())).thenReturn(response)

        val result = flowAccessApiImpl.executeScriptAtBlockId(script, blockId, arguments)
        assertResultSuccess(result) { assertEquals("response_value", it.stringValue) }

        verify(mockApi).executeScriptAtBlockID(Access.ExecuteScriptAtBlockIDRequest.newBuilder().setBlockId(blockId.byteStringValue).setScript(script.byteStringValue).addAllArguments(arguments).build())
    }

    @Test
    fun `Test executeScriptAtBlockHeight`() {
        val script = FlowScript("some_script")
        val height = 123L
        val arguments = listOf(ByteString.copyFromUtf8("argument1"), ByteString.copyFromUtf8("argument2"))
        val response = Access.ExecuteScriptResponse.newBuilder().setValue(ByteString.copyFromUtf8("response_value")).build()

        `when`(mockApi.executeScriptAtBlockHeight(any())).thenReturn(response)

        val result = flowAccessApiImpl.executeScriptAtBlockHeight(script, height, arguments)
        assertResultSuccess(result) { assertEquals("response_value", it.stringValue) }

        verify(mockApi).executeScriptAtBlockHeight(Access.ExecuteScriptAtBlockHeightRequest.newBuilder().setBlockHeight(height).setScript(script.byteStringValue).addAllArguments(arguments).build())
    }

    @Test
    fun `Test getEventsForHeightRange`() {
        val type = "event_type"
        val range = 1L..10L
        val eventResult1 = Access.EventsResponse.Result.newBuilder().build()
        val eventResult2 = Access.EventsResponse.Result.newBuilder().build()
        val response = Access.EventsResponse.newBuilder().addResults(eventResult1).addResults(eventResult2).build()

        `when`(mockApi.getEventsForHeightRange(any())).thenReturn(response)

        val result = flowAccessApiImpl.getEventsForHeightRange(type, range)
        assertResultSuccess(result) { assertEquals(2, it.size) }

        verify(mockApi).getEventsForHeightRange(Access.GetEventsForHeightRangeRequest.newBuilder().setType(type).setStartHeight(range.first).setEndHeight(range.last).build())
    }

    @Test
    fun `Test getEventsForBlockIds`() {
        val type = "event_type"
        val blockIds = setOf(FlowId("01"), FlowId("02"))
        val eventResult1 = Access.EventsResponse.Result.newBuilder().build()
        val eventResult2 = Access.EventsResponse.Result.newBuilder().build()
        val response = Access.EventsResponse.newBuilder().addResults(eventResult1).addResults(eventResult2).build()

        `when`(mockApi.getEventsForBlockIDs(any())).thenReturn(response)

        val result = flowAccessApiImpl.getEventsForBlockIds(type, blockIds)
        assertResultSuccess(result) { assertEquals(2, it.size) }
    }

    @Test
    fun `Test getNetworkParameters`() {
        val mockFlowChainId = FlowChainId.of("test_chain_id")
        val response = Access.GetNetworkParametersResponse.newBuilder().setChainId("test_chain_id").build()

        `when`(mockApi.getNetworkParameters(Access.GetNetworkParametersRequest.newBuilder().build())).thenReturn(response)

        val result = flowAccessApiImpl.getNetworkParameters()
        assertResultSuccess(result) { assertEquals(mockFlowChainId, it) }
    }

    @Test
    fun `Test getLatestProtocolStateSnapshot`() {
        val mockFlowSnapshot = FlowSnapshot("test_serialized_snapshot".toByteArray())
        val response = Access.ProtocolStateSnapshotResponse.newBuilder().setSerializedSnapshot(ByteString.copyFromUtf8("test_serialized_snapshot")).build()

        `when`(mockApi.getLatestProtocolStateSnapshot(Access.GetLatestProtocolStateSnapshotRequest.newBuilder().build())).thenReturn(response)

        val result = flowAccessApiImpl.getLatestProtocolStateSnapshot()
        assertResultSuccess(result) { assertEquals(mockFlowSnapshot, it) }
    }

    private fun <T> assertResultSuccess(result: FlowAccessApi.AccessApiCallResponse<T>, assertions: (T) -> Unit) {
        when (result) {
            is FlowAccessApi.AccessApiCallResponse.Success -> assertions(result.data)
            is FlowAccessApi.AccessApiCallResponse.Error -> throw IllegalStateException("Request failed: ${result.message}", result.throwable)
        }
    }

    private fun createMockTransaction(flowId: FlowId = FlowId("01")) = FlowTransaction(FlowScript("script"), emptyList(), flowId, 123L, FlowTransactionProposalKey(FlowAddress("02"), 1, 123L), FlowAddress("02"), emptyList())

    private fun createMockAccount(flowAddress: FlowAddress) = FlowAccount(flowAddress, BigDecimal.ONE, FlowCode("code".toByteArray()), emptyList(), emptyMap())
}
