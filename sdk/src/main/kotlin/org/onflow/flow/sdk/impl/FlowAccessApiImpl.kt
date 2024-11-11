package org.onflow.flow.sdk.impl

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import com.google.protobuf.ByteString
import org.onflow.flow.sdk.*
import io.grpc.ManagedChannel
import kotlinx.coroutines.*
import org.onflow.protobuf.access.Access
import org.onflow.protobuf.access.AccessAPIGrpc
import org.onflow.protobuf.executiondata.ExecutionDataAPIGrpc
import org.onflow.protobuf.executiondata.Executiondata
import java.io.Closeable

class FlowAccessApiImpl(
    private val api: AccessAPIGrpc.AccessAPIBlockingStub,
    private val executionDataApi: ExecutionDataAPIGrpc.ExecutionDataAPIBlockingStub,
) : FlowAccessApi,
    Closeable {
    override fun close() {
        val chan = api.channel
        if (chan is ManagedChannel) {
            chan.shutdownNow()
        }
    }

    private fun <T> executeWithResponse(action: () -> T, errorMessage: String): FlowAccessApi.AccessApiCallResponse<T> {
        return try {
            FlowAccessApi.AccessApiCallResponse.Success(action())
        } catch (e: Exception) {
            FlowAccessApi.AccessApiCallResponse.Error(errorMessage, e)
        }
    }

    override fun ping(): FlowAccessApi.AccessApiCallResponse<Unit> =
        executeWithResponse(
            action = {
                api.ping(
                    Access.PingRequest.newBuilder().build()
                )
                Unit
            },
            errorMessage = "Failed to ping"
        )

    override fun getAccountKeyAtLatestBlock(address: FlowAddress, keyIndex: Int): FlowAccessApi.AccessApiCallResponse<FlowAccountKey> =
        executeWithResponse(
            action = {
                val ret = api.getAccountKeyAtLatestBlock(
                    Access.GetAccountKeyAtLatestBlockRequest
                        .newBuilder()
                        .setAddress(address.byteStringValue)
                        .setIndex(keyIndex)
                        .build()
                )
                FlowAccountKey.of(ret.accountKey)
            },
            errorMessage = "Failed to get account key at latest block"
        )

    override fun getAccountKeyAtBlockHeight(address: FlowAddress, keyIndex: Int, height: Long): FlowAccessApi.AccessApiCallResponse<FlowAccountKey> =
        executeWithResponse(
            action = {
                val ret = api.getAccountKeyAtBlockHeight(
                    Access.GetAccountKeyAtBlockHeightRequest
                        .newBuilder()
                        .setAddress(address.byteStringValue)
                        .setIndex(keyIndex)
                        .setBlockHeight(height)
                        .build()
                )
                FlowAccountKey.of(ret.accountKey)
            },
            errorMessage = "Failed to get account key at block height"
        )

    override fun getAccountKeysAtLatestBlock(address: FlowAddress): FlowAccessApi.AccessApiCallResponse<List<FlowAccountKey>> =
        executeWithResponse(
            action = {
                val ret = api.getAccountKeysAtLatestBlock(
                    Access.GetAccountKeysAtLatestBlockRequest
                        .newBuilder()
                        .setAddress(address.byteStringValue)
                        .build()
                )
                ret.accountKeysList.map { FlowAccountKey.of(it) }
            },
            errorMessage = "Failed to get account keys at latest block"
        )

    override fun getAccountKeysAtBlockHeight(address: FlowAddress, height: Long): FlowAccessApi.AccessApiCallResponse<List<FlowAccountKey>> =
        executeWithResponse(
            action = {
                val ret = api.getAccountKeysAtBlockHeight(
                    Access.GetAccountKeysAtBlockHeightRequest
                        .newBuilder()
                        .setAddress(address.byteStringValue)
                        .setBlockHeight(height)
                        .build()
                )
                ret.accountKeysList.map { FlowAccountKey.of(it) }
            },
            errorMessage = "Failed to get account keys at block height"
        )

    override fun getLatestBlockHeader(sealed: Boolean): FlowAccessApi.AccessApiCallResponse<FlowBlockHeader> =
        executeWithResponse(
            action = {
                val ret = api.getLatestBlockHeader(
                    Access.GetLatestBlockHeaderRequest
                        .newBuilder()
                        .setIsSealed(sealed)
                        .build()
                )
                FlowBlockHeader.of(ret.block)
            },
            errorMessage = "Failed to get latest block header"
        )

    override fun getBlockHeaderById(id: FlowId): FlowAccessApi.AccessApiCallResponse<FlowBlockHeader> =
        executeWithResponse(
            action = {
                val ret = api.getBlockHeaderByID(
                    Access.GetBlockHeaderByIDRequest
                        .newBuilder()
                        .setId(id.byteStringValue)
                        .build()
                )
                if (ret.hasBlock()) FlowBlockHeader.of(ret.block) else throw Exception("Block header not found")
            },
            errorMessage = "Failed to get block header by ID"
        )

    override fun getBlockHeaderByHeight(height: Long): FlowAccessApi.AccessApiCallResponse<FlowBlockHeader> =
        executeWithResponse(
            action = {
                val ret = api.getBlockHeaderByHeight(
                    Access.GetBlockHeaderByHeightRequest
                        .newBuilder()
                        .setHeight(height)
                        .build()
                )
                if (ret.hasBlock()) FlowBlockHeader.of(ret.block) else throw Exception("Block header not found")
            },
            errorMessage = "Failed to get block header by height"
        )


    override fun getLatestBlock(sealed: Boolean, fullBlockResponse: Boolean): FlowAccessApi.AccessApiCallResponse<FlowBlock> =
        executeWithResponse(
            action = {
                val ret = api.getLatestBlock(
                    Access.GetLatestBlockRequest
                        .newBuilder()
                        .setIsSealed(sealed)
                        .setFullBlockResponse(fullBlockResponse)
                        .build()
                )
                FlowBlock.of(ret.block)
            },
            errorMessage = "Failed to get latest block"
        )

    override fun getAccountBalanceAtLatestBlock(address: FlowAddress): FlowAccessApi.AccessApiCallResponse<Long> =
        executeWithResponse(
            action = {
                val ret = api.getAccountBalanceAtLatestBlock(
                    Access.GetAccountBalanceAtLatestBlockRequest
                        .newBuilder()
                        .setAddress(address.byteStringValue)
                        .build()
                )
                ret.balance
            },
            errorMessage = "Failed to get account balance at latest block"
        )

    override fun getAccountBalanceAtBlockHeight(address: FlowAddress, height: Long): FlowAccessApi.AccessApiCallResponse<Long> =
        executeWithResponse(
            action = {
                val ret = api.getAccountBalanceAtBlockHeight(
                    Access.GetAccountBalanceAtBlockHeightRequest
                        .newBuilder()
                        .setAddress(address.byteStringValue)
                        .setBlockHeight(height)
                        .build()
                )
                ret.balance
            },
            errorMessage = "Failed to get account balance at block height"
        )


    override fun getBlockById(id: FlowId, fullBlockResponse: Boolean): FlowAccessApi.AccessApiCallResponse<FlowBlock> =
        executeWithResponse(
            action = {
                val ret = api.getBlockByID(
                    Access.GetBlockByIDRequest
                        .newBuilder()
                        .setId(id.byteStringValue)
                        .setFullBlockResponse(fullBlockResponse)
                        .build()
                )
                if (ret.hasBlock()) FlowBlock.of(ret.block) else throw Exception("Block not found")
            },
            errorMessage = "Failed to get block by ID"
        )

    override fun getBlockByHeight(height: Long, fullBlockResponse: Boolean): FlowAccessApi.AccessApiCallResponse<FlowBlock> =
        executeWithResponse(
            action = {
                val ret = api.getBlockByHeight(
                    Access.GetBlockByHeightRequest
                        .newBuilder()
                        .setHeight(height)
                        .setFullBlockResponse(fullBlockResponse)
                        .build()
                )
                if (ret.hasBlock()) FlowBlock.of(ret.block) else throw Exception("Block not found")
            },
            errorMessage = "Failed to get block by height"
        )

    override fun getCollectionById(id: FlowId): FlowAccessApi.AccessApiCallResponse<FlowCollection> =
        executeWithResponse(
            action = {
                val ret = api.getCollectionByID(
                    Access.GetCollectionByIDRequest
                        .newBuilder()
                        .setId(id.byteStringValue)
                        .build()
                )
                if (ret.hasCollection()) FlowCollection.of(ret.collection) else throw Exception("Collection not found")
            },
            errorMessage = "Failed to get collection by ID"
        )

    override fun getFullCollectionById(id: FlowId): FlowAccessApi.AccessApiCallResponse<List<FlowTransaction>> =
        executeWithResponse(
            action = {
                val ret = api.getFullCollectionByID(
                    Access.GetFullCollectionByIDRequest
                        .newBuilder()
                        .setId(id.byteStringValue)
                        .build()
                )
                ret.transactionsList.map { FlowTransaction.of(it) }
            },
            errorMessage = "Failed to get full collection by ID"
        )

    override fun sendTransaction(transaction: FlowTransaction): FlowAccessApi.AccessApiCallResponse<FlowId> =
        executeWithResponse(
            action = {
                val ret = api.sendTransaction(
                    Access.SendTransactionRequest
                        .newBuilder()
                        .setTransaction(transaction.builder().build())
                        .build()
                )
                FlowId.of(ret.id.toByteArray())
            },
            errorMessage = "Failed to send transaction"
        )

    override fun getTransactionById(id: FlowId): FlowAccessApi.AccessApiCallResponse<FlowTransaction> =
        executeWithResponse(
            action = {
                val ret = api.getTransaction(
                    Access.GetTransactionRequest
                        .newBuilder()
                        .setId(id.byteStringValue)
                        .build()
                )
                if (ret.hasTransaction()) FlowTransaction.of(ret.transaction) else throw Exception("Transaction not found")
            },
            errorMessage = "Failed to get transaction by ID"
        )


    override fun getTransactionResultById(id: FlowId): FlowAccessApi.AccessApiCallResponse<FlowTransactionResult> =
        executeWithResponse(
            action = {
                val ret = api.getTransactionResult(
                    Access.GetTransactionRequest
                        .newBuilder()
                        .setId(id.byteStringValue)
                        .build()
                )
                FlowTransactionResult.of(ret)
            },
            errorMessage = "Failed to get transaction result by ID"
        )

    override fun getTransactionResultByIndex(blockId: FlowId, index: Int): FlowAccessApi.AccessApiCallResponse<FlowTransactionResult> =
        executeWithResponse(
            action = {
                val ret = api.getTransactionResultByIndex(
                    Access.GetTransactionByIndexRequest
                        .newBuilder()
                        .setBlockId(blockId.byteStringValue)
                        .setIndex(index)
                        .build()
                )
                FlowTransactionResult.of(ret)
            },
            errorMessage = "Failed to get transaction result by index"
        )

    override fun getSystemTransaction(blockId: FlowId): FlowAccessApi.AccessApiCallResponse<FlowTransaction> =
        executeWithResponse(
            action = {
                val ret = api.getSystemTransaction(
                    Access.GetSystemTransactionRequest
                        .newBuilder()
                        .setBlockId(blockId.byteStringValue)
                        .build()
                )
                if (ret.hasTransaction()) FlowTransaction.of(ret.transaction) else throw Exception("System transaction not found")
            },
            errorMessage = "Failed to get system transaction by block ID"
        )

    override fun getSystemTransactionResult(blockId: FlowId): FlowAccessApi.AccessApiCallResponse<FlowTransactionResult> =
        executeWithResponse(
            action = {
                val ret = api.getSystemTransactionResult(
                    Access.GetSystemTransactionResultRequest
                        .newBuilder()
                        .setBlockId(blockId.byteStringValue)
                        .build()
                )
                FlowTransactionResult.of(ret)
            },
            errorMessage = "Failed to get system transaction result by block ID"
        )


    @Deprecated("Behaves identically to getAccountAtLatestBlock", replaceWith = ReplaceWith("getAccountAtLatestBlock"))
    override fun getAccountByAddress(address: FlowAddress): FlowAccessApi.AccessApiCallResponse<FlowAccount> =
        executeWithResponse(
            action = {
                val ret = api.getAccount(
                    Access.GetAccountRequest
                        .newBuilder()
                        .setAddress(address.byteStringValue)
                        .build()
                )
                if (ret.hasAccount()) FlowAccount.of(ret.account) else throw Exception("Account not found")
            },
            errorMessage = "Failed to get account by address"
        )

    override fun getAccountAtLatestBlock(address: FlowAddress): FlowAccessApi.AccessApiCallResponse<FlowAccount> =
        executeWithResponse(
            action = {
                val ret = api.getAccountAtLatestBlock(
                    Access.GetAccountAtLatestBlockRequest
                        .newBuilder()
                        .setAddress(address.byteStringValue)
                        .build()
                )
                if (ret.hasAccount()) FlowAccount.of(ret.account) else throw Exception("Account not found")
            },
            errorMessage = "Failed to get account at latest block"
        )

    override fun getAccountByBlockHeight(address: FlowAddress, height: Long): FlowAccessApi.AccessApiCallResponse<FlowAccount> =
        executeWithResponse(
            action = {
                val ret = api.getAccountAtBlockHeight(
                    Access.GetAccountAtBlockHeightRequest
                        .newBuilder()
                        .setAddress(address.byteStringValue)
                        .setBlockHeight(height)
                        .build()
                )
                if (ret.hasAccount()) FlowAccount.of(ret.account) else throw Exception("Account not found")
            },
            errorMessage = "Failed to get account by block height"
        )

    override fun executeScriptAtLatestBlock(script: FlowScript, arguments: Iterable<ByteString>): FlowAccessApi.AccessApiCallResponse<FlowScriptResponse> =
        try {
            val request = Access.ExecuteScriptAtLatestBlockRequest
                .newBuilder()
                .setScript(script.byteStringValue)
                .addAllArguments(arguments)
                .build()

            val ret = api.executeScriptAtLatestBlock(request)

            FlowAccessApi.AccessApiCallResponse.Success(FlowScriptResponse(ret.value.toByteArray()))
        } catch (e: Exception) {
            FlowAccessApi.AccessApiCallResponse.Error("Failed to execute script at latest block", e)
        }

    override fun executeScriptAtBlockId(script: FlowScript, blockId: FlowId, arguments: Iterable<ByteString>): FlowAccessApi.AccessApiCallResponse<FlowScriptResponse> =
        try {
            val ret = api.executeScriptAtBlockID(
                Access.ExecuteScriptAtBlockIDRequest
                    .newBuilder()
                    .setBlockId(blockId.byteStringValue)
                    .setScript(script.byteStringValue)
                    .addAllArguments(arguments)
                    .build()
            )
            FlowAccessApi.AccessApiCallResponse.Success(FlowScriptResponse(ret.value.toByteArray()))
        } catch (e: Exception) {
            FlowAccessApi.AccessApiCallResponse.Error("Failed to execute script at block ID", e)
        }

    override fun executeScriptAtBlockHeight(script: FlowScript, height: Long, arguments: Iterable<ByteString>): FlowAccessApi.AccessApiCallResponse<FlowScriptResponse> =
        try {
            val ret = api.executeScriptAtBlockHeight(
                Access.ExecuteScriptAtBlockHeightRequest
                    .newBuilder()
                    .setBlockHeight(height)
                    .setScript(script.byteStringValue)
                    .addAllArguments(arguments)
                    .build()
            )
            FlowAccessApi.AccessApiCallResponse.Success(FlowScriptResponse(ret.value.toByteArray()))
        } catch (e: Exception) {
            FlowAccessApi.AccessApiCallResponse.Error("Failed to execute script at block height", e)
        }

    override fun getEventsForHeightRange(type: String, range: ClosedRange<Long>): FlowAccessApi.AccessApiCallResponse<List<FlowEventResult>> =
        try {
            val ret = api.getEventsForHeightRange(
                Access.GetEventsForHeightRangeRequest
                    .newBuilder()
                    .setType(type)
                    .setStartHeight(range.start)
                    .setEndHeight(range.endInclusive)
                    .build()
            )
            FlowAccessApi.AccessApiCallResponse.Success(ret.resultsList.map { FlowEventResult.of(it) })
        } catch (e: Exception) {
            FlowAccessApi.AccessApiCallResponse.Error("Failed to get events for height range", e)
        }

    override fun getEventsForBlockIds(type: String, ids: Set<FlowId>): FlowAccessApi.AccessApiCallResponse<List<FlowEventResult>> =
        try {
            val ret = api.getEventsForBlockIDs(
                Access.GetEventsForBlockIDsRequest
                    .newBuilder()
                    .setType(type)
                    .addAllBlockIds(ids.map { it.byteStringValue })
                    .build()
            )
            FlowAccessApi.AccessApiCallResponse.Success(ret.resultsList.map { FlowEventResult.of(it) })
        } catch (e: Exception) {
            FlowAccessApi.AccessApiCallResponse.Error("Failed to get events for block IDs", e)
        }

    override fun getNetworkParameters(): FlowAccessApi.AccessApiCallResponse<FlowChainId> =
        try {
            val ret = api.getNetworkParameters(
                Access.GetNetworkParametersRequest
                    .newBuilder()
                    .build()
            )
            FlowAccessApi.AccessApiCallResponse.Success(FlowChainId.of(ret.chainId))
        } catch (e: Exception) {
            FlowAccessApi.AccessApiCallResponse.Error("Failed to get network parameters", e)
        }

    override fun getLatestProtocolStateSnapshot(): FlowAccessApi.AccessApiCallResponse<FlowSnapshot> =
        try {
            val ret = api.getLatestProtocolStateSnapshot(
                Access.GetLatestProtocolStateSnapshotRequest
                    .newBuilder()
                    .build()
            )
            FlowAccessApi.AccessApiCallResponse.Success(FlowSnapshot(ret.serializedSnapshot.toByteArray()))
        } catch (e: Exception) {
            FlowAccessApi.AccessApiCallResponse.Error("Failed to get latest protocol state snapshot", e)
        }

    override fun getProtocolStateSnapshotByBlockId(blockId: FlowId): FlowAccessApi.AccessApiCallResponse<FlowSnapshot> =
        try {
            val ret = api.getProtocolStateSnapshotByBlockID(
                Access.GetProtocolStateSnapshotByBlockIDRequest
                    .newBuilder()
                    .setBlockId(blockId.byteStringValue)
                    .build()
            )
            FlowAccessApi.AccessApiCallResponse.Success(FlowSnapshot(ret.serializedSnapshot.toByteArray()))
        } catch (e: Exception) {
            FlowAccessApi.AccessApiCallResponse.Error("Failed to get protocol state snapshot by block ID", e)
        }

    override fun getProtocolStateSnapshotByHeight(height: Long): FlowAccessApi.AccessApiCallResponse<FlowSnapshot> =
        try {
            val ret = api.getProtocolStateSnapshotByHeight(
                Access.GetProtocolStateSnapshotByHeightRequest
                    .newBuilder()
                    .setBlockHeight(height)
                    .build()
            )
            FlowAccessApi.AccessApiCallResponse.Success(FlowSnapshot(ret.serializedSnapshot.toByteArray()))
        } catch (e: Exception) {
            FlowAccessApi.AccessApiCallResponse.Error("Failed to get protocol state snapshot by height", e)
        }

    override fun getNodeVersionInfo(): FlowAccessApi.AccessApiCallResponse<FlowNodeVersionInfo> =
        try {
            val ret = api.getNodeVersionInfo(
                Access.GetNodeVersionInfoRequest
                    .newBuilder()
                    .build()
            )

            val compatibleRange = if (ret.info.hasCompatibleRange()) {
                FlowCompatibleRange(ret.info.compatibleRange.startHeight, ret.info.compatibleRange.endHeight)
            } else {
                null
            }

            FlowAccessApi.AccessApiCallResponse.Success(FlowNodeVersionInfo(ret.info.semver, ret.info.commit, ret.info.sporkId.toByteArray(), ret.info.protocolVersion, ret.info.sporkRootBlockHeight, ret.info.nodeRootBlockHeight, compatibleRange))
        } catch (e: Exception) {
            FlowAccessApi.AccessApiCallResponse.Error("Failed to get node version info", e)
        }

    override fun getTransactionsByBlockId(id: FlowId): FlowAccessApi.AccessApiCallResponse<List<FlowTransaction>> =
        try {
            val ret = api.getTransactionsByBlockID(
                Access.GetTransactionsByBlockIDRequest
                    .newBuilder()
                    .setBlockId(id.byteStringValue)
                    .build()
            )
            FlowAccessApi.AccessApiCallResponse.Success(ret.transactionsList.map { FlowTransaction.of(it) })
        } catch (e: Exception) {
            FlowAccessApi.AccessApiCallResponse.Error("Failed to get transactions by block ID", e)
        }

    override fun getTransactionResultsByBlockId(id: FlowId): FlowAccessApi.AccessApiCallResponse<List<FlowTransactionResult>> =
        try {
            val ret = api.getTransactionResultsByBlockID(
                Access.GetTransactionsByBlockIDRequest
                    .newBuilder()
                    .setBlockId(id.byteStringValue)
                    .build()
            )
            FlowAccessApi.AccessApiCallResponse.Success(ret.transactionResultsList.map { FlowTransactionResult.of(it) })
        } catch (e: Exception) {
            FlowAccessApi.AccessApiCallResponse.Error("Failed to get transaction results by block ID", e)
        }

    override fun getExecutionResultByBlockId(id: FlowId): FlowAccessApi.AccessApiCallResponse<FlowExecutionResult> =
        try {
            val ret = api.getExecutionResultByID(
                Access.GetExecutionResultByIDRequest
                    .newBuilder()
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

    private fun <T, R, M> subscribeGeneric(
        scope: CoroutineScope,
        requestBuilder: () -> T,
        responseHandler: (T) -> Iterator<R>,
        responseMapper: (R) -> M
    ): Triple<ReceiveChannel<M>, ReceiveChannel<Throwable>, Job> {
        val responseChannel = Channel<M>(Channel.UNLIMITED)
        val errorChannel = Channel<Throwable>(Channel.UNLIMITED)

        val job = scope.launch {
            try {
                val request = requestBuilder()
                val responseIterator = responseHandler(request)

                for (response in responseIterator) {
                    responseChannel.send(responseMapper(response))
                }
            } catch (e: Exception) {
                errorChannel.send(e)
            } finally {
                responseChannel.close()
                errorChannel.close()
            }
        }
        return Triple(responseChannel, errorChannel, job)
    }

    override fun subscribeExecutionDataByBlockId(
        scope: CoroutineScope,
        blockId: FlowId
    ): Triple<ReceiveChannel<FlowBlockExecutionData>, ReceiveChannel<Throwable>, Job> =
        subscribeGeneric(
            scope,
            requestBuilder = {
                Executiondata.SubscribeExecutionDataFromStartBlockIDRequest
                    .newBuilder()
                    .setStartBlockId(blockId.byteStringValue)
                    .build()
            },
            responseHandler = { executionDataApi.subscribeExecutionDataFromStartBlockID(it) },
            responseMapper = { FlowBlockExecutionData.of(it.blockExecutionData) }
        )

    override fun subscribeExecutionDataByBlockHeight(
        scope: CoroutineScope,
        height: Long
    ): Triple<ReceiveChannel<FlowBlockExecutionData>, ReceiveChannel<Throwable>, Job> =
        subscribeGeneric(
            scope,
            requestBuilder = {
                Executiondata.SubscribeExecutionDataFromStartBlockHeightRequest
                    .newBuilder()
                    .setStartBlockHeight(height)
                    .build()
            },
            responseHandler = { executionDataApi.subscribeExecutionDataFromStartBlockHeight(it) },
            responseMapper = { FlowBlockExecutionData.of(it.blockExecutionData) }
        )

    override fun subscribeEventsByBlockId(
        scope: CoroutineScope,
        blockId: FlowId
    ): Triple<ReceiveChannel<List<FlowEvent>>, ReceiveChannel<Throwable>, Job> =
        subscribeGeneric(
            scope,
            requestBuilder = {
                Executiondata.SubscribeEventsFromStartBlockIDRequest
                    .newBuilder()
                    .setStartBlockId(blockId.byteStringValue)
                    .build()
            },
            responseHandler = { executionDataApi.subscribeEventsFromStartBlockID(it) },
            responseMapper = { it.eventsList.map { event -> FlowEvent.of(event) } }
        )

    override fun subscribeEventsByBlockHeight(
        scope: CoroutineScope,
        height: Long
    ): Triple<ReceiveChannel<List<FlowEvent>>, ReceiveChannel<Throwable>, Job> =
        subscribeGeneric(
            scope,
            requestBuilder = {
                Executiondata.SubscribeEventsFromStartHeightRequest
                    .newBuilder()
                    .setStartBlockHeight(height)
                    .build()
            },
            responseHandler = { executionDataApi.subscribeEventsFromStartHeight(it) },
            responseMapper = { it.eventsList.map { event -> FlowEvent.of(event) } }
        )
}
