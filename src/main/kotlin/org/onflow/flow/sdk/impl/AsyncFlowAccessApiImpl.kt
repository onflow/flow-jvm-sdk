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
    private val api: AccessAPIGrpc.AccessAPIFutureStub
) : AsyncFlowAccessApi, Closeable {
    override fun close() {
        val chan = api.channel
        if (chan is ManagedChannel) {
            chan.shutdownNow()
        }
    }

    override fun ping(): CompletableFuture<FlowAccessApi.FlowResult<Unit>> {
        return try {
            completableFuture(
                api.ping(Access.PingRequest.newBuilder().build())
            ).handle { _, ex ->
                if (ex != null) {
                    FlowAccessApi.FlowResult.Error("Failed to ping", ex)
                } else {
                    FlowAccessApi.FlowResult.Success(Unit)
                }
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(FlowAccessApi.FlowResult.Error("Failed to ping", e))
        }
    }

    override fun getLatestBlockHeader(sealed: Boolean): CompletableFuture<FlowAccessApi.FlowResult<FlowBlockHeader>> {
        return try {
            completableFuture(api.getLatestBlockHeader(
                Access.GetLatestBlockHeaderRequest.newBuilder().setIsSealed(sealed).build()
            )).handle { response, ex ->
                if (ex != null) {
                    FlowAccessApi.FlowResult.Error("Failed to get latest block header", ex)
                } else {
                    FlowAccessApi.FlowResult.Success(FlowBlockHeader.of(response.block))
                }
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(FlowAccessApi.FlowResult.Error("Failed to get latest block header", e))
        }
    }

    override fun getBlockHeaderById(id: FlowId): CompletableFuture<FlowAccessApi.FlowResult<FlowBlockHeader?>> {
        return try {
            completableFuture(
                api.getBlockHeaderByID(Access.GetBlockHeaderByIDRequest.newBuilder().setId(id.byteStringValue).build())
            ).handle { response, ex ->
                if (ex != null) {
                    FlowAccessApi.FlowResult.Error("Failed to get block header by ID", ex)
                } else {
                    if (response.hasBlock()) {
                        FlowAccessApi.FlowResult.Success(FlowBlockHeader.of(response.block))
                    } else {
                        FlowAccessApi.FlowResult.Error("Block header not found")
                    }
                }
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(FlowAccessApi.FlowResult.Error("Failed to get block header by ID", e))
        }
    }

    override fun getBlockHeaderByHeight(height: Long): CompletableFuture<FlowAccessApi.FlowResult<FlowBlockHeader?>> {
        return try {
            completableFuture(
                api.getBlockHeaderByHeight(Access.GetBlockHeaderByHeightRequest.newBuilder().setHeight(height).build())
            ).handle { response, ex ->
                if (ex != null) {
                    FlowAccessApi.FlowResult.Error("Failed to get block header by height", ex)
                } else {
                    if (response.hasBlock()) {
                        FlowAccessApi.FlowResult.Success(FlowBlockHeader.of(response.block))
                    } else {
                        FlowAccessApi.FlowResult.Error("Block header not found")
                    }
                }
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(FlowAccessApi.FlowResult.Error("Failed to get block header by height", e))
        }
    }

    override fun getLatestBlock(sealed: Boolean): CompletableFuture<FlowAccessApi.FlowResult<FlowBlock>> {
        return try {
            completableFuture(
                api.getLatestBlock(Access.GetLatestBlockRequest.newBuilder().setIsSealed(sealed).build())
            ).handle { response, ex ->
                if (ex != null) {
                    FlowAccessApi.FlowResult.Error("Failed to get latest block", ex)
                } else {
                    FlowAccessApi.FlowResult.Success(FlowBlock.of(response.block))
                }
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(FlowAccessApi.FlowResult.Error("Failed to get latest block", e))
        }
    }

    override fun getBlockById(id: FlowId): CompletableFuture<FlowAccessApi.FlowResult<FlowBlock?>> {
        return try {
            completableFuture(
                api.getBlockByID(Access.GetBlockByIDRequest.newBuilder().setId(id.byteStringValue).build())
            ).handle { response, ex ->
                if (ex != null) {
                    FlowAccessApi.FlowResult.Error("Failed to get block by ID", ex)
                } else {
                    if (response.hasBlock()) {
                        FlowAccessApi.FlowResult.Success(FlowBlock.of(response.block))
                    } else {
                        FlowAccessApi.FlowResult.Error("Block not found")
                    }
                }
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(FlowAccessApi.FlowResult.Error("Failed to get block by ID", e))
        }
    }

    override fun getBlockByHeight(height: Long): CompletableFuture<FlowAccessApi.FlowResult<FlowBlock?>> {
        return try {
            completableFuture(
                api.getBlockByHeight(Access.GetBlockByHeightRequest.newBuilder().setHeight(height).build())
            ).handle { response, ex ->
                if (ex != null) {
                    FlowAccessApi.FlowResult.Error("Failed to get block by height", ex)
                } else {
                    if (response.hasBlock()) {
                        FlowAccessApi.FlowResult.Success(FlowBlock.of(response.block))
                    } else {
                        FlowAccessApi.FlowResult.Error("Block not found")
                    }
                }
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(FlowAccessApi.FlowResult.Error("Failed to get block by height", e))
        }
    }

    override fun getCollectionById(id: FlowId): CompletableFuture<FlowAccessApi.FlowResult<FlowCollection?>> {
        return try {
            completableFuture(
                api.getCollectionByID(Access.GetCollectionByIDRequest.newBuilder().setId(id.byteStringValue).build())
            ).handle { response, ex ->
                if (ex != null) {
                    FlowAccessApi.FlowResult.Error("Failed to get collection by ID", ex)
                } else {
                    if (response.hasCollection()) {
                        FlowAccessApi.FlowResult.Success(FlowCollection.of(response.collection))
                    } else {
                        FlowAccessApi.FlowResult.Error("Collection not found")
                    }
                }
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(FlowAccessApi.FlowResult.Error("Failed to get collection by ID", e))
        }
    }

    override fun sendTransaction(transaction: FlowTransaction): CompletableFuture<FlowAccessApi.FlowResult<FlowId>> {
        return try {
            completableFuture(
                api.sendTransaction(Access.SendTransactionRequest.newBuilder().setTransaction(transaction.builder().build()).build())
            ).handle { response, ex ->
                if (ex != null) {
                    FlowAccessApi.FlowResult.Error("Failed to send transaction", ex)
                } else {
                    FlowAccessApi.FlowResult.Success(FlowId.of(response.id.toByteArray()))
                }
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(FlowAccessApi.FlowResult.Error("Failed to send transaction", e))
        }
    }

    override fun getTransactionById(id: FlowId): CompletableFuture<FlowAccessApi.FlowResult<FlowTransaction?>> {
        return try {
            completableFuture(
                api.getTransaction(Access.GetTransactionRequest.newBuilder().setId(id.byteStringValue).build())
            ).handle { response, ex ->
                if (ex != null) {
                    FlowAccessApi.FlowResult.Error("Failed to get transaction by ID", ex)
                } else {
                    if (response.hasTransaction()) {
                        FlowAccessApi.FlowResult.Success(FlowTransaction.of(response.transaction))
                    } else {
                        FlowAccessApi.FlowResult.Error("Transaction not found")
                    }
                }
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(FlowAccessApi.FlowResult.Error("Failed to get transaction by ID", e))
        }
    }

    override fun getTransactionResultById(id: FlowId): CompletableFuture<FlowAccessApi.FlowResult<FlowTransactionResult?>> {
        return try {
            completableFuture(
                api.getTransactionResult(Access.GetTransactionRequest.newBuilder().setId(id.byteStringValue).build())
            ).handle { response, ex ->
                if (ex != null) {
                    FlowAccessApi.FlowResult.Error("Failed to get transaction result by ID", ex)
                } else {
                    FlowAccessApi.FlowResult.Success(FlowTransactionResult.of(response))
                }
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(FlowAccessApi.FlowResult.Error("Failed to get transaction result by ID", e))
        }
    }

    override fun getTransactionsByBlockId(id: FlowId): CompletableFuture<FlowAccessApi.FlowResult<List<FlowTransaction>>> {
        return try {
            completableFuture(
                api.getTransactionsByBlockID(Access.GetTransactionsByBlockIDRequest.newBuilder().setBlockId(id.byteStringValue).build())
            ).handle { response, ex ->
                if (ex != null) {
                    FlowAccessApi.FlowResult.Error("Failed to get transactions by block ID", ex)
                } else {
                    FlowAccessApi.FlowResult.Success(response.transactionsList.map { FlowTransaction.of(it) })
                }
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(FlowAccessApi.FlowResult.Error("Failed to get transactions by block ID", e))
        }
    }

    override fun getTransactionResultsByBlockId(id: FlowId): CompletableFuture<FlowAccessApi.FlowResult<List<FlowTransactionResult>>> {
        return try {
            completableFuture(
                api.getTransactionResultsByBlockID(Access.GetTransactionsByBlockIDRequest.newBuilder().setBlockId(id.byteStringValue).build())
            ).handle { response, ex ->
                if (ex != null) {
                    FlowAccessApi.FlowResult.Error("Failed to get transaction results by block ID", ex)
                } else {
                    FlowAccessApi.FlowResult.Success(response.transactionResultsList.map { FlowTransactionResult.of(it) })
                }
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(FlowAccessApi.FlowResult.Error("Failed to get transaction results by block ID", e))
        }
    }

    override fun getExecutionResultByBlockId(id: FlowId): CompletableFuture<FlowAccessApi.FlowResult<ExecutionResult?>> {
        return try {
            completableFuture(
                api.getExecutionResultByID(Access.GetExecutionResultByIDRequest.newBuilder().setId(id.byteStringValue).build())
            ).handle { response, ex ->
                if (ex != null) {
                    FlowAccessApi.FlowResult.Error("Failed to get execution result by block ID", ex)
                } else {
                    if (response.hasExecutionResult()) {
                        FlowAccessApi.FlowResult.Success(ExecutionResult.of(response))
                    } else {
                        FlowAccessApi.FlowResult.Error("Execution result not found")
                    }
                }
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(FlowAccessApi.FlowResult.Error("Failed to get execution result by block ID", e))
        }
    }

    @Deprecated("Behaves identically to getAccountAtLatestBlock", replaceWith = ReplaceWith("getAccountAtLatestBlock"))
    override fun getAccountByAddress(addresss: FlowAddress): CompletableFuture<FlowAccessApi.FlowResult<FlowAccount>> {
        return try {
            completableFuture(
                api.getAccount(Access.GetAccountRequest.newBuilder().setAddress(addresss.byteStringValue).build())
            ).handle { response, ex ->
                if (ex != null) {
                    FlowAccessApi.FlowResult.Error("Failed to get account by address", ex)
                } else {
                    if (response.hasAccount()) {
                        FlowAccessApi.FlowResult.Success(FlowAccount.of(response.account))
                    } else {
                        FlowAccessApi.FlowResult.Error("Account not found")
                    }
                }
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(FlowAccessApi.FlowResult.Error("Failed to get account by address", e))
        }
    }

    override fun getAccountAtLatestBlock(addresss: FlowAddress): CompletableFuture<FlowAccessApi.FlowResult<FlowAccount>> {
        return try {
            completableFuture(
                api.getAccountAtLatestBlock(Access.GetAccountAtLatestBlockRequest.newBuilder().setAddress(addresss.byteStringValue).build())
            ).handle { response, ex ->
                if (ex != null) {
                    FlowAccessApi.FlowResult.Error("Failed to get account at latest block", ex)
                } else {
                    if (response.hasAccount()) {
                        FlowAccessApi.FlowResult.Success(FlowAccount.of(response.account))
                    } else {
                        FlowAccessApi.FlowResult.Error("Account not found")
                    }
                }
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(FlowAccessApi.FlowResult.Error("Failed to get account at latest block", e))
        }
    }

    override fun getAccountByBlockHeight(addresss: FlowAddress, height: Long): CompletableFuture<FlowAccessApi.FlowResult<FlowAccount>> {
        return try {
            completableFuture(
                api.getAccountAtBlockHeight(
                    Access.GetAccountAtBlockHeightRequest.newBuilder()
                        .setAddress(addresss.byteStringValue)
                        .setBlockHeight(height)
                        .build()
                )
            ).handle { response, ex ->
                if (ex != null) {
                    FlowAccessApi.FlowResult.Error("Failed to get account by block height", ex)
                } else {
                    if (response.hasAccount()) {
                        FlowAccessApi.FlowResult.Success(FlowAccount.of(response.account))
                    } else {
                        FlowAccessApi.FlowResult.Error("Account not found")
                    }
                }
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(FlowAccessApi.FlowResult.Error("Failed to get account by block height", e))
        }
    }

    override fun executeScriptAtLatestBlock(script: FlowScript, arguments: Iterable<ByteString>): CompletableFuture<FlowAccessApi.FlowResult<FlowScriptResponse>> {
        return try {
            completableFuture(
                api.executeScriptAtLatestBlock(
                    Access.ExecuteScriptAtLatestBlockRequest.newBuilder()
                        .setScript(script.byteStringValue)
                        .addAllArguments(arguments)
                        .build()
                )
            ).handle { response, ex ->
                if (ex != null) {
                    FlowAccessApi.FlowResult.Error("Failed to execute script at latest block", ex)
                } else {
                    FlowAccessApi.FlowResult.Success(FlowScriptResponse(response.value.toByteArray()))
                }
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(FlowAccessApi.FlowResult.Error("Failed to execute script at latest block", e))
        }
    }

    override fun executeScriptAtBlockId(script: FlowScript, blockId: FlowId, arguments: Iterable<ByteString>): CompletableFuture<FlowAccessApi.FlowResult<FlowScriptResponse>> {
        return try {
            completableFuture(
                api.executeScriptAtBlockID(
                    Access.ExecuteScriptAtBlockIDRequest.newBuilder()
                        .setBlockId(blockId.byteStringValue)
                        .setScript(script.byteStringValue)
                        .addAllArguments(arguments)
                        .build()
                )
            ).handle { response, ex ->
                if (ex != null) {
                    FlowAccessApi.FlowResult.Error("Failed to execute script at block ID", ex)
                } else {
                    FlowAccessApi.FlowResult.Success(FlowScriptResponse(response.value.toByteArray()))
                }
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(FlowAccessApi.FlowResult.Error("Failed to execute script at block ID", e))
        }
    }

    override fun executeScriptAtBlockHeight(script: FlowScript, height: Long, arguments: Iterable<ByteString>): CompletableFuture<FlowAccessApi.FlowResult<FlowScriptResponse>> {
        return try {
            completableFuture(
                api.executeScriptAtBlockHeight(
                    Access.ExecuteScriptAtBlockHeightRequest.newBuilder()
                        .setBlockHeight(height)
                        .setScript(script.byteStringValue)
                        .addAllArguments(arguments)
                        .build()
                )
            ).handle { response, ex ->
                if (ex != null) {
                    FlowAccessApi.FlowResult.Error("Failed to execute script at block height", ex)
                } else {
                    FlowAccessApi.FlowResult.Success(FlowScriptResponse(response.value.toByteArray()))
                }
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(FlowAccessApi.FlowResult.Error("Failed to execute script at block height", e))
        }
    }

    override fun getEventsForHeightRange(type: String, range: ClosedRange<Long>): CompletableFuture<FlowAccessApi.FlowResult<List<FlowEventResult>>> {
        return try {
            completableFuture(
                api.getEventsForHeightRange(
                    Access.GetEventsForHeightRangeRequest.newBuilder()
                        .setType(type)
                        .setStartHeight(range.start)
                        .setEndHeight(range.endInclusive)
                        .build()
                )
            ).handle { response, ex ->
                if (ex != null) {
                    FlowAccessApi.FlowResult.Error("Failed to get events for height range", ex)
                } else {
                    FlowAccessApi.FlowResult.Success(response.resultsList.map { FlowEventResult.of(it) })
                }
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(FlowAccessApi.FlowResult.Error("Failed to get events for height range", e))
        }
    }

    override fun getEventsForBlockIds(type: String, ids: Set<FlowId>): CompletableFuture<FlowAccessApi.FlowResult<List<FlowEventResult>>> {
        return try {
            completableFuture(
                api.getEventsForBlockIDs(
                    Access.GetEventsForBlockIDsRequest.newBuilder()
                        .setType(type)
                        .addAllBlockIds(ids.map { it.byteStringValue })
                        .build()
                )
            ).handle { response, ex ->
                if (ex != null) {
                    FlowAccessApi.FlowResult.Error("Failed to get events for block IDs", ex)
                } else {
                    FlowAccessApi.FlowResult.Success(response.resultsList.map { FlowEventResult.of(it) })
                }
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(FlowAccessApi.FlowResult.Error("Failed to get events for block IDs", e))
        }
    }

    override fun getNetworkParameters(): CompletableFuture<FlowAccessApi.FlowResult<FlowChainId>> {
        return try {
            completableFuture(
                api.getNetworkParameters(Access.GetNetworkParametersRequest.newBuilder().build())
            ).handle { response, ex ->
                if (ex != null) {
                    FlowAccessApi.FlowResult.Error("Failed to get network parameters", ex)
                } else {
                    FlowAccessApi.FlowResult.Success(FlowChainId.of(response.chainId))
                }
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(FlowAccessApi.FlowResult.Error("Failed to get network parameters", e))
        }
    }

    override fun getLatestProtocolStateSnapshot(): CompletableFuture<FlowAccessApi.FlowResult<FlowSnapshot>> {
        return try {
            completableFuture(
                api.getLatestProtocolStateSnapshot(Access.GetLatestProtocolStateSnapshotRequest.newBuilder().build())
            ).handle { response, ex ->
                if (ex != null) {
                    FlowAccessApi.FlowResult.Error("Failed to get latest protocol state snapshot", ex)
                } else {
                    FlowAccessApi.FlowResult.Success(FlowSnapshot(response.serializedSnapshot.toByteArray()))
                }
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(FlowAccessApi.FlowResult.Error("Failed to get latest protocol state snapshot", e))
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
