package org.onflow.flow.sdk

import com.google.protobuf.ByteString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.onflow.flow.sdk.impl.AsyncFlowAccessApiImplTest.Companion.HEIGHT
import org.onflow.flow.sdk.impl.AsyncFlowAccessApiImplTest.Companion.blockId
import org.onflow.flow.sdk.impl.AsyncFlowAccessApiImplTest.Companion.mockAccountKey
import org.onflow.flow.sdk.impl.AsyncFlowAccessApiImplTest.Companion.mockAccountKeys
import org.onflow.flow.sdk.impl.AsyncFlowAccessApiImplTest.Companion.mockBlock
import org.onflow.flow.sdk.impl.AsyncFlowAccessApiImplTest.Companion.mockBlockHeader
import org.onflow.flow.sdk.impl.AsyncFlowAccessApiImplTest.Companion.testException
import org.onflow.flow.sdk.impl.FlowAccessApiImplTest.Companion.createMockAccount
import org.onflow.flow.sdk.impl.FlowAccessApiImplTest.Companion.createMockTransaction
import org.onflow.protobuf.access.Access
import org.onflow.protobuf.entities.BlockExecutionDataOuterClass
import org.onflow.protobuf.entities.EventOuterClass

class FlowAccessApiTest {
    private lateinit var flowAccessApi: FlowAccessApi
    private val address = FlowAddress("01")
    private val keyIndex = 0
    private val height = HEIGHT
    private val expectedBalance = 1000L
    private val account = createMockAccount(address)
    private val transaction = createMockTransaction()

    @BeforeEach
    fun setUp() {
        flowAccessApi = mock(FlowAccessApi::class.java)
    }

    @Test
    fun `Test getAccountKeyAtLatestBlock`() {
        `when`(flowAccessApi.getAccountKeyAtLatestBlock(address, keyIndex)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(mockAccountKey))

        val result = flowAccessApi.getAccountKeyAtLatestBlock(address, keyIndex)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(mockAccountKey), result)
        verify(flowAccessApi).getAccountKeyAtLatestBlock(address, keyIndex)
    }

    @Test
    fun `Test getAccountKeyAtBlockHeight`() {
        `when`(flowAccessApi.getAccountKeyAtBlockHeight(address, keyIndex, height)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(mockAccountKey))

        val result = flowAccessApi.getAccountKeyAtBlockHeight(address, keyIndex, height)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(mockAccountKey), result)
        verify(flowAccessApi).getAccountKeyAtBlockHeight(address, keyIndex, height)
    }

    @Test
    fun `Test getAccountKeysAtLatestBlock`() {
        `when`(flowAccessApi.getAccountKeysAtLatestBlock(address)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(mockAccountKeys))

        val result = flowAccessApi.getAccountKeysAtLatestBlock(address)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(mockAccountKeys), result)
        verify(flowAccessApi).getAccountKeysAtLatestBlock(address)
    }

    @Test
    fun `Test getAccountKeysAtBlockHeight`() {
        `when`(flowAccessApi.getAccountKeysAtBlockHeight(address, height)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(mockAccountKeys))

        val result = flowAccessApi.getAccountKeysAtBlockHeight(address, height)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(mockAccountKeys), result)
        verify(flowAccessApi).getAccountKeysAtBlockHeight(address, height)
    }

    @Test
    fun `Test getLatestBlockHeader`() {
        `when`(flowAccessApi.getLatestBlockHeader(true)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(mockBlockHeader))

        val result = flowAccessApi.getLatestBlockHeader()

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(mockBlockHeader), result)
    }

    @Test
    fun `Test getBlockHeaderById`() {
        `when`(flowAccessApi.getBlockHeaderById(blockId)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(mockBlockHeader))

        val result = flowAccessApi.getBlockHeaderById(blockId)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(mockBlockHeader), result)
    }

    @Test
    fun `Test getBlockHeaderByHeight`() {
        `when`(flowAccessApi.getBlockHeaderByHeight(height)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(mockBlockHeader))

        val result = flowAccessApi.getBlockHeaderByHeight(height)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(mockBlockHeader), result)
    }

    @Test
    fun `Test getLatestBlock`() {
        `when`(flowAccessApi.getLatestBlock(true)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(mockBlock))

        val result = flowAccessApi.getLatestBlock()

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(mockBlock), result)
    }

    @Test
    fun `Test getBlockById`() {
        `when`(flowAccessApi.getBlockById(blockId)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(mockBlock))

        val result = flowAccessApi.getBlockById(blockId)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(mockBlock), result)
    }

    @Test
    fun `Test getBlockByHeight`() {
        `when`(flowAccessApi.getBlockByHeight(height)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(mockBlock))

        val result = flowAccessApi.getBlockByHeight(height)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(mockBlock), result)
    }

    @Test
    fun `Test getAccountBalanceAtLatestBlock success`() {
        val response = FlowAccessApi.AccessApiCallResponse.Success(expectedBalance)

        `when`(flowAccessApi.getAccountBalanceAtLatestBlock(address)).thenReturn(response)

        val result = flowAccessApi.getAccountBalanceAtLatestBlock(address)

        assertEquals(response, result)
        verify(flowAccessApi).getAccountBalanceAtLatestBlock(address)
    }

    @Test
    fun `Test getAccountBalanceAtLatestBlock failure`() {
        val response = FlowAccessApi.AccessApiCallResponse.Error("Failed to get account balance at latest block", testException)

        `when`(flowAccessApi.getAccountBalanceAtLatestBlock(address)).thenReturn(response)

        val result = flowAccessApi.getAccountBalanceAtLatestBlock(address)

        assertEquals(response, result)
        verify(flowAccessApi).getAccountBalanceAtLatestBlock(address)
    }

    @Test
    fun `Test getAccountBalanceAtBlockHeight success`() {
        val response = FlowAccessApi.AccessApiCallResponse.Success(expectedBalance)

        `when`(flowAccessApi.getAccountBalanceAtBlockHeight(address, height)).thenReturn(response)

        val result = flowAccessApi.getAccountBalanceAtBlockHeight(address, height)

        assertEquals(response, result)
        verify(flowAccessApi).getAccountBalanceAtBlockHeight(address, height)
    }

    @Test
    fun `Test getAccountBalanceAtBlockHeight failure`() {
        val response = FlowAccessApi.AccessApiCallResponse.Error("Failed to get account balance at block height", testException)

        `when`(flowAccessApi.getAccountBalanceAtBlockHeight(address, height)).thenReturn(response)

        val result = flowAccessApi.getAccountBalanceAtBlockHeight(address, height)

        assertEquals(response, result)
        verify(flowAccessApi).getAccountBalanceAtBlockHeight(address, height)
    }

    @Test
    fun `Test getCollectionById`() {
        val flowId = blockId
        val flowCollection = FlowCollection(flowId, emptyList())
        `when`(flowAccessApi.getCollectionById(flowId)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(flowCollection))

        val result = flowAccessApi.getCollectionById(flowId)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(flowCollection), result)
    }

    @Test
    fun `Test getFullCollectionById`() {
        val flowId = blockId
        `when`(flowAccessApi.getFullCollectionById(flowId)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(listOf(transaction)))

        val result = flowAccessApi.getFullCollectionById(flowId)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(listOf(transaction)), result)
    }

    @Test
    fun `Test sendTransaction`() {
        val flowId = blockId
        `when`(flowAccessApi.sendTransaction(transaction)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(flowId))

        val result = flowAccessApi.sendTransaction(transaction)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(flowId), result)
    }

    @Test
    fun `Test getTransactionById`() {
        val flowId = blockId
        `when`(flowAccessApi.getTransactionById(flowId)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(transaction))

        val result = flowAccessApi.getTransactionById(flowId)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(transaction), result)
    }

    @Test
    fun `Test getTransactionResultById`() {
        val flowId = blockId
        val flowTransactionResult = FlowTransactionResult.of(Access.TransactionResultResponse.getDefaultInstance())
        `when`(flowAccessApi.getTransactionResultById(flowId)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(flowTransactionResult))

        val result = flowAccessApi.getTransactionResultById(flowId)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(flowTransactionResult), result)
    }

    @Test
    fun `Test getSystemTransaction`() {
        val flowId = blockId
        `when`(flowAccessApi.getSystemTransaction(flowId)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(transaction))

        val result = flowAccessApi.getSystemTransaction(flowId)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(transaction), result)
    }

    @Test
    fun `Test getSystemTransactionResult`() {
        val flowId = blockId
        val flowTransactionResult = FlowTransactionResult.of(Access.TransactionResultResponse.getDefaultInstance())
        `when`(flowAccessApi.getSystemTransactionResult(flowId)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(flowTransactionResult))

        val result = flowAccessApi.getSystemTransactionResult(flowId)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(flowTransactionResult), result)
    }

    @Test
    fun `Test getTransactionResultByIndex`() {
        val flowId = blockId
        val flowTransactionResult = FlowTransactionResult.of(Access.TransactionResultResponse.getDefaultInstance())
        `when`(flowAccessApi.getTransactionResultByIndex(flowId, keyIndex)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(flowTransactionResult))

        val result = flowAccessApi.getTransactionResultByIndex(flowId, keyIndex)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(flowTransactionResult), result)
    }

    @Test
    fun `Test getAccountAtLatestBlock`() {
        `when`(flowAccessApi.getAccountAtLatestBlock(address)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(account))

        val result = flowAccessApi.getAccountAtLatestBlock(address)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(account), result)
    }

    @Test
    fun `Test getAccountByBlockHeight`() {
        `when`(flowAccessApi.getAccountByBlockHeight(address, height)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(account))

        val result = flowAccessApi.getAccountByBlockHeight(address, height)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(account), result)
    }

    @Test
    fun `Test executeScriptAtLatestBlock`() {
        val script = FlowScript("script")
        val arguments = listOf(ByteString.copyFromUtf8("argument1"))
        val response = FlowScriptResponse("response".toByteArray())
        `when`(flowAccessApi.executeScriptAtLatestBlock(script, arguments)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(response))

        val result = flowAccessApi.executeScriptAtLatestBlock(script, arguments)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(response), result)
    }

    @Test
    fun `Test executeScriptAtBlockId`() {
        val script = FlowScript("script")
        val arguments = listOf(ByteString.copyFromUtf8("argument1"))
        val response = FlowScriptResponse("response".toByteArray())
        `when`(flowAccessApi.executeScriptAtBlockId(script, blockId, arguments)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(response))

        val result = flowAccessApi.executeScriptAtBlockId(script, blockId, arguments)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(response), result)
    }

    @Test
    fun `Test executeScriptAtBlockHeight`() {
        val script = FlowScript("script")
        val arguments = listOf(ByteString.copyFromUtf8("argument1"))
        val response = FlowScriptResponse("response".toByteArray())
        `when`(flowAccessApi.executeScriptAtBlockHeight(script, height, arguments)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(response))

        val result = flowAccessApi.executeScriptAtBlockHeight(script, height, arguments)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(response), result)
    }

    @Test
    fun `Test getEventsForHeightRange`() {
        val type = "eventType"
        val range = 100L..200L
        val eventResults = listOf(FlowEventResult.of(Access.EventsResponse.Result.getDefaultInstance()), FlowEventResult.of(Access.EventsResponse.Result.getDefaultInstance()))
        `when`(flowAccessApi.getEventsForHeightRange(type, range)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(eventResults))

        val result = flowAccessApi.getEventsForHeightRange(type, range)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(eventResults), result)
    }

    @Test
    fun `Test getEventsForBlockIds`() {
        val type = "eventType"
        val ids = setOf(FlowId("01"), FlowId("02"))
        val eventResults = listOf(FlowEventResult.of(Access.EventsResponse.Result.getDefaultInstance()), FlowEventResult.of(Access.EventsResponse.Result.getDefaultInstance()))
        `when`(flowAccessApi.getEventsForBlockIds(type, ids)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(eventResults))

        val result = flowAccessApi.getEventsForBlockIds(type, ids)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(eventResults), result)
    }

    @Test
    fun `Test getNetworkParameters`() {
        val chainId = FlowChainId.TESTNET
        `when`(flowAccessApi.getNetworkParameters()).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(chainId))

        val result = flowAccessApi.getNetworkParameters()

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(chainId), result)
    }

    @Test
    fun `Test getLatestProtocolStateSnapshot`() {
        val snapshot = FlowSnapshot("snapshot".toByteArray())
        `when`(flowAccessApi.getLatestProtocolStateSnapshot()).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(snapshot))

        val result = flowAccessApi.getLatestProtocolStateSnapshot()

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(snapshot), result)
    }

    @Test
    fun `Test getProtocolStateSnapshotByBlockId`() {
        val snapshot = FlowSnapshot("snapshot".toByteArray())
        `when`(flowAccessApi.getProtocolStateSnapshotByBlockId(blockId)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(snapshot))

        val result = flowAccessApi.getProtocolStateSnapshotByBlockId(blockId)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(snapshot), result)
    }

    @Test
    fun `Test getProtocolStateSnapshotByHeight`() {
        val snapshot = FlowSnapshot("snapshot".toByteArray())
        `when`(flowAccessApi.getProtocolStateSnapshotByHeight(HEIGHT)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(snapshot))

        val result = flowAccessApi.getProtocolStateSnapshotByHeight(HEIGHT)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(snapshot), result)
    }

    @Test
    fun `Test getTransactionsByBlockId`() {
        val transactions = listOf(transaction)
        `when`(flowAccessApi.getTransactionsByBlockId(blockId)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(transactions))

        val result = flowAccessApi.getTransactionsByBlockId(blockId)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(transactions), result)
    }

    @Test
    fun `Test getTransactionsByBlockId with multiple results`() {
        val transaction1 = FlowTransaction(FlowScript("script1"), emptyList(), FlowId.of("01".toByteArray()), 123L, FlowTransactionProposalKey(FlowAddress("02"), 1, 123L), FlowAddress("02"), emptyList())

        val transaction2 = FlowTransaction(FlowScript("script2"), emptyList(), FlowId.of("02".toByteArray()), 456L, FlowTransactionProposalKey(FlowAddress("03"), 2, 456L), FlowAddress("03"), emptyList())

        val transactions = listOf(transaction1, transaction2)

        `when`(flowAccessApi.getTransactionsByBlockId(blockId)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(transactions))

        val result = flowAccessApi.getTransactionsByBlockId(blockId)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(transactions), result)

        assertEquals(2, transactions.size)
        assertEquals(transaction1, transactions[0])
        assertEquals(transaction2, transactions[1])
    }

    @Test
    fun `Test getTransactionResultsByBlockId`() {
        val transactionResults = listOf(FlowTransactionResult.of(Access.TransactionResultResponse.getDefaultInstance()))
        `when`(flowAccessApi.getTransactionResultsByBlockId(blockId)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(transactionResults))

        val result = flowAccessApi.getTransactionResultsByBlockId(blockId)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(transactionResults), result)
    }

    @Test
    fun `Test getTransactionResultsByBlockId with multiple results`() {
        val flowId = blockId

        val transactionResult1 = FlowTransactionResult(
            FlowTransactionStatus.SEALED,
            1,
            "message1",
            emptyList(),
            flowId,
            1L,
            flowId,
            flowId,
            1
        )

        val transactionResult2 = FlowTransactionResult(
            FlowTransactionStatus.SEALED,
            2,
            "message2",
            emptyList(),
            flowId,
            1L,
            flowId,
            flowId,
            1
        )

        val transactions = listOf(transactionResult1, transactionResult2)

        `when`(flowAccessApi.getTransactionResultsByBlockId(flowId)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(transactions))

        val result = flowAccessApi.getTransactionResultsByBlockId(flowId)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(transactions), result)

        assertEquals(
            2,
            FlowAccessApi.AccessApiCallResponse
                .Success(transactions)
                .data
                .size
        )
        assertEquals(transactionResult1, FlowAccessApi.AccessApiCallResponse.Success(transactions).data[0])
        assertEquals(transactionResult2, FlowAccessApi.AccessApiCallResponse.Success(transactions).data[1])
    }

    @Test
    fun `Test getExecutionResultByBlockId`() {
        val executionResult = FlowExecutionResult.of(Access.ExecutionResultByIDResponse.getDefaultInstance())
        `when`(flowAccessApi.getExecutionResultByBlockId(blockId)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(executionResult))

        val result = flowAccessApi.getExecutionResultByBlockId(blockId)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(executionResult), result)
    }

    @Test
    fun `Test subscribeExecutionDataByBlockId`(): Unit = runBlocking {
        val scope = this
        val responseChannel = Channel<FlowBlockExecutionData>(Channel.UNLIMITED)
        val errorChannel = Channel<Throwable>(Channel.UNLIMITED)
        val job = Job()

        `when`(flowAccessApi.subscribeExecutionDataByBlockId(scope, blockId)).thenReturn(Triple(responseChannel, errorChannel, job))

        val result = flowAccessApi.subscribeExecutionDataByBlockId(scope, blockId)
        val expectedExecutionData = FlowBlockExecutionData.of(BlockExecutionDataOuterClass.BlockExecutionData.getDefaultInstance())

        responseChannel.send(expectedExecutionData)
        responseChannel.close()

        verify(flowAccessApi).subscribeExecutionDataByBlockId(scope, blockId)

        launch {
            result.first.consumeEach { executionData ->
                assertEquals(expectedExecutionData, executionData)
            }
        }

        launch {
            result.second.consumeEach { error ->
                throw error
            }
        }

        errorChannel.close()
    }

    @Test
    fun `Test subscribeExecutionDataByBlockHeight`(): Unit = runBlocking {
        val blockHeight = 100L
        val scope = this
        val responseChannel = Channel<FlowBlockExecutionData>(Channel.UNLIMITED)
        val errorChannel = Channel<Throwable>(Channel.UNLIMITED)
        val job = Job()

        `when`(flowAccessApi.subscribeExecutionDataByBlockHeight(scope, blockHeight)).thenReturn(Triple(responseChannel, errorChannel, job))

        val result = flowAccessApi.subscribeExecutionDataByBlockHeight(scope, blockHeight)
        val expectedExecutionData = FlowBlockExecutionData.of(BlockExecutionDataOuterClass.BlockExecutionData.getDefaultInstance())

        responseChannel.send(expectedExecutionData)
        responseChannel.close()

        verify(flowAccessApi).subscribeExecutionDataByBlockHeight(scope, blockHeight)

        launch {
            result.first.consumeEach { executionData ->
                assertEquals(expectedExecutionData, executionData)
            }
        }

        launch {
            result.second.consumeEach { error ->
                throw error
            }
        }

        errorChannel.close()
    }

    @Test
    fun `Test subscribeEventsByBlockId`(): Unit = runBlocking {
        val scope = this
        val responseChannel = Channel<List<FlowEvent>>(Channel.UNLIMITED)
        val errorChannel = Channel<Throwable>(Channel.UNLIMITED)
        val job = Job()

        `when`(flowAccessApi.subscribeEventsByBlockId(scope, blockId)).thenReturn(Triple(responseChannel, errorChannel, job))

        val result = flowAccessApi.subscribeEventsByBlockId(scope, blockId)
        val expectedEvents = listOf(FlowEvent.of(EventOuterClass.Event.getDefaultInstance()))

        responseChannel.send(expectedEvents)
        responseChannel.close()

        verify(flowAccessApi).subscribeEventsByBlockId(scope, blockId)

        launch {
            result.first.consumeEach { events ->
                assertEquals(expectedEvents, events)
            }
        }

        launch {
            result.second.consumeEach { error ->
                throw error
            }
        }

        errorChannel.close()
    }

    @Test
    fun `Test subscribeEventsByBlockHeight`(): Unit = runBlocking {
        val blockHeight = 100L
        val scope = this
        val responseChannel = Channel<List<FlowEvent>>(Channel.UNLIMITED)
        val errorChannel = Channel<Throwable>(Channel.UNLIMITED)
        val job = Job()

        `when`(flowAccessApi.subscribeEventsByBlockHeight(scope, blockHeight)).thenReturn(Triple(responseChannel, errorChannel, job))

        val result = flowAccessApi.subscribeEventsByBlockHeight(scope, blockHeight)
        val expectedEvents = listOf(FlowEvent.of(EventOuterClass.Event.getDefaultInstance()))

        responseChannel.send(expectedEvents)
        responseChannel.close()

        verify(flowAccessApi).subscribeEventsByBlockHeight(scope, blockHeight)

        launch {
            result.first.consumeEach { events ->
                assertEquals(expectedEvents, events)
            }
        }

        launch {
            result.second.consumeEach { error ->
                throw error
            }
        }

        errorChannel.close()
    }
}
