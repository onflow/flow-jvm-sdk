package org.onflow.flow.sdk.impl

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.runBlocking
import com.google.protobuf.ByteString
import org.onflow.flow.sdk.*
import io.grpc.ManagedChannel
import org.onflow.protobuf.access.Access
import org.onflow.protobuf.access.AccessAPIGrpc
import org.onflow.protobuf.executiondata.ExecutionDataAPIGrpc
import org.onflow.protobuf.executiondata.Executiondata
import java.io.Closeable

class FlowAccessApiImpl(
    private val api: AccessAPIGrpc.AccessAPIBlockingStub,
    private val executionDataApi: ExecutionDataAPIGrpc.ExecutionDataAPIBlockingStub
) : FlowAccessApi, Closeable {
    override fun close() {
        val chan = api.channel
        if (chan is ManagedChannel) {
            chan.shutdownNow()
        }
    }

    override fun ping() {
        api.ping(
            Access.PingRequest.newBuilder()
                .build()
        )
    }

    override fun getLatestBlockHeader(sealed: Boolean): FlowBlockHeader {
        val ret = api.getLatestBlockHeader(
            Access.GetLatestBlockHeaderRequest.newBuilder()
                .setIsSealed(sealed)
                .build()
        )
        return FlowBlockHeader.of(ret.block)
    }

    override fun getBlockHeaderById(id: FlowId): FlowBlockHeader? {
        val ret = api.getBlockHeaderByID(
            Access.GetBlockHeaderByIDRequest.newBuilder()
                .setId(id.byteStringValue)
                .build()
        )
        return if (ret.hasBlock()) {
            FlowBlockHeader.of(ret.block)
        } else {
            null
        }
    }

    override fun getBlockHeaderByHeight(height: Long): FlowBlockHeader? {
        val ret = api.getBlockHeaderByHeight(
            Access.GetBlockHeaderByHeightRequest.newBuilder()
                .setHeight(height)
                .build()
        )
        return if (ret.hasBlock()) {
            FlowBlockHeader.of(ret.block)
        } else {
            null
        }
    }

    override fun getLatestBlock(sealed: Boolean): FlowBlock {
        val ret = api.getLatestBlock(
            Access.GetLatestBlockRequest.newBuilder()
                .setIsSealed(sealed)
                .build()
        )
        return FlowBlock.of(ret.block)
    }

    override fun getBlockById(id: FlowId): FlowBlock? {
        val ret = api.getBlockByID(
            Access.GetBlockByIDRequest.newBuilder()
                .setId(id.byteStringValue)
                .build()
        )
        return if (ret.hasBlock()) {
            FlowBlock.of(ret.block)
        } else {
            null
        }
    }

    override fun getTransactionsByBlockId(id: FlowId): List<FlowTransaction> {
        val ret = api.getTransactionsByBlockID(
            Access.GetTransactionsByBlockIDRequest.newBuilder()
                .setBlockId(id.byteStringValue)
                .build()
        )
        return ret.transactionsList.map { FlowTransaction.of(it) }
    }

    override fun getTransactionResultsByBlockId(id: FlowId): List<FlowTransactionResult> {
        val ret = api.getTransactionResultsByBlockID(
            Access.GetTransactionsByBlockIDRequest.newBuilder()
                .setBlockId(id.byteStringValue)
                .build()
        )
        return ret.transactionResultsList.map { FlowTransactionResult.of(it) }
    }

    override fun getExecutionResultByBlockId(id: FlowId): ExecutionResult? {
        val ret = api.getExecutionResultByID(
            Access.GetExecutionResultByIDRequest.newBuilder()
                .setId(id.byteStringValue)
                .build()
        )
        return if (ret.hasExecutionResult()) {
            ExecutionResult.of(ret)
        } else {
            null
        }
    }

    override fun getBlockByHeight(height: Long): FlowBlock? {
        val ret = api.getBlockByHeight(
            Access.GetBlockByHeightRequest.newBuilder()
                .setHeight(height)
                .build()
        )
        return if (ret.hasBlock()) {
            FlowBlock.of(ret.block)
        } else {
            null
        }
    }

    override fun getCollectionById(id: FlowId): FlowCollection? {
        val ret = api.getCollectionByID(
            Access.GetCollectionByIDRequest.newBuilder()
                .setId(id.byteStringValue)
                .build()
        )
        return if (ret.hasCollection()) {
            FlowCollection.of(ret.collection)
        } else {
            null
        }
    }

    override fun sendTransaction(transaction: FlowTransaction): FlowId {
        val ret = api.sendTransaction(
            Access.SendTransactionRequest.newBuilder()
                .setTransaction(transaction.builder().build())
                .build()
        )
        return FlowId.of(ret.id.toByteArray())
    }

    override fun getTransactionById(id: FlowId): FlowTransaction? {
        val ret = api.getTransaction(
            Access.GetTransactionRequest.newBuilder()
                .setId(id.byteStringValue)
                .build()
        )
        return if (ret.hasTransaction()) {
            FlowTransaction.of(ret.transaction)
        } else {
            null
        }
    }

    override fun getTransactionResultById(id: FlowId): FlowTransactionResult {
        val ret = api.getTransactionResult(
            Access.GetTransactionRequest.newBuilder()
                .setId(id.byteStringValue)
                .build()
        )
        return FlowTransactionResult.of(ret)
    }

    @Deprecated("Behaves identically to getAccountAtLatestBlock", replaceWith = ReplaceWith("getAccountAtLatestBlock"))
    override fun getAccountByAddress(addresss: FlowAddress): FlowAccount? {
        val ret = api.getAccount(
            Access.GetAccountRequest.newBuilder()
                .setAddress(addresss.byteStringValue)
                .build()
        )
        return if (ret.hasAccount()) {
            FlowAccount.of(ret.account)
        } else {
            null
        }
    }

    override fun getAccountAtLatestBlock(addresss: FlowAddress): FlowAccount? {
        val ret = api.getAccountAtLatestBlock(
            Access.GetAccountAtLatestBlockRequest.newBuilder()
                .setAddress(addresss.byteStringValue)
                .build()
        )
        return if (ret.hasAccount()) {
            FlowAccount.of(ret.account)
        } else {
            null
        }
    }

    override fun getAccountByBlockHeight(addresss: FlowAddress, height: Long): FlowAccount? {
        val ret = api.getAccountAtBlockHeight(
            Access.GetAccountAtBlockHeightRequest.newBuilder()
                .setAddress(addresss.byteStringValue)
                .setBlockHeight(height)
                .build()
        )
        return if (ret.hasAccount()) {
            FlowAccount.of(ret.account)
        } else {
            null
        }
    }

    override fun executeScriptAtLatestBlock(script: FlowScript, arguments: Iterable<ByteString>): FlowScriptResponse {
        val ret = api.executeScriptAtLatestBlock(
            Access.ExecuteScriptAtLatestBlockRequest.newBuilder()
                .setScript(script.byteStringValue)
                .addAllArguments(arguments)
                .build()
        )
        return FlowScriptResponse(ret.value.toByteArray())
    }

    override fun executeScriptAtBlockId(script: FlowScript, blockId: FlowId, arguments: Iterable<ByteString>): FlowScriptResponse {
        val ret = api.executeScriptAtBlockID(
            Access.ExecuteScriptAtBlockIDRequest.newBuilder()
                .setBlockId(blockId.byteStringValue)
                .setScript(script.byteStringValue)
                .addAllArguments(arguments)
                .build()
        )
        return FlowScriptResponse(ret.value.toByteArray())
    }

    override fun executeScriptAtBlockHeight(script: FlowScript, height: Long, arguments: Iterable<ByteString>): FlowScriptResponse {
        val ret = api.executeScriptAtBlockHeight(
            Access.ExecuteScriptAtBlockHeightRequest.newBuilder()
                .setBlockHeight(height)
                .setScript(script.byteStringValue)
                .addAllArguments(arguments)
                .build()
        )
        return FlowScriptResponse(ret.value.toByteArray())
    }

    override fun getEventsForHeightRange(type: String, range: ClosedRange<Long>): List<FlowEventResult> {
        val ret = api.getEventsForHeightRange(
            Access.GetEventsForHeightRangeRequest.newBuilder()
                .setType(type)
                .setStartHeight(range.start)
                .setEndHeight(range.endInclusive)
                .build()
        )
        return ret.resultsList
            .map { FlowEventResult.of(it) }
    }

    override fun getEventsForBlockIds(type: String, ids: Set<FlowId>): List<FlowEventResult> {
        val ret = api.getEventsForBlockIDs(
            Access.GetEventsForBlockIDsRequest.newBuilder()
                .setType(type)
                .addAllBlockIds(ids.map { it.byteStringValue })
                .build()
        )
        return ret.resultsList
            .map { FlowEventResult.of(it) }
    }

    override fun getNetworkParameters(): FlowChainId {
        val ret = api.getNetworkParameters(
            Access.GetNetworkParametersRequest.newBuilder()
                .build()
        )
        return FlowChainId.of(ret.chainId)
    }

    override fun getLatestProtocolStateSnapshot(): FlowSnapshot {
        val ret = api.getLatestProtocolStateSnapshot(
            Access.GetLatestProtocolStateSnapshotRequest.newBuilder()
                .build()
        )
        return FlowSnapshot(ret.serializedSnapshot.toByteArray())
    }

    override fun subscribeExecutionDataByBlockId(
        blockId: FlowId
    ): Pair<ReceiveChannel<Executiondata.SubscribeExecutionDataResponse>, ReceiveChannel<Throwable>> {
        val responseChannel = Channel<Executiondata.SubscribeExecutionDataResponse>(Channel.UNLIMITED)
        val errorChannel = Channel<Throwable>(Channel.UNLIMITED)

        runBlocking {
            try {
                val request = Executiondata.SubscribeExecutionDataFromStartBlockIDRequest.newBuilder()
                    .setStartBlockId(blockId.byteStringValue)
                    .build()

                val responseIterator = executionDataApi.subscribeExecutionDataFromStartBlockID(request)

                for (response in responseIterator) {
                    responseChannel.send(response)
                    // to-do: cast to custom response class
                }
            } catch (e: Exception) {
                errorChannel.send(e)
            } finally {
                responseChannel.close()
                errorChannel.close()
            }
        }
        return responseChannel to errorChannel
    }

    override fun subscribeExecutionDataByBlockHeight(
        height: Long
    ): Pair<ReceiveChannel<Executiondata.SubscribeExecutionDataResponse>, ReceiveChannel<Throwable>> {
        val responseChannel = Channel<Executiondata.SubscribeExecutionDataResponse>(Channel.UNLIMITED)
        val errorChannel = Channel<Throwable>(Channel.UNLIMITED)

        runBlocking {
            try {
                val request = Executiondata.SubscribeExecutionDataFromStartBlockHeightRequest.newBuilder()
                    .setStartBlockHeight(height)
                    .build()

                val responseIterator = executionDataApi.subscribeExecutionDataFromStartBlockHeight(request)

                for (response in responseIterator) {
                    responseChannel.send(response)
                    // to-do: cast to custom response class
                }
            } catch (e: Exception) {
                errorChannel.send(e)
            } finally {
                responseChannel.close()
                errorChannel.close()
            }
        }
        return responseChannel to errorChannel
    }

    override fun subscribeEventsByBlockId(
        blockId: FlowId
    ): Pair<ReceiveChannel<Executiondata.SubscribeEventsResponse>, ReceiveChannel<Throwable>> {
        val responseChannel = Channel<Executiondata.SubscribeEventsResponse>(Channel.UNLIMITED)
        val errorChannel = Channel<Throwable>(Channel.UNLIMITED)

        runBlocking {
            try {
                val request = Executiondata.SubscribeEventsFromStartBlockIDRequest.newBuilder()
                    .setStartBlockId(blockId.byteStringValue)
                    .build()

                val responseIterator = executionDataApi.subscribeEventsFromStartBlockID(request)

                for (response in responseIterator) {
                    responseChannel.send(response)
                    // to-do: cast to custom response class
                }
            } catch (e: Exception) {
                errorChannel.send(e)
            } finally {
                responseChannel.close()
                errorChannel.close()
            }
        }
        return responseChannel to errorChannel
    }

    override fun subscribeEventsByBlockHeight(
        height: Long
    ): Pair<ReceiveChannel<Executiondata.SubscribeEventsResponse>, ReceiveChannel<Throwable>> {
        val responseChannel = Channel<Executiondata.SubscribeEventsResponse>(Channel.UNLIMITED)
        val errorChannel = Channel<Throwable>(Channel.UNLIMITED)

        runBlocking {
            try {
                val request = Executiondata.SubscribeEventsFromStartHeightRequest.newBuilder()
                    .setStartBlockHeight(height)
                    .build()

                val responseIterator = executionDataApi.subscribeEventsFromStartHeight(request)

                for (response in responseIterator) {
                    responseChannel.send(response)
                    // to-do: cast to custom response class
                }
            } catch (e: Exception) {
                errorChannel.send(e)
            } finally {
                responseChannel.close()
                errorChannel.close()
            }
        }
        return responseChannel to errorChannel
    }
}
