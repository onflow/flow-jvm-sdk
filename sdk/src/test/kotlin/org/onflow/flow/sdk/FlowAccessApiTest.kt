package org.onflow.flow.sdk

import com.google.protobuf.ByteString
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.onflow.protobuf.access.Access
import org.onflow.protobuf.entities.AccountOuterClass
import org.onflow.protobuf.entities.BlockExecutionDataOuterClass
import org.onflow.protobuf.entities.BlockHeaderOuterClass
import org.onflow.protobuf.entities.BlockOuterClass
import org.onflow.protobuf.entities.EventOuterClass
import org.onflow.protobuf.entities.TransactionOuterClass

class FlowAccessApiTest {
    @Test
    fun `Test getAccountKeyAtLatestBlock`() {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val address = FlowAddress("01")
        val keyIndex = 0
        val accountKey = FlowAccountKey.of(AccountOuterClass.AccountKey.getDefaultInstance())

        `when`(flowAccessApi.getAccountKeyAtLatestBlock(address, keyIndex)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(accountKey))

        val result = flowAccessApi.getAccountKeyAtLatestBlock(address, keyIndex)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(accountKey), result)
        verify(flowAccessApi).getAccountKeyAtLatestBlock(address, keyIndex)
    }

    @Test
    fun `Test getAccountKeyAtBlockHeight`() {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val address = FlowAddress("01")
        val keyIndex = 0
        val height = 123L
        val accountKey = FlowAccountKey.of(AccountOuterClass.AccountKey.getDefaultInstance())

        `when`(flowAccessApi.getAccountKeyAtBlockHeight(address, keyIndex, height)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(accountKey))

        val result = flowAccessApi.getAccountKeyAtBlockHeight(address, keyIndex, height)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(accountKey), result)
        verify(flowAccessApi).getAccountKeyAtBlockHeight(address, keyIndex, height)
    }

    @Test
    fun `Test getAccountKeysAtLatestBlock`() {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val address = FlowAddress("01")
        val accountKeys = listOf(
            FlowAccountKey.of(AccountOuterClass.AccountKey.getDefaultInstance()),
            FlowAccountKey.of(AccountOuterClass.AccountKey.getDefaultInstance())
        )

        `when`(flowAccessApi.getAccountKeysAtLatestBlock(address)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(accountKeys))

        val result = flowAccessApi.getAccountKeysAtLatestBlock(address)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(accountKeys), result)
        verify(flowAccessApi).getAccountKeysAtLatestBlock(address)
    }

    @Test
    fun `Test getAccountKeysAtBlockHeight`() {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val address = FlowAddress("01")
        val height = 123L
        val accountKeys = listOf(
            FlowAccountKey.of(AccountOuterClass.AccountKey.getDefaultInstance()),
            FlowAccountKey.of(AccountOuterClass.AccountKey.getDefaultInstance())
        )

        `when`(flowAccessApi.getAccountKeysAtBlockHeight(address, height)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(accountKeys))

        val result = flowAccessApi.getAccountKeysAtBlockHeight(address, height)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(accountKeys), result)
        verify(flowAccessApi).getAccountKeysAtBlockHeight(address, height)
    }

    @Test
    fun `Test getLatestBlockHeader`() {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val latestBlockHeader = FlowBlockHeader.of(BlockHeaderOuterClass.BlockHeader.getDefaultInstance())
        `when`(flowAccessApi.getLatestBlockHeader(true)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(latestBlockHeader))

        val result = flowAccessApi.getLatestBlockHeader()

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(latestBlockHeader), result)
    }

    @Test
    fun `Test getBlockHeaderById`() {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val blockId = FlowId("01")
        val blockHeader = FlowBlockHeader.of(BlockHeaderOuterClass.BlockHeader.getDefaultInstance())
        `when`(flowAccessApi.getBlockHeaderById(blockId)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(blockHeader))

        val result = flowAccessApi.getBlockHeaderById(blockId)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(blockHeader), result)
    }

    @Test
    fun `Test getBlockHeaderByHeight`() {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val height = 123L
        val blockHeader = FlowBlockHeader.of(BlockHeaderOuterClass.BlockHeader.getDefaultInstance())
        `when`(flowAccessApi.getBlockHeaderByHeight(height)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(blockHeader))

        val result = flowAccessApi.getBlockHeaderByHeight(height)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(blockHeader), result)
    }

    @Test
    fun `Test getLatestBlock`() {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val latestBlock = FlowBlock.of(BlockOuterClass.Block.getDefaultInstance())
        `when`(flowAccessApi.getLatestBlock(true)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(latestBlock))

        val result = flowAccessApi.getLatestBlock()

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(latestBlock), result)
    }

    @Test
    fun `Test getBlockById`() {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val blockId = FlowId("01")
        val block = FlowBlock.of(BlockOuterClass.Block.getDefaultInstance())
        `when`(flowAccessApi.getBlockById(blockId)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(block))

        val result = flowAccessApi.getBlockById(blockId)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(block), result)
    }

    @Test
    fun `Test getBlockByHeight`() {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val height = 123L
        val block = FlowBlock.of(BlockOuterClass.Block.getDefaultInstance())
        `when`(flowAccessApi.getBlockByHeight(height)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(block))

        val result = flowAccessApi.getBlockByHeight(height)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(block), result)
    }

    @Test
    fun `Test getAccountBalanceAtLatestBlock success`() {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val flowAddress = FlowAddress("01")
        val expectedBalance = 1000L
        val response = FlowAccessApi.AccessApiCallResponse.Success(expectedBalance)

        `when`(flowAccessApi.getAccountBalanceAtLatestBlock(flowAddress)).thenReturn(response)

        val result = flowAccessApi.getAccountBalanceAtLatestBlock(flowAddress)

        assertEquals(response, result)
        verify(flowAccessApi).getAccountBalanceAtLatestBlock(flowAddress)
    }

    @Test
    fun `Test getAccountBalanceAtLatestBlock failure`() {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val flowAddress = FlowAddress("01")
        val exception = RuntimeException("Test exception")
        val response = FlowAccessApi.AccessApiCallResponse.Error("Failed to get account balance at latest block", exception)

        `when`(flowAccessApi.getAccountBalanceAtLatestBlock(flowAddress)).thenReturn(response)

        val result = flowAccessApi.getAccountBalanceAtLatestBlock(flowAddress)

        assertEquals(response, result)
        verify(flowAccessApi).getAccountBalanceAtLatestBlock(flowAddress)
    }

    @Test
    fun `Test getAccountBalanceAtBlockHeight success`() {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val flowAddress = FlowAddress("01")
        val blockHeight = 123L
        val expectedBalance = 1000L
        val response = FlowAccessApi.AccessApiCallResponse.Success(expectedBalance)

        `when`(flowAccessApi.getAccountBalanceAtBlockHeight(flowAddress, blockHeight)).thenReturn(response)

        val result = flowAccessApi.getAccountBalanceAtBlockHeight(flowAddress, blockHeight)

        assertEquals(response, result)
        verify(flowAccessApi).getAccountBalanceAtBlockHeight(flowAddress, blockHeight)
    }

    @Test
    fun `Test getAccountBalanceAtBlockHeight failure`() {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val flowAddress = FlowAddress("01")
        val blockHeight = 123L
        val exception = RuntimeException("Test exception")
        val response = FlowAccessApi.AccessApiCallResponse.Error("Failed to get account balance at block height", exception)

        `when`(flowAccessApi.getAccountBalanceAtBlockHeight(flowAddress, blockHeight)).thenReturn(response)

        val result = flowAccessApi.getAccountBalanceAtBlockHeight(flowAddress, blockHeight)

        assertEquals(response, result)
        verify(flowAccessApi).getAccountBalanceAtBlockHeight(flowAddress, blockHeight)
    }

    @Test
    fun `Test getCollectionById`() {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val flowId = FlowId("01")
        val flowCollection = FlowCollection(flowId, emptyList())
        `when`(flowAccessApi.getCollectionById(flowId)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(flowCollection))

        val result = flowAccessApi.getCollectionById(flowId)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(flowCollection), result)
    }

    @Test
    fun `Test sendTransaction`() {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val flowTransaction = FlowTransaction.of(TransactionOuterClass.Transaction.getDefaultInstance())
        val flowId = FlowId("01")
        `when`(flowAccessApi.sendTransaction(flowTransaction)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(flowId))

        val result = flowAccessApi.sendTransaction(flowTransaction)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(flowId), result)
    }

    @Test
    fun `Test getTransactionById`() {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val flowId = FlowId("01")
        val flowTransaction = FlowTransaction.of(TransactionOuterClass.Transaction.getDefaultInstance())
        `when`(flowAccessApi.getTransactionById(flowId)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(flowTransaction))

        val result = flowAccessApi.getTransactionById(flowId)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(flowTransaction), result)
    }

    @Test
    fun `Test getTransactionResultById`() {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val flowId = FlowId("01")
        val flowTransactionResult = FlowTransactionResult.of(Access.TransactionResultResponse.getDefaultInstance())
        `when`(flowAccessApi.getTransactionResultById(flowId)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(flowTransactionResult))

        val result = flowAccessApi.getTransactionResultById(flowId)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(flowTransactionResult), result)
    }

    @Test
    fun `Test getAccountAtLatestBlock`() {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val flowAddress = FlowAddress("01")
        val flowAccount = FlowAccount.of(AccountOuterClass.Account.getDefaultInstance())
        `when`(flowAccessApi.getAccountAtLatestBlock(flowAddress)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(flowAccount))

        val result = flowAccessApi.getAccountAtLatestBlock(flowAddress)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(flowAccount), result)
    }

    @Test
    fun `Test getAccountByBlockHeight`() {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val flowAddress = FlowAddress("01")
        val height = 123L
        val flowAccount = FlowAccount.of(AccountOuterClass.Account.getDefaultInstance())
        `when`(flowAccessApi.getAccountByBlockHeight(flowAddress, height)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(flowAccount))

        val result = flowAccessApi.getAccountByBlockHeight(flowAddress, height)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(flowAccount), result)
    }

    @Test
    fun `Test executeScriptAtLatestBlock`() {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val script = FlowScript("script")
        val arguments = listOf(ByteString.copyFromUtf8("argument1"))
        val response = FlowScriptResponse("response".toByteArray())
        `when`(flowAccessApi.executeScriptAtLatestBlock(script, arguments)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(response))

        val result = flowAccessApi.executeScriptAtLatestBlock(script, arguments)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(response), result)
    }

    @Test
    fun `Test executeScriptAtBlockId`() {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val script = FlowScript("script")
        val blockId = FlowId("01")
        val arguments = listOf(ByteString.copyFromUtf8("argument1"))
        val response = FlowScriptResponse("response".toByteArray())
        `when`(flowAccessApi.executeScriptAtBlockId(script, blockId, arguments)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(response))

        val result = flowAccessApi.executeScriptAtBlockId(script, blockId, arguments)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(response), result)
    }

    @Test
    fun `Test executeScriptAtBlockHeight`() {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val script = FlowScript("script")
        val height = 123L
        val arguments = listOf(ByteString.copyFromUtf8("argument1"))
        val response = FlowScriptResponse("response".toByteArray())
        `when`(flowAccessApi.executeScriptAtBlockHeight(script, height, arguments)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(response))

        val result = flowAccessApi.executeScriptAtBlockHeight(script, height, arguments)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(response), result)
    }

    @Test
    fun `Test getEventsForHeightRange`() {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val type = "eventType"
        val range = 100L..200L
        val eventResults = listOf(FlowEventResult.of(Access.EventsResponse.Result.getDefaultInstance()), FlowEventResult.of(Access.EventsResponse.Result.getDefaultInstance()))
        `when`(flowAccessApi.getEventsForHeightRange(type, range)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(eventResults))

        val result = flowAccessApi.getEventsForHeightRange(type, range)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(eventResults), result)
    }

    @Test
    fun `Test getEventsForBlockIds`() {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val type = "eventType"
        val ids = setOf(FlowId("01"), FlowId("02"))
        val eventResults = listOf(FlowEventResult.of(Access.EventsResponse.Result.getDefaultInstance()), FlowEventResult.of(Access.EventsResponse.Result.getDefaultInstance()))
        `when`(flowAccessApi.getEventsForBlockIds(type, ids)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(eventResults))

        val result = flowAccessApi.getEventsForBlockIds(type, ids)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(eventResults), result)
    }

    @Test
    fun `Test getNetworkParameters`() {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val chainId = FlowChainId.TESTNET
        `when`(flowAccessApi.getNetworkParameters()).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(chainId))

        val result = flowAccessApi.getNetworkParameters()

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(chainId), result)
    }

    @Test
    fun `Test getLatestProtocolStateSnapshot`() {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val snapshot = FlowSnapshot("snapshot".toByteArray())
        `when`(flowAccessApi.getLatestProtocolStateSnapshot()).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(snapshot))

        val result = flowAccessApi.getLatestProtocolStateSnapshot()

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(snapshot), result)
    }

    @Test
    fun `Test getTransactionsByBlockId`() {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val blockId = FlowId("01")
        val transactions = listOf(FlowTransaction.of(TransactionOuterClass.Transaction.getDefaultInstance()))
        `when`(flowAccessApi.getTransactionsByBlockId(blockId)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(transactions))

        val result = flowAccessApi.getTransactionsByBlockId(blockId)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(transactions), result)
    }

    @Test
    fun `Test getTransactionsByBlockId with multiple results`() {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val blockId = FlowId("01")

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
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val blockId = FlowId("01")
        val transactionResults = listOf(FlowTransactionResult.of(Access.TransactionResultResponse.getDefaultInstance()))
        `when`(flowAccessApi.getTransactionResultsByBlockId(blockId)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(transactionResults))

        val result = flowAccessApi.getTransactionResultsByBlockId(blockId)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(transactionResults), result)
    }

    @Test
    fun `Test getTransactionResultsByBlockId with multiple results`() {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val flowId = FlowId("01")

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
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val blockId = FlowId("01")
        val executionResult = FlowExecutionResult.of(Access.ExecutionResultByIDResponse.getDefaultInstance())
        `when`(flowAccessApi.getExecutionResultByBlockId(blockId)).thenReturn(FlowAccessApi.AccessApiCallResponse.Success(executionResult))

        val result = flowAccessApi.getExecutionResultByBlockId(blockId)

        assertEquals(FlowAccessApi.AccessApiCallResponse.Success(executionResult), result)
    }

    @Test
    fun `Test subscribeExecutionDataByBlockId`(): Unit = runBlocking {
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val blockId = FlowId("01")
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
        val flowAccessApi = mock(FlowAccessApi::class.java)
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
        val flowAccessApi = mock(FlowAccessApi::class.java)
        val blockId = FlowId("01")
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
        val flowAccessApi = mock(FlowAccessApi::class.java)
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
