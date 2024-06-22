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

    private val api = mock(AccessAPIGrpc.AccessAPIBlockingStub::class.java)
    private val flowAccessApi = FlowAccessApiImpl(api)

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
        flowAccessApi.ping()
        verify(api).ping(Access.PingRequest.newBuilder().build())
    }

    @Test
    fun `Test getLatestBlockHeader`() {
        val mockBlockHeader = FlowBlockHeader(FlowId("01"), FlowId("01"), 123L)

        val blockHeaderProto = Access.BlockHeaderResponse.newBuilder().setBlock(mockBlockHeader.builder().build()).build()

        `when`(api.getLatestBlockHeader(any())).thenReturn(blockHeaderProto)

        when (val result = flowAccessApi.getLatestBlockHeader(sealed = true)) {
            is FlowAccessApi.FlowResult.Success -> assertEquals(mockBlockHeader, result.data)
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to get latest block header: ${result.message}", result.throwable)
        }
    }

    @Test
    fun `Test getBlockHeaderById`() {
        val blockId = FlowId("01")
        val mockBlockHeader = FlowBlockHeader(blockId, FlowId("01"), 123L)

        val blockHeaderProto = Access.BlockHeaderResponse.newBuilder().setBlock(mockBlockHeader.builder().build()).build()

        `when`(api.getBlockHeaderByID(any())).thenReturn(blockHeaderProto)

        when (val result = flowAccessApi.getBlockHeaderById(blockId)) {
            is FlowAccessApi.FlowResult.Success -> assertEquals(mockBlockHeader, result.data)
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to get block header by ID: ${result.message}", result.throwable)
        }
    }

    @Test
    fun `Test getBlockHeaderByHeight`() {
        val height = 123L
        val mockBlockHeader = FlowBlockHeader(FlowId("01"), FlowId("01"), height)

        val blockHeaderProto = Access.BlockHeaderResponse.newBuilder().setBlock(mockBlockHeader.builder().build()).build()

        `when`(api.getBlockHeaderByHeight(any())).thenReturn(blockHeaderProto)

        when (val result = flowAccessApi.getBlockHeaderByHeight(height)) {
            is FlowAccessApi.FlowResult.Success -> assertEquals(mockBlockHeader, result.data)
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to get block header by height: ${result.message}", result.throwable)
        }
    }

    @Test
    fun `Test getLatestBlock`() {
        val mockBlock = FlowBlock(FlowId("01"), FlowId("01"), 123L, LocalDateTime.now(), emptyList(), emptyList(), emptyList())

        val blockProto = Access.BlockResponse.newBuilder().setBlock(mockBlock.builder().build()).build()

        `when`(api.getLatestBlock(any())).thenReturn(blockProto)

        when (val result = flowAccessApi.getLatestBlock(sealed = true)) {
            is FlowAccessApi.FlowResult.Success -> assertEquals(mockBlock, result.data)
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to get latest block: ${result.message}", result.throwable)
        }
    }

    @Test
    fun `Test getBlockById`() {
        val blockId = FlowId("01")
        val mockBlock = FlowBlock(blockId, FlowId("01"), 123L, LocalDateTime.now(), emptyList(), emptyList(), emptyList())

        val blockProto = Access.BlockResponse.newBuilder().setBlock(mockBlock.builder().build()).build()

        `when`(api.getBlockByID(any())).thenReturn(blockProto)

        when (val result = flowAccessApi.getBlockById(blockId)) {
            is FlowAccessApi.FlowResult.Success -> assertEquals(mockBlock, result.data)
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to get block by ID: ${result.message}", result.throwable)
        }
    }

    @Test
    fun `Test getBlockByHeight`() {
        val height = 123L
        val mockBlock = FlowBlock(FlowId("01"), FlowId("01"), height, LocalDateTime.now(), emptyList(), emptyList(), emptyList())

        val blockProto = Access.BlockResponse.newBuilder().setBlock(mockBlock.builder().build()).build()

        `when`(api.getBlockByHeight(any())).thenReturn(blockProto)

        when (val result = flowAccessApi.getBlockByHeight(height)) {
            is FlowAccessApi.FlowResult.Success -> assertEquals(mockBlock, result.data)
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to get block by height: ${result.message}", result.throwable)
        }
    }

    @Test
    fun `Test getCollectionById`() {
        val collectionId = FlowId("01")
        val mockCollection = FlowCollection(collectionId, emptyList())

        val collectionProto = Access.CollectionResponse.newBuilder().setCollection(mockCollection.builder().build()).build()

        `when`(api.getCollectionByID(any())).thenReturn(collectionProto)

        when (val result = flowAccessApi.getCollectionById(collectionId)) {
            is FlowAccessApi.FlowResult.Success -> assertEquals(mockCollection, result.data)
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to get collection by ID: ${result.message}", result.throwable)
        }
    }

    @Test
    fun `Test sendTransaction`() {
        val mockTransaction = FlowTransaction(
            FlowScript("script"),
            emptyList(),
            FlowId.of("01".toByteArray()),
            123L,
            FlowTransactionProposalKey(FlowAddress("02"), 1, 123L),
            FlowAddress("02"),
            emptyList()
        )

        val transactionProto = Access.SendTransactionResponse.newBuilder()
            .setId(ByteString.copyFromUtf8("01"))
            .build()

        `when`(api.sendTransaction(any())).thenReturn(transactionProto)

        when (val result = flowAccessApi.sendTransaction(mockTransaction)) {
            is FlowAccessApi.FlowResult.Success -> assertEquals(FlowId.of("01".toByteArray()), result.data)
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to send transaction: ${result.message}", result.throwable)
        }
    }

    @Test
    fun `Test getTransactionById`() {
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
        val transactionProto = Access.TransactionResponse.newBuilder()
            .setTransaction(flowTransaction.builder().build())
            .build()

        `when`(api.getTransaction(any())).thenReturn(transactionProto)

        when (val result = flowAccessApi.getTransactionById(flowId)) {
            is FlowAccessApi.FlowResult.Success -> assertEquals(flowTransaction, result.data)
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to get transaction by ID: ${result.message}", result.throwable)
        }
    }

    @Test
    fun `Test getTransactionResultById`() {
        val flowId = FlowId.of("id".toByteArray())
        val flowTransactionResult = FlowTransactionResult(FlowTransactionStatus.SEALED, 1, "message", emptyList())

        val response = Access.TransactionResultResponse.newBuilder()
            .setStatus(TransactionOuterClass.TransactionStatus.SEALED)
            .setStatusCode(1)
            .setErrorMessage("message")
            .setBlockId(ByteString.copyFromUtf8("id"))
            .build()

        `when`(api.getTransactionResult(any())).thenReturn(response)

        when (val result = flowAccessApi.getTransactionResultById(flowId)) {
            is FlowAccessApi.FlowResult.Success -> assertEquals(flowTransactionResult, result.data)
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to get transaction result by ID: ${result.message}", result.throwable)
        }
    }

    @Test
    fun `Test getAccountByAddress`() {
        val flowAddress = FlowAddress("01")
        val flowAccount = FlowAccount(flowAddress, BigDecimal.ONE, FlowCode("code".toByteArray()), emptyList(), emptyMap())

        val accountProto = Access.GetAccountResponse.newBuilder()
            .setAccount(flowAccount.builder().build())
            .build()

        `when`(api.getAccount(any())).thenReturn(accountProto)

        when (val result = flowAccessApi.getAccountByAddress(flowAddress)) {
            is FlowAccessApi.FlowResult.Success -> {
                val retrievedAccount = result.data
                assertEquals(flowAccount.address, retrievedAccount.address)
                assertEquals(flowAccount.balance.stripTrailingZeros(), retrievedAccount.balance.stripTrailingZeros())
                assertEquals(flowAccount.keys, retrievedAccount.keys)
                assertEquals(flowAccount.contracts, retrievedAccount.contracts)
            }
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to get account by address: ${result.message}", result.throwable)
        }
    }

    @Test
    fun `Test getAccountAtLatestBlock`() {
        val flowAddress = FlowAddress("01")
        val flowAccount = FlowAccount(flowAddress, BigDecimal.ONE, FlowCode("code".toByteArray()), emptyList(), emptyMap())

        val accountProto = Access.AccountResponse.newBuilder()
            .setAccount(flowAccount.builder().build())
            .build()

        `when`(api.getAccountAtLatestBlock(any())).thenReturn(accountProto)

        when (val result = flowAccessApi.getAccountAtLatestBlock(flowAddress)) {
            is FlowAccessApi.FlowResult.Success -> {
                val retrievedAccount = result.data
                assertEquals(flowAccount.address, retrievedAccount.address)
                assertEquals(flowAccount.balance.stripTrailingZeros(), retrievedAccount.balance.stripTrailingZeros())
                assertEquals(flowAccount.keys, retrievedAccount.keys)
                assertEquals(flowAccount.contracts, retrievedAccount.contracts)
            }
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to get account at latest block: ${result.message}", result.throwable)
        }
    }

    @Test
    fun `Test getAccountByBlockHeight`() {
        val flowAddress = FlowAddress("01")
        val flowAccount = FlowAccount(flowAddress, BigDecimal.ONE, FlowCode("code".toByteArray()), emptyList(), emptyMap())

        val height = 123L

        val accountProto = Access.AccountResponse.newBuilder()
            .setAccount(flowAccount.builder().build())
            .build()

        `when`(api.getAccountAtBlockHeight(any())).thenReturn(accountProto)

        when (val result = flowAccessApi.getAccountByBlockHeight(flowAddress, height)) {
            is FlowAccessApi.FlowResult.Success -> {
                val retrievedAccount = result.data
                assertEquals(flowAccount.address, retrievedAccount.address)
                assertEquals(flowAccount.balance.stripTrailingZeros(), retrievedAccount.balance.stripTrailingZeros())
                assertEquals(flowAccount.keys, retrievedAccount.keys)
                assertEquals(flowAccount.contracts, retrievedAccount.contracts)
            }
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to get account by block height: ${result.message}", result.throwable)
        }
    }

    @Test
    fun `Test executeScriptAtLatestBlock`() {
        val script = FlowScript("script".toByteArray())
        val arguments = listOf(ByteString.copyFromUtf8("argument1"), ByteString.copyFromUtf8("argument2"))

        val response = Access.ExecuteScriptResponse.newBuilder()
            .setValue(ByteString.copyFromUtf8("response_value"))
            .build()

        `when`(api.executeScriptAtLatestBlock(any())).thenReturn(response)

        val result = flowAccessApi.executeScriptAtLatestBlock(script, arguments)

        verify(api).executeScriptAtLatestBlock(
            Access.ExecuteScriptAtLatestBlockRequest.newBuilder()
                .setScript(script.byteStringValue)
                .addAllArguments(arguments)
                .build()
        )

        when (result) {
            is FlowAccessApi.FlowResult.Success -> assertEquals("response_value", result.data.stringValue)
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to execute script at latest block: ${result.message}", result.throwable)
        }
    }

    @Test
    fun `Test executeScriptAtBlockId`() {
        val script = FlowScript("some_script")
        val blockId = FlowId("01")
        val arguments = listOf(ByteString.copyFromUtf8("argument1"), ByteString.copyFromUtf8("argument2"))

        val response = Access.ExecuteScriptResponse.newBuilder()
            .setValue(ByteString.copyFromUtf8("response_value"))
            .build()

        `when`(api.executeScriptAtBlockID(any())).thenReturn(response)

        val result = flowAccessApi.executeScriptAtBlockId(script, blockId, arguments)

        verify(api).executeScriptAtBlockID(
            Access.ExecuteScriptAtBlockIDRequest.newBuilder()
                .setBlockId(blockId.byteStringValue)
                .setScript(script.byteStringValue)
                .addAllArguments(arguments)
                .build()
        )

        when (result) {
            is FlowAccessApi.FlowResult.Success -> assertEquals("response_value", result.data.stringValue)
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to execute script at block ID: ${result.message}", result.throwable)
        }
    }

    @Test
    fun `Test executeScriptAtBlockHeight`() {
        val script = FlowScript("some_script")
        val height = 123L
        val arguments = listOf(ByteString.copyFromUtf8("argument1"), ByteString.copyFromUtf8("argument2"))

        val response = Access.ExecuteScriptResponse.newBuilder()
            .setValue(ByteString.copyFromUtf8("response_value"))
            .build()

        `when`(api.executeScriptAtBlockHeight(any())).thenReturn(response)

        val result = flowAccessApi.executeScriptAtBlockHeight(script, height, arguments)

        verify(api).executeScriptAtBlockHeight(
            Access.ExecuteScriptAtBlockHeightRequest.newBuilder()
                .setBlockHeight(height)
                .setScript(script.byteStringValue)
                .addAllArguments(arguments)
                .build()
        )

        when (result) {
            is FlowAccessApi.FlowResult.Success -> assertEquals("response_value", result.data.stringValue)
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to execute script at block height: ${result.message}", result.throwable)
        }
    }

    @Test
    fun `Test getEventsForHeightRange`() {
        val type = "event_type"
        val range = 1L..10L

        val eventResult1 = Access.EventsResponse.Result.newBuilder().build()
        val eventResult2 = Access.EventsResponse.Result.newBuilder().build()
        val response = Access.EventsResponse.newBuilder()
            .addResults(eventResult1)
            .addResults(eventResult2)
            .build()

        `when`(api.getEventsForHeightRange(any())).thenReturn(response)

        val result = flowAccessApi.getEventsForHeightRange(type, range)

        verify(api).getEventsForHeightRange(
            Access.GetEventsForHeightRangeRequest.newBuilder()
                .setType(type)
                .setStartHeight(range.first)
                .setEndHeight(range.last)
                .build()
        )

        when (result) {
            is FlowAccessApi.FlowResult.Success -> assertEquals(2, result.data.size)
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to get events for height range: ${result.message}", result.throwable)
        }
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

        `when`(api.getEventsForBlockIDs(any())).thenReturn(response)

        when (val result = flowAccessApi.getEventsForBlockIds(type, blockIds)) {
            is FlowAccessApi.FlowResult.Success -> assertEquals(2, result.data.size)
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to get events for block IDs: ${result.message}", result.throwable)
        }
    }

    @Test
    fun `Test getNetworkParameters`() {
        val mockFlowChainId = FlowChainId.of("test_chain_id")

        val response = Access.GetNetworkParametersResponse.newBuilder()
            .setChainId("test_chain_id")
            .build()

        `when`(api.getNetworkParameters(Access.GetNetworkParametersRequest.newBuilder().build()))
            .thenReturn(response)

        when (val result = flowAccessApi.getNetworkParameters()) {
            is FlowAccessApi.FlowResult.Success -> assertEquals(mockFlowChainId, result.data)
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to get network parameters: ${result.message}", result.throwable)
        }
    }

    @Test
    fun `Test getLatestProtocolStateSnapshot`() {
        val mockFlowSnapshot = FlowSnapshot("test_serialized_snapshot".toByteArray())

        val response = Access.ProtocolStateSnapshotResponse.newBuilder()
            .setSerializedSnapshot(ByteString.copyFromUtf8("test_serialized_snapshot"))
            .build()

        `when`(api.getLatestProtocolStateSnapshot(Access.GetLatestProtocolStateSnapshotRequest.newBuilder().build()))
            .thenReturn(response)

        when (val result = flowAccessApi.getLatestProtocolStateSnapshot()) {
            is FlowAccessApi.FlowResult.Success -> assertEquals(mockFlowSnapshot, result.data)
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to get latest protocol state snapshot: ${result.message}", result.throwable)
        }
    }

    @Test
    fun `Test getTransactionsByBlockId`() {
        val blockId = FlowId("01")
        val transactions = listOf(FlowTransaction.of(TransactionOuterClass.Transaction.getDefaultInstance()))

        val response = Access.TransactionsResponse.newBuilder()
            .addAllTransactions(transactions.map { it.builder().build() })
            .build()

        `when`(api.getTransactionsByBlockID(any())).thenReturn(response)

        when (val result = flowAccessApi.getTransactionsByBlockId(blockId)) {
            is FlowAccessApi.FlowResult.Success -> assertEquals(transactions, result.data)
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to get transactions by block ID: ${result.message}", result.throwable)
        }
    }

    @Test
    fun `Test getTransactionsByBlockId with multiple results`() {
        val blockId = FlowId("01")

        val transaction1 = FlowTransaction.of(TransactionOuterClass.Transaction.getDefaultInstance())
        val transaction2 = FlowTransaction.of(TransactionOuterClass.Transaction.newBuilder().setReferenceBlockId(ByteString.copyFromUtf8("02")).build())

        val transactions = listOf(transaction1, transaction2)

        val response = Access.TransactionsResponse.newBuilder()
            .addAllTransactions(transactions.map { it.builder().build() })
            .build()

        `when`(api.getTransactionsByBlockID(any())).thenReturn(response)

        when (val result = flowAccessApi.getTransactionsByBlockId(blockId)) {
            is FlowAccessApi.FlowResult.Success -> {
                assertEquals(2, result.data.size)
                assertEquals(transaction1, result.data[0])
                assertEquals(transaction2, result.data[1])
            }
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to get transactions by block ID: ${result.message}", result.throwable)
        }
    }

    @Test
    fun `Test getTransactionResultsByBlockId`() {
        val blockId = FlowId("01")
        val transactionResults = listOf(FlowTransactionResult.of(Access.TransactionResultResponse.getDefaultInstance()))

        val response = Access.TransactionResultsResponse.newBuilder()
            .addAllTransactionResults(transactionResults.map { it.builder().build() })
            .build()

        `when`(api.getTransactionResultsByBlockID(any())).thenReturn(response)

        when (val result = flowAccessApi.getTransactionResultsByBlockId(blockId)) {
            is FlowAccessApi.FlowResult.Success -> assertEquals(transactionResults, result.data)
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to get transaction results by block ID: ${result.message}", result.throwable)
        }
    }

    @Test
    fun `Test getTransactionResultsByBlockId with multiple results`() {
        val blockId = FlowId("01")

        val transactionResult1 = FlowTransactionResult.of(
            Access.TransactionResultResponse.newBuilder()
                .setStatus(TransactionOuterClass.TransactionStatus.SEALED)
                .setStatusCode(1)
                .setErrorMessage("message1")
                .build()
        )

        val transactionResult2 = FlowTransactionResult.of(
            Access.TransactionResultResponse.newBuilder()
                .setStatus(TransactionOuterClass.TransactionStatus.SEALED)
                .setStatusCode(2)
                .setErrorMessage("message2")
                .build()
        )

        val transactionResults = listOf(transactionResult1, transactionResult2)

        val response = Access.TransactionResultsResponse.newBuilder()
            .addAllTransactionResults(transactionResults.map { it.builder().build() })
            .build()

        `when`(api.getTransactionResultsByBlockID(any())).thenReturn(response)

        when (val result = flowAccessApi.getTransactionResultsByBlockId(blockId)) {
            is FlowAccessApi.FlowResult.Success -> {
                assertEquals(2, result.data.size)
                assertEquals(transactionResult1, result.data[0])
                assertEquals(transactionResult2, result.data[1])
            }
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to get transaction results by block ID: ${result.message}", result.throwable)
        }
    }

    @Test
    fun `Test getExecutionResultByBlockId`() {
        val blockId = FlowId("01")
        val executionResult = ExecutionResult(FlowId("01"), FlowId("02"))

        val response = Access.ExecutionResultByIDResponse.newBuilder()
            .setExecutionResult(
                ExecutionResultOuterClass.ExecutionResult.newBuilder()
                    .setBlockId(blockId.byteStringValue)
                    .setPreviousResultId((FlowId("02").byteStringValue))
                    .build()
            )
            .build()

        `when`(api.getExecutionResultByID(any())).thenReturn(response)

        when (val result = flowAccessApi.getExecutionResultByBlockId(blockId)) {
            is FlowAccessApi.FlowResult.Success -> assertEquals(executionResult, result.data)
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to get execution result by block ID: ${result.message}", result.throwable)
        }
    }
}
