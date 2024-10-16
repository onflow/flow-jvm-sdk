package org.onflow.flow.sdk.impl

import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.google.protobuf.ByteString
import org.onflow.flow.sdk.*
import io.grpc.ManagedChannel
import org.onflow.protobuf.access.Access
import org.onflow.protobuf.access.AccessAPIGrpc
import java.io.Closeable
import java.util.concurrent.CompletableFuture

class AsyncFlowAccessApiImpl(
    private val api: AccessAPIGrpc.AccessAPIFutureStub,
) : AsyncFlowAccessApi,
    Closeable {
    override fun close() {
        val chan = api.channel
        if (chan is ManagedChannel) {
            chan.shutdownNow()
        }
    }

    override fun ping(): CompletableFuture<FlowAccessApi.AccessApiCallResponse<Unit>> {
        return try {
            completableFuture(
                try {
                    api.ping(Access.PingRequest.newBuilder().build())
                } catch (e: Exception) {
                    return CompletableFuture.completedFuture(FlowAccessApi.AccessApiCallResponse.Error("Failed to ping", e))
                }
            ).handle { _, ex ->
                if (ex != null) {
                    FlowAccessApi.AccessApiCallResponse.Error("Failed to ping", ex)
                } else {
                    FlowAccessApi.AccessApiCallResponse.Success(Unit)
                }
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(FlowAccessApi.AccessApiCallResponse.Error("Failed to ping", e))
        }
    }

    override fun getLatestBlockHeader(sealed: Boolean): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowBlockHeader>> {
        return try {
            completableFuture(
                try {
                    api
                        .getLatestBlockHeader(
                            Access.GetLatestBlockHeaderRequest
                                .newBuilder()
                                .setIsSealed(sealed)
                                .build()
                        )
                } catch (e: Exception) {
                    return CompletableFuture.completedFuture(FlowAccessApi.AccessApiCallResponse.Error("Failed to get latest block header", e))
                }
            ).handle { response, ex ->
                if (ex != null) {
                    FlowAccessApi.AccessApiCallResponse.Error("Failed to get latest block header", ex)
                } else {
                    FlowAccessApi.AccessApiCallResponse.Success(FlowBlockHeader.of(response.block))
                }
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(FlowAccessApi.AccessApiCallResponse.Error("Failed to get latest block header", e))
        }
    }

    override fun getBlockHeaderById(id: FlowId): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowBlockHeader?>> {
        return try {
            completableFuture(
                try {
                    api.getBlockHeaderByID(
                        Access.GetBlockHeaderByIDRequest
                            .newBuilder()
                            .setId(id.byteStringValue)
                            .build()
                    )
                } catch (e: Exception) {
                    return CompletableFuture.completedFuture(FlowAccessApi.AccessApiCallResponse.Error("Failed to get block header by ID", e))
                }
            ).handle { response, ex ->
                if (ex != null) {
                    FlowAccessApi.AccessApiCallResponse.Error("Failed to get block header by ID", ex)
                } else {
                    if (response.hasBlock()) {
                        FlowAccessApi.AccessApiCallResponse.Success(FlowBlockHeader.of(response.block))
                    } else {
                        FlowAccessApi.AccessApiCallResponse.Error("Block header not found")
                    }
                }
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(FlowAccessApi.AccessApiCallResponse.Error("Failed to get block header by ID", e))
        }
    }

    override fun getBlockHeaderByHeight(height: Long): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowBlockHeader?>> {
        return try {
            completableFuture(
                try {
                    api.getBlockHeaderByHeight(
                        Access.GetBlockHeaderByHeightRequest
                            .newBuilder()
                            .setHeight(height)
                            .build()
                    )
                } catch (e: Exception) {
                    return CompletableFuture.completedFuture(FlowAccessApi.AccessApiCallResponse.Error("Failed to get block header by height", e))
                }
            ).handle { response, ex ->
                if (ex != null) {
                    FlowAccessApi.AccessApiCallResponse.Error("Failed to get block header by height", ex)
                } else {
                    if (response.hasBlock()) {
                        FlowAccessApi.AccessApiCallResponse.Success(FlowBlockHeader.of(response.block))
                    } else {
                        FlowAccessApi.AccessApiCallResponse.Error("Block header not found")
                    }
                }
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(FlowAccessApi.AccessApiCallResponse.Error("Failed to get block header by height", e))
        }
    }

    override fun getLatestBlock(sealed: Boolean): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowBlock>> {
        return try {
            completableFuture(
                try {
                    api.getLatestBlock(
                        Access.GetLatestBlockRequest
                            .newBuilder()
                            .setIsSealed(sealed)
                            .build()
                    )
                } catch (e: Exception) {
                    return CompletableFuture.completedFuture(FlowAccessApi.AccessApiCallResponse.Error("Failed to get latest block", e))
                }
            ).handle { response, ex ->
                if (ex != null) {
                    FlowAccessApi.AccessApiCallResponse.Error("Failed to get latest block", ex)
                } else {
                    FlowAccessApi.AccessApiCallResponse.Success(FlowBlock.of(response.block))
                }
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(FlowAccessApi.AccessApiCallResponse.Error("Failed to get latest block", e))
        }
    }

    override fun getBlockById(id: FlowId): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowBlock?>> {
        return try {
            completableFuture(
                try {
                    api.getBlockByID(
                        Access.GetBlockByIDRequest
                            .newBuilder()
                            .setId(id.byteStringValue)
                            .build()
                    )
                } catch (e: Exception) {
                    return CompletableFuture.completedFuture(FlowAccessApi.AccessApiCallResponse.Error("Failed to get block by ID", e))
                }
            ).handle { response, ex ->
                if (ex != null) {
                    FlowAccessApi.AccessApiCallResponse.Error("Failed to get block by ID", ex)
                } else {
                    if (response.hasBlock()) {
                        FlowAccessApi.AccessApiCallResponse.Success(FlowBlock.of(response.block))
                    } else {
                        FlowAccessApi.AccessApiCallResponse.Error("Block not found")
                    }
                }
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(FlowAccessApi.AccessApiCallResponse.Error("Failed to get block by ID", e))
        }
    }

    override fun getBlockByHeight(height: Long): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowBlock?>> {
        return try {
            completableFuture(
                try {
                    api.getBlockByHeight(
                        Access.GetBlockByHeightRequest
                            .newBuilder()
                            .setHeight(height)
                            .build()
                    )
                } catch (e: Exception) {
                    return CompletableFuture.completedFuture(FlowAccessApi.AccessApiCallResponse.Error("Failed to get block by height", e))
                }
            ).handle { response, ex ->
                if (ex != null) {
                    FlowAccessApi.AccessApiCallResponse.Error("Failed to get block by height", ex)
                } else {
                    if (response.hasBlock()) {
                        FlowAccessApi.AccessApiCallResponse.Success(FlowBlock.of(response.block))
                    } else {
                        FlowAccessApi.AccessApiCallResponse.Error("Block not found")
                    }
                }
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(FlowAccessApi.AccessApiCallResponse.Error("Failed to get block by height", e))
        }
    }

    override fun getCollectionById(id: FlowId): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowCollection?>> {
        return try {
            completableFuture(
                try {
                    api.getCollectionByID(
                        Access.GetCollectionByIDRequest
                            .newBuilder()
                            .setId(id.byteStringValue)
                            .build()
                    )
                } catch (e: Exception) {
                    return CompletableFuture.completedFuture(FlowAccessApi.AccessApiCallResponse.Error("Failed to get collection by ID", e))
                }
            ).handle { response, ex ->
                if (ex != null) {
                    FlowAccessApi.AccessApiCallResponse.Error("Failed to get collection by ID", ex)
                } else {
                    if (response.hasCollection()) {
                        FlowAccessApi.AccessApiCallResponse.Success(FlowCollection.of(response.collection))
                    } else {
                        FlowAccessApi.AccessApiCallResponse.Error("Collection not found")
                    }
                }
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(FlowAccessApi.AccessApiCallResponse.Error("Failed to get collection by ID", e))
        }
    }

    override fun sendTransaction(transaction: FlowTransaction): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowId>> {
        return try {
            completableFuture(
                try {
                    api.sendTransaction(
                        Access.SendTransactionRequest
                            .newBuilder()
                            .setTransaction(transaction.builder().build())
                            .build()
                    )
                } catch (e: Exception) {
                    return CompletableFuture.completedFuture(FlowAccessApi.AccessApiCallResponse.Error("Failed to send transaction", e))
                }
            ).handle { response, ex ->
                if (ex != null) {
                    FlowAccessApi.AccessApiCallResponse.Error("Failed to send transaction", ex)
                } else {
                    FlowAccessApi.AccessApiCallResponse.Success(FlowId.of(response.id.toByteArray()))
                }
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(FlowAccessApi.AccessApiCallResponse.Error("Failed to send transaction", e))
        }
    }

    override fun getTransactionById(id: FlowId): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowTransaction?>> {
        return try {
            completableFuture(
                try {
                    api.getTransaction(
                        Access.GetTransactionRequest
                            .newBuilder()
                            .setId(id.byteStringValue)
                            .build()
                    )
                } catch (e: Exception) {
                    return CompletableFuture.completedFuture(FlowAccessApi.AccessApiCallResponse.Error("Failed to get transaction by ID", e))
                }
            ).handle { response, ex ->
                if (ex != null) {
                    FlowAccessApi.AccessApiCallResponse.Error("Failed to get transaction by ID", ex)
                } else {
                    if (response.hasTransaction()) {
                        FlowAccessApi.AccessApiCallResponse.Success(FlowTransaction.of(response.transaction))
                    } else {
                        FlowAccessApi.AccessApiCallResponse.Error("Transaction not found")
                    }
                }
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(FlowAccessApi.AccessApiCallResponse.Error("Failed to get transaction by ID", e))
        }
    }

    override fun getTransactionResultById(id: FlowId): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowTransactionResult?>> {
        return try {
            completableFuture(
                try {
                    api.getTransactionResult(
                        Access.GetTransactionRequest
                            .newBuilder()
                            .setId(id.byteStringValue)
                            .build()
                    )
                } catch (e: Exception) {
                    return CompletableFuture.completedFuture(FlowAccessApi.AccessApiCallResponse.Error("Failed to get transaction result by ID", e))
                }
            ).handle { response, ex ->
                if (ex != null) {
                    FlowAccessApi.AccessApiCallResponse.Error("Failed to get transaction result by ID", ex)
                } else {
                    FlowAccessApi.AccessApiCallResponse.Success(FlowTransactionResult.of(response))
                }
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(FlowAccessApi.AccessApiCallResponse.Error("Failed to get transaction result by ID", e))
        }
    }

    @Deprecated("Behaves identically to getAccountAtLatestBlock", replaceWith = ReplaceWith("getAccountAtLatestBlock"))
    override fun getAccountByAddress(addresss: FlowAddress): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowAccount>> {
        return try {
            completableFuture(
                try {
                    api.getAccount(
                        Access.GetAccountRequest
                            .newBuilder()
                            .setAddress(addresss.byteStringValue)
                            .build()
                    )
                } catch (e: Exception) {
                    return CompletableFuture.completedFuture(FlowAccessApi.AccessApiCallResponse.Error("Failed to get account by address", e))
                }
            ).handle { response, ex ->
                if (ex != null) {
                    FlowAccessApi.AccessApiCallResponse.Error("Failed to get account by address", ex)
                } else {
                    if (response.hasAccount()) {
                        FlowAccessApi.AccessApiCallResponse.Success(FlowAccount.of(response.account))
                    } else {
                        FlowAccessApi.AccessApiCallResponse.Error("Account not found")
                    }
                }
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(FlowAccessApi.AccessApiCallResponse.Error("Failed to get account by address", e))
        }
    }

    override fun getAccountAtLatestBlock(addresss: FlowAddress): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowAccount>> {
        return try {
            completableFuture(
                try {
                    api.getAccountAtLatestBlock(
                        Access.GetAccountAtLatestBlockRequest
                            .newBuilder()
                            .setAddress(addresss.byteStringValue)
                            .build()
                    )
                } catch (e: Exception) {
                    return CompletableFuture.completedFuture(FlowAccessApi.AccessApiCallResponse.Error("Failed to get account at latest block", e))
                }
            ).handle { response, ex ->
                if (ex != null) {
                    FlowAccessApi.AccessApiCallResponse.Error("Failed to get account at latest block", ex)
                } else {
                    if (response.hasAccount()) {
                        FlowAccessApi.AccessApiCallResponse.Success(FlowAccount.of(response.account))
                    } else {
                        FlowAccessApi.AccessApiCallResponse.Error("Account not found")
                    }
                }
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(FlowAccessApi.AccessApiCallResponse.Error("Failed to get account at latest block", e))
        }
    }

    override fun getAccountByBlockHeight(addresss: FlowAddress, height: Long): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowAccount>> {
        return try {
            completableFuture(
                try {
                    api.getAccountAtBlockHeight(
                        Access.GetAccountAtBlockHeightRequest
                            .newBuilder()
                            .setAddress(addresss.byteStringValue)
                            .setBlockHeight(height)
                            .build()
                    )
                } catch (e: Exception) {
                    return CompletableFuture.completedFuture(FlowAccessApi.AccessApiCallResponse.Error("Failed to get account by block height", e))
                }
            ).handle { response, ex ->
                if (ex != null) {
                    FlowAccessApi.AccessApiCallResponse.Error("Failed to get account by block height", ex)
                } else {
                    if (response.hasAccount()) {
                        FlowAccessApi.AccessApiCallResponse.Success(FlowAccount.of(response.account))
                    } else {
                        FlowAccessApi.AccessApiCallResponse.Error("Account not found")
                    }
                }
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(FlowAccessApi.AccessApiCallResponse.Error("Failed to get account by block height", e))
        }
    }

    override fun executeScriptAtLatestBlock(script: FlowScript, arguments: Iterable<ByteString>): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowScriptResponse>> {
        return try {
            completableFuture(
                try {
                    api.executeScriptAtLatestBlock(
                        Access.ExecuteScriptAtLatestBlockRequest
                            .newBuilder()
                            .setScript(script.byteStringValue)
                            .addAllArguments(arguments)
                            .build()
                    )
                } catch (e: Exception) {
                    return CompletableFuture.completedFuture(FlowAccessApi.AccessApiCallResponse.Error("Failed to execute script at latest block", e))
                }
            ).handle { response, ex ->
                if (ex != null) {
                    FlowAccessApi.AccessApiCallResponse.Error("Failed to execute script at latest block", ex)
                } else {
                    FlowAccessApi.AccessApiCallResponse.Success(FlowScriptResponse(response.value.toByteArray()))
                }
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(FlowAccessApi.AccessApiCallResponse.Error("Failed to execute script at latest block", e))
        }
    }

    override fun executeScriptAtBlockId(script: FlowScript, blockId: FlowId, arguments: Iterable<ByteString>): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowScriptResponse>> {
        return try {
            completableFuture(
                try {
                    api.executeScriptAtBlockID(
                        Access.ExecuteScriptAtBlockIDRequest
                            .newBuilder()
                            .setBlockId(blockId.byteStringValue)
                            .setScript(script.byteStringValue)
                            .addAllArguments(arguments)
                            .build()
                    )
                } catch (e: Exception) {
                    return CompletableFuture.completedFuture(FlowAccessApi.AccessApiCallResponse.Error("Failed to execute script at block ID", e))
                }
            ).handle { response, ex ->
                if (ex != null) {
                    FlowAccessApi.AccessApiCallResponse.Error("Failed to execute script at block ID", ex)
                } else {
                    FlowAccessApi.AccessApiCallResponse.Success(FlowScriptResponse(response.value.toByteArray()))
                }
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(FlowAccessApi.AccessApiCallResponse.Error("Failed to execute script at block ID", e))
        }
    }

    override fun executeScriptAtBlockHeight(script: FlowScript, height: Long, arguments: Iterable<ByteString>): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowScriptResponse>> {
        return try {
            completableFuture(
                try {
                    api.executeScriptAtBlockHeight(
                        Access.ExecuteScriptAtBlockHeightRequest
                            .newBuilder()
                            .setBlockHeight(height)
                            .setScript(script.byteStringValue)
                            .addAllArguments(arguments)
                            .build()
                    )
                } catch (e: Exception) {
                    return CompletableFuture.completedFuture(FlowAccessApi.AccessApiCallResponse.Error("Failed to execute script at block height", e))
                }
            ).handle { response, ex ->
                if (ex != null) {
                    FlowAccessApi.AccessApiCallResponse.Error("Failed to execute script at block height", ex)
                } else {
                    FlowAccessApi.AccessApiCallResponse.Success(FlowScriptResponse(response.value.toByteArray()))
                }
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(FlowAccessApi.AccessApiCallResponse.Error("Failed to execute script at block height", e))
        }
    }

    override fun getEventsForHeightRange(type: String, range: ClosedRange<Long>): CompletableFuture<FlowAccessApi.AccessApiCallResponse<List<FlowEventResult>>> {
        return try {
            completableFuture(
                try {
                    api.getEventsForHeightRange(
                        Access.GetEventsForHeightRangeRequest
                            .newBuilder()
                            .setType(type)
                            .setStartHeight(range.start)
                            .setEndHeight(range.endInclusive)
                            .build()
                    )
                } catch (e: Exception) {
                    return CompletableFuture.completedFuture(FlowAccessApi.AccessApiCallResponse.Error("Failed to get events for height range", e))
                }
            ).handle { response, ex ->
                if (ex != null) {
                    FlowAccessApi.AccessApiCallResponse.Error("Failed to get events for height range", ex)
                } else {
                    FlowAccessApi.AccessApiCallResponse.Success(response.resultsList.map { FlowEventResult.of(it) })
                }
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(FlowAccessApi.AccessApiCallResponse.Error("Failed to get events for height range", e))
        }
    }

    override fun getEventsForBlockIds(type: String, ids: Set<FlowId>): CompletableFuture<FlowAccessApi.AccessApiCallResponse<List<FlowEventResult>>> {
        return try {
            completableFuture(
                try {
                    api.getEventsForBlockIDs(
                        Access.GetEventsForBlockIDsRequest
                            .newBuilder()
                            .setType(type)
                            .addAllBlockIds(ids.map { it.byteStringValue })
                            .build()
                    )
                } catch (e: Exception) {
                    return CompletableFuture.completedFuture(FlowAccessApi.AccessApiCallResponse.Error("Failed to get events for block IDs", e))
                }
            ).handle { response, ex ->
                if (ex != null) {
                    FlowAccessApi.AccessApiCallResponse.Error("Failed to get events for block IDs", ex)
                } else {
                    FlowAccessApi.AccessApiCallResponse.Success(response.resultsList.map { FlowEventResult.of(it) })
                }
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(FlowAccessApi.AccessApiCallResponse.Error("Failed to get events for block IDs", e))
        }
    }

    override fun getNetworkParameters(): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowChainId>> {
        return try {
            completableFuture(
                try {
                    api.getNetworkParameters(
                        Access.GetNetworkParametersRequest
                            .newBuilder()
                            .build()
                    )
                } catch (e: Exception) {
                    return CompletableFuture.completedFuture(FlowAccessApi.AccessApiCallResponse.Error("Failed to get network parameters", e))
                }
            ).handle { response, ex ->
                if (ex != null) {
                    FlowAccessApi.AccessApiCallResponse.Error("Failed to get network parameters", ex)
                } else {
                    FlowAccessApi.AccessApiCallResponse.Success(FlowChainId.of(response.chainId))
                }
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(FlowAccessApi.AccessApiCallResponse.Error("Failed to get network parameters", e))
        }
    }

    override fun getLatestProtocolStateSnapshot(): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowSnapshot>> {
        return try {
            completableFuture(
                try {
                    api.getLatestProtocolStateSnapshot(
                        Access.GetLatestProtocolStateSnapshotRequest
                            .newBuilder()
                            .build()
                    )
                } catch (e: Exception) {
                    return CompletableFuture.completedFuture(FlowAccessApi.AccessApiCallResponse.Error("Failed to get latest protocol state snapshot", e))
                }
            ).handle { response, ex ->
                if (ex != null) {
                    FlowAccessApi.AccessApiCallResponse.Error("Failed to get latest protocol state snapshot", ex)
                } else {
                    FlowAccessApi.AccessApiCallResponse.Success(FlowSnapshot(response.serializedSnapshot.toByteArray()))
                }
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(FlowAccessApi.AccessApiCallResponse.Error("Failed to get latest protocol state snapshot", e))
        }
    }

    override fun getTransactionsByBlockId(id: FlowId): CompletableFuture<FlowAccessApi.AccessApiCallResponse<List<FlowTransaction>>> {
        return try {
            completableFuture(
                try {
                    api.getTransactionsByBlockID(
                        Access.GetTransactionsByBlockIDRequest
                            .newBuilder()
                            .setBlockId(id.byteStringValue)
                            .build()
                    )
                } catch (e: Exception) {
                    return CompletableFuture.completedFuture(FlowAccessApi.AccessApiCallResponse.Error("Failed to get transactions by block ID", e))
                }
            ).handle { response, ex ->
                if (ex != null) {
                    FlowAccessApi.AccessApiCallResponse.Error("Failed to get transactions by block ID", ex)
                } else {
                    FlowAccessApi.AccessApiCallResponse.Success(response.transactionsList.map { FlowTransaction.of(it) })
                }
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(FlowAccessApi.AccessApiCallResponse.Error("Failed to get transactions by block ID", e))
        }
    }

    override fun getTransactionResultsByBlockId(id: FlowId): CompletableFuture<FlowAccessApi.AccessApiCallResponse<List<FlowTransactionResult>>> {
        return try {
            completableFuture(
                try {
                    api.getTransactionResultsByBlockID(
                        Access.GetTransactionsByBlockIDRequest
                            .newBuilder()
                            .setBlockId(id.byteStringValue)
                            .build()
                    )
                } catch (e: Exception) {
                    return CompletableFuture.completedFuture(FlowAccessApi.AccessApiCallResponse.Error("Failed to get transaction results by block ID", e))
                }
            ).handle { response, ex ->
                if (ex != null) {
                    FlowAccessApi.AccessApiCallResponse.Error("Failed to get transaction results by block ID", ex)
                } else {
                    FlowAccessApi.AccessApiCallResponse.Success(response.transactionResultsList.map { FlowTransactionResult.of(it) })
                }
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(FlowAccessApi.AccessApiCallResponse.Error("Failed to get transaction results by block ID", e))
        }
    }

    override fun getExecutionResultByBlockId(id: FlowId): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowExecutionResult?>> {
        return try {
            completableFuture(
                try {
                    api.getExecutionResultByID(
                        Access.GetExecutionResultByIDRequest
                            .newBuilder()
                            .setId(id.byteStringValue)
                            .build()
                    )
                } catch (e: Exception) {
                    return CompletableFuture.completedFuture(FlowAccessApi.AccessApiCallResponse.Error("Failed to get execution result by block ID", e))
                }
            ).handle { response, ex ->
                if (ex != null) {
                    FlowAccessApi.AccessApiCallResponse.Error("Failed to get execution result by block ID", ex)
                } else {
                    if (response.hasExecutionResult()) {
                        FlowAccessApi.AccessApiCallResponse.Success(FlowExecutionResult.of(response))
                    } else {
                        FlowAccessApi.AccessApiCallResponse.Error("Execution result not found")
                    }
                }
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(FlowAccessApi.AccessApiCallResponse.Error("Failed to get execution result by block ID", e))
        }
    }
}

fun <T> completableFuture(future: ListenableFuture<T>): CompletableFuture<T> {
    val completable: CompletableFuture<T> = object : CompletableFuture<T>() {
        override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
            val result: Boolean = future.cancel(mayInterruptIfRunning)
            super.cancel(mayInterruptIfRunning)
            return result
        }
    }
    Futures.addCallback(
        future,
        object : FutureCallback<T> {
            override fun onSuccess(result: T?) {
                completable.complete(result)
            }

            override fun onFailure(t: Throwable) {
                completable.completeExceptionally(t)
            }
        },
        MoreExecutors.directExecutor()
    )
    return completable
}
