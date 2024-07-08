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

    override fun ping(): FlowAccessApi.AccessApiCallResponse<Unit> {
        return try {
            api.ping(
                Access.PingRequest.newBuilder()
                    .build()
            )
            FlowAccessApi.AccessApiCallResponse.Success(Unit)
        } catch (e: Exception) {
            FlowAccessApi.AccessApiCallResponse.Error("Failed to ping", e)
        }
    }

    override fun getLatestBlockHeader(sealed: Boolean): FlowAccessApi.AccessApiCallResponse<FlowBlockHeader> {
        return try {
            val ret = api.getLatestBlockHeader(
                Access.GetLatestBlockHeaderRequest.newBuilder()
                    .setIsSealed(sealed)
                    .build()
            )
            FlowAccessApi.AccessApiCallResponse.Success(FlowBlockHeader.of(ret.block))
        } catch (e: Exception) {
            FlowAccessApi.AccessApiCallResponse.Error("Failed to get latest block header", e)
        }
    }

    override fun getBlockHeaderById(id: FlowId): FlowAccessApi.AccessApiCallResponse<FlowBlockHeader> {
        return try {
            val ret = api.getBlockHeaderByID(
                Access.GetBlockHeaderByIDRequest.newBuilder()
                    .setId(id.byteStringValue)
                    .build()
            )
            if (ret.hasBlock()) {
                FlowAccessApi.AccessApiCallResponse.Success(FlowBlockHeader.of(ret.block))
            } else {
                FlowAccessApi.AccessApiCallResponse.Error("Block header not found")
            }
        } catch (e: Exception) {
            FlowAccessApi.AccessApiCallResponse.Error("Failed to get block header by ID", e)
        }
    }

    override fun getBlockHeaderByHeight(height: Long): FlowAccessApi.AccessApiCallResponse<FlowBlockHeader> {
        return try {
            val ret = api.getBlockHeaderByHeight(
                Access.GetBlockHeaderByHeightRequest.newBuilder()
                    .setHeight(height)
                    .build()
            )
            if (ret.hasBlock()) {
                FlowAccessApi.AccessApiCallResponse.Success(FlowBlockHeader.of(ret.block))
            } else {
                FlowAccessApi.AccessApiCallResponse.Error("Block header not found")
            }
        } catch (e: Exception) {
            FlowAccessApi.AccessApiCallResponse.Error("Failed to get block header by height", e)
        }
    }

    override fun getLatestBlock(sealed: Boolean): FlowAccessApi.AccessApiCallResponse<FlowBlock> {
        return try {
            val ret = api.getLatestBlock(
                Access.GetLatestBlockRequest.newBuilder()
                    .setIsSealed(sealed)
                    .build()
            )
            FlowAccessApi.AccessApiCallResponse.Success(FlowBlock.of(ret.block))
        } catch (e: Exception) {
            FlowAccessApi.AccessApiCallResponse.Error("Failed to get latest block", e)
        }
    }

    override fun getBlockById(id: FlowId): FlowAccessApi.AccessApiCallResponse<FlowBlock> {
        return try {
            val ret = api.getBlockByID(
                Access.GetBlockByIDRequest.newBuilder()
                    .setId(id.byteStringValue)
                    .build()
            )
            if (ret.hasBlock()) {
                FlowAccessApi.AccessApiCallResponse.Success(FlowBlock.of(ret.block))
            } else {
                FlowAccessApi.AccessApiCallResponse.Error("Block not found")
            }
        } catch (e: Exception) {
            FlowAccessApi.AccessApiCallResponse.Error("Failed to get block by ID", e)
        }
    }

    override fun getBlockByHeight(height: Long): FlowAccessApi.AccessApiCallResponse<FlowBlock> {
        return try {
            val ret = api.getBlockByHeight(
                Access.GetBlockByHeightRequest.newBuilder()
                    .setHeight(height)
                    .build()
            )
            if (ret.hasBlock()) {
                FlowAccessApi.AccessApiCallResponse.Success(FlowBlock.of(ret.block))
            } else {
                FlowAccessApi.AccessApiCallResponse.Error("Block not found")
            }
        } catch (e: Exception) {
            FlowAccessApi.AccessApiCallResponse.Error("Failed to get block by height", e)
        }
    }

    override fun getCollectionById(id: FlowId): FlowAccessApi.AccessApiCallResponse<FlowCollection> {
        return try {
            val ret = api.getCollectionByID(
                Access.GetCollectionByIDRequest.newBuilder()
                    .setId(id.byteStringValue)
                    .build()
            )
            if (ret.hasCollection()) {
                FlowAccessApi.AccessApiCallResponse.Success(FlowCollection.of(ret.collection))
            } else {
                FlowAccessApi.AccessApiCallResponse.Error("Collection not found")
            }
        } catch (e: Exception) {
            FlowAccessApi.AccessApiCallResponse.Error("Failed to get collection by ID", e)
        }
    }

    override fun sendTransaction(transaction: FlowTransaction): FlowAccessApi.AccessApiCallResponse<FlowId> {
        return try {
            val ret = api.sendTransaction(
                Access.SendTransactionRequest.newBuilder()
                    .setTransaction(transaction.builder().build())
                    .build()
            )
            FlowAccessApi.AccessApiCallResponse.Success(FlowId.of(ret.id.toByteArray()))
        } catch (e: Exception) {
            FlowAccessApi.AccessApiCallResponse.Error("Failed to send transaction", e)
        }
    }

    override fun getTransactionById(id: FlowId): FlowAccessApi.AccessApiCallResponse<FlowTransaction> {
        return try {
            val ret = api.getTransaction(
                Access.GetTransactionRequest.newBuilder()
                    .setId(id.byteStringValue)
                    .build()
            )
            if (ret.hasTransaction()) {
                FlowAccessApi.AccessApiCallResponse.Success(FlowTransaction.of(ret.transaction))
            } else {
                FlowAccessApi.AccessApiCallResponse.Error("Transaction not found")
            }
        } catch (e: Exception) {
            FlowAccessApi.AccessApiCallResponse.Error("Failed to get transaction by ID", e)
        }
    }

    override fun getTransactionResultById(id: FlowId): FlowAccessApi.AccessApiCallResponse<FlowTransactionResult> {
        return try {
            val ret = api.getTransactionResult(
                Access.GetTransactionRequest.newBuilder()
                    .setId(id.byteStringValue)
                    .build()
            )
            FlowAccessApi.AccessApiCallResponse.Success(FlowTransactionResult.of(ret))
        } catch (e: Exception) {
            FlowAccessApi.AccessApiCallResponse.Error("Failed to get transaction result by ID", e)
        }
    }

    @Deprecated("Behaves identically to getAccountAtLatestBlock", replaceWith = ReplaceWith("getAccountAtLatestBlock"))
    override fun getAccountByAddress(addresss: FlowAddress): FlowAccessApi.AccessApiCallResponse<FlowAccount> {
        return try {
            val ret = api.getAccount(
                Access.GetAccountRequest.newBuilder()
                    .setAddress(addresss.byteStringValue)
                    .build()
            )
            if (ret.hasAccount()) {
                FlowAccessApi.AccessApiCallResponse.Success(FlowAccount.of(ret.account))
            } else {
                FlowAccessApi.AccessApiCallResponse.Error("Account not found")
            }
        } catch (e: Exception) {
            FlowAccessApi.AccessApiCallResponse.Error("Failed to get account by address", e)
        }
    }

    override fun getAccountAtLatestBlock(addresss: FlowAddress): FlowAccessApi.AccessApiCallResponse<FlowAccount> {
        return try {
            val ret = api.getAccountAtLatestBlock(
                Access.GetAccountAtLatestBlockRequest.newBuilder()
                    .setAddress(addresss.byteStringValue)
                    .build()
            )
            if (ret.hasAccount()) {
                FlowAccessApi.AccessApiCallResponse.Success(FlowAccount.of(ret.account))
            } else {
                FlowAccessApi.AccessApiCallResponse.Error("Account not found")
            }
        } catch (e: Exception) {
            FlowAccessApi.AccessApiCallResponse.Error("Failed to get account at latest block", e)
        }
    }

    override fun getAccountByBlockHeight(addresss: FlowAddress, height: Long): FlowAccessApi.AccessApiCallResponse<FlowAccount> {
        return try {
            val ret = api.getAccountAtBlockHeight(
                Access.GetAccountAtBlockHeightRequest.newBuilder()
                    .setAddress(addresss.byteStringValue)
                    .setBlockHeight(height)
                    .build()
            )
            if (ret.hasAccount()) {
                FlowAccessApi.AccessApiCallResponse.Success(FlowAccount.of(ret.account))
            } else {
                FlowAccessApi.AccessApiCallResponse.Error("Account not found")
            }
        } catch (e: Exception) {
            FlowAccessApi.AccessApiCallResponse.Error("Failed to get account by block height", e)
        }
    }

    override fun executeScriptAtLatestBlock(script: FlowScript, arguments: Iterable<ByteString>): FlowAccessApi.AccessApiCallResponse<FlowScriptResponse> {
        return try {
            val request = Access.ExecuteScriptAtLatestBlockRequest.newBuilder()
                .setScript(script.byteStringValue)
                .addAllArguments(arguments)
                .build()

            val ret = api.executeScriptAtLatestBlock(request)

            FlowAccessApi.AccessApiCallResponse.Success(FlowScriptResponse(ret.value.toByteArray()))
        } catch (e: Exception) {
            println("Error executing script: ${e.message}")
            e.printStackTrace()
            FlowAccessApi.AccessApiCallResponse.Error("Failed to execute script at latest block", e)
        }
    }

    override fun executeScriptAtBlockId(script: FlowScript, blockId: FlowId, arguments: Iterable<ByteString>): FlowAccessApi.AccessApiCallResponse<FlowScriptResponse> {
        return try {
            val ret = api.executeScriptAtBlockID(
                Access.ExecuteScriptAtBlockIDRequest.newBuilder()
                    .setBlockId(blockId.byteStringValue)
                    .setScript(script.byteStringValue)
                    .addAllArguments(arguments)
                    .build()
            )
            FlowAccessApi.AccessApiCallResponse.Success(FlowScriptResponse(ret.value.toByteArray()))
        } catch (e: Exception) {
            FlowAccessApi.AccessApiCallResponse.Error("Failed to execute script at block ID", e)
        }
    }

    override fun executeScriptAtBlockHeight(script: FlowScript, height: Long, arguments: Iterable<ByteString>): FlowAccessApi.AccessApiCallResponse<FlowScriptResponse> {
        return try {
            val ret = api.executeScriptAtBlockHeight(
                Access.ExecuteScriptAtBlockHeightRequest.newBuilder()
                    .setBlockHeight(height)
                    .setScript(script.byteStringValue)
                    .addAllArguments(arguments)
                    .build()
            )
            FlowAccessApi.AccessApiCallResponse.Success(FlowScriptResponse(ret.value.toByteArray()))
        } catch (e: Exception) {
            FlowAccessApi.AccessApiCallResponse.Error("Failed to execute script at block height", e)
        }
    }

    override fun getEventsForHeightRange(type: String, range: ClosedRange<Long>): FlowAccessApi.AccessApiCallResponse<List<FlowEventResult>> {
        return try {
            val ret = api.getEventsForHeightRange(
                Access.GetEventsForHeightRangeRequest.newBuilder()
                    .setType(type)
                    .setStartHeight(range.start)
                    .setEndHeight(range.endInclusive)
                    .build()
            )
            FlowAccessApi.AccessApiCallResponse.Success(ret.resultsList.map { FlowEventResult.of(it) })
        } catch (e: Exception) {
            FlowAccessApi.AccessApiCallResponse.Error("Failed to get events for height range", e)
        }
    }

    override fun getEventsForBlockIds(type: String, ids: Set<FlowId>): FlowAccessApi.AccessApiCallResponse<List<FlowEventResult>> {
        return try {
            val ret = api.getEventsForBlockIDs(
                Access.GetEventsForBlockIDsRequest.newBuilder()
                    .setType(type)
                    .addAllBlockIds(ids.map { it.byteStringValue })
                    .build()
            )
            FlowAccessApi.AccessApiCallResponse.Success(ret.resultsList.map { FlowEventResult.of(it) })
        } catch (e: Exception) {
            FlowAccessApi.AccessApiCallResponse.Error("Failed to get events for block IDs", e)
        }
    }

    override fun getNetworkParameters(): FlowAccessApi.AccessApiCallResponse<FlowChainId> {
        return try {
            val ret = api.getNetworkParameters(
                Access.GetNetworkParametersRequest.newBuilder()
                    .build()
            )
            FlowAccessApi.AccessApiCallResponse.Success(FlowChainId.of(ret.chainId))
        } catch (e: Exception) {
            FlowAccessApi.AccessApiCallResponse.Error("Failed to get network parameters", e)
        }
    }

    override fun getLatestProtocolStateSnapshot(): FlowAccessApi.AccessApiCallResponse<FlowSnapshot> {
        return try {
            val ret = api.getLatestProtocolStateSnapshot(
                Access.GetLatestProtocolStateSnapshotRequest.newBuilder()
                    .build()
            )
            FlowAccessApi.AccessApiCallResponse.Success(FlowSnapshot(ret.serializedSnapshot.toByteArray()))
        } catch (e: Exception) {
            FlowAccessApi.AccessApiCallResponse.Error("Failed to get latest protocol state snapshot", e)
        }
    }

    override fun getTransactionsByBlockId(id: FlowId): FlowAccessApi.AccessApiCallResponse<List<FlowTransaction>> {
        return try {
            val ret = api.getTransactionsByBlockID(
                Access.GetTransactionsByBlockIDRequest.newBuilder()
                    .setBlockId(id.byteStringValue)
                    .build()
            )
            FlowAccessApi.AccessApiCallResponse.Success(ret.transactionsList.map { FlowTransaction.of(it) })
        } catch (e: Exception) {
            FlowAccessApi.AccessApiCallResponse.Error("Failed to get transactions by block ID", e)
        }
    }

    override fun getTransactionResultsByBlockId(id: FlowId): FlowAccessApi.AccessApiCallResponse<List<FlowTransactionResult>> {
        return try {
            val ret = api.getTransactionResultsByBlockID(
                Access.GetTransactionsByBlockIDRequest.newBuilder()
                    .setBlockId(id.byteStringValue)
                    .build()
            )
            FlowAccessApi.AccessApiCallResponse.Success(ret.transactionResultsList.map { FlowTransactionResult.of(it) })
        } catch (e: Exception) {
            FlowAccessApi.AccessApiCallResponse.Error("Failed to get transaction results by block ID", e)
        }
    }

    override fun getExecutionResultByBlockId(id: FlowId): FlowAccessApi.AccessApiCallResponse<FlowExecutionResult> {
        return try {
            val ret = api.getExecutionResultByID(
                Access.GetExecutionResultByIDRequest.newBuilder()
                    .setId(id.byteStringValue)
                    .build()
            )
            if (ret.hasExecutionResult()) {
                FlowAccessApi.AccessApiCallResponse.Success(FlowExecutionResult.of(ret))
            } else {
                FlowAccessApi.AccessApiCallResponse.Error("Execution result not found")
            }
        } catch (e: Exception) {
            FlowAccessApi.AccessApiCallResponse.Error("Failed to get execution result by block ID", e)
        }
    }

    override fun subscribeExecutionDataByBlockId(
        blockId: FlowId
    ): FlowAccessApi.AccessApiCallResponse<Pair<ReceiveChannel<FlowBlockExecutionData>, ReceiveChannel<Throwable>>> {
        val responseChannel = Channel<FlowBlockExecutionData>(Channel.UNLIMITED)
        val errorChannel = Channel<Throwable>(Channel.UNLIMITED)

        return runBlocking {
            try {
                val request = Executiondata.SubscribeExecutionDataFromStartBlockIDRequest.newBuilder()
                    .setStartBlockId(blockId.byteStringValue)
                    .build()

                val responseIterator = executionDataApi.subscribeExecutionDataFromStartBlockID(request)

                for (response in responseIterator) {
                    responseChannel.send(FlowBlockExecutionData.of(response.blockExecutionData))
                }
                FlowAccessApi.AccessApiCallResponse.Success(responseChannel to errorChannel)
            } catch (e: Exception) {
                errorChannel.send(e)
                FlowAccessApi.AccessApiCallResponse.Error("Failed to subscribe execution data by block ID", e)
            } finally {
                responseChannel.close()
                errorChannel.close()
            }
        }
    }

    override fun subscribeExecutionDataByBlockHeight(
        height: Long
    ): FlowAccessApi.AccessApiCallResponse<Pair<ReceiveChannel<FlowBlockExecutionData>, ReceiveChannel<Throwable>>> {
        val responseChannel = Channel<FlowBlockExecutionData>(Channel.UNLIMITED)
        val errorChannel = Channel<Throwable>(Channel.UNLIMITED)

        return runBlocking {
            try {
                val request = Executiondata.SubscribeExecutionDataFromStartBlockHeightRequest.newBuilder()
                    .setStartBlockHeight(height)
                    .build()

                val responseIterator = executionDataApi.subscribeExecutionDataFromStartBlockHeight(request)

                for (response in responseIterator) {
                    responseChannel.send(FlowBlockExecutionData.of(response.blockExecutionData))
                }
                FlowAccessApi.AccessApiCallResponse.Success(responseChannel to errorChannel)
            } catch (e: Exception) {
                errorChannel.send(e)
                FlowAccessApi.AccessApiCallResponse.Error("Failed to subscribe execution data by block height", e)
            } finally {
                responseChannel.close()
                errorChannel.close()
            }
        }
    }

    override fun subscribeEventsByBlockId(
        blockId: FlowId
    ): FlowAccessApi.AccessApiCallResponse<Pair<ReceiveChannel<List<FlowEvent>>, ReceiveChannel<Throwable>>> {
        val responseChannel = Channel<List<FlowEvent>>(Channel.UNLIMITED)
        val errorChannel = Channel<Throwable>(Channel.UNLIMITED)

        return runBlocking {
            try {
                val request = Executiondata.SubscribeEventsFromStartBlockIDRequest.newBuilder()
                    .setStartBlockId(blockId.byteStringValue)
                    .build()

                val responseIterator = executionDataApi.subscribeEventsFromStartBlockID(request)

                for (response in responseIterator) {
                    responseChannel.send(response.eventsList.map { FlowEvent.of(it) })
                }
                FlowAccessApi.AccessApiCallResponse.Success(responseChannel to errorChannel)
            } catch (e: Exception) {
                errorChannel.send(e)
                FlowAccessApi.AccessApiCallResponse.Error("Failed to subscribe events by block ID", e)
            } finally {
                responseChannel.close()
                errorChannel.close()
            }
        }
    }

    override fun subscribeEventsByBlockHeight(
        height: Long
    ): FlowAccessApi.AccessApiCallResponse<Pair<ReceiveChannel<List<FlowEvent>>, ReceiveChannel<Throwable>>> {
        val responseChannel = Channel<List<FlowEvent>>(Channel.UNLIMITED)
        val errorChannel = Channel<Throwable>(Channel.UNLIMITED)

        return runBlocking {
            try {
                val request = Executiondata.SubscribeEventsFromStartHeightRequest.newBuilder()
                    .setStartBlockHeight(height)
                    .build()

                val responseIterator = executionDataApi.subscribeEventsFromStartHeight(request)

                for (response in responseIterator) {
                    responseChannel.send(response.eventsList.map { FlowEvent.of(it) })
                }
                FlowAccessApi.AccessApiCallResponse.Success(responseChannel to errorChannel)
            } catch (e: Exception) {
                errorChannel.send(e)
                FlowAccessApi.AccessApiCallResponse.Error("Failed to subscribe events by block height", e)
            } finally {
                responseChannel.close()
                errorChannel.close()
            }
        }
    }

}
