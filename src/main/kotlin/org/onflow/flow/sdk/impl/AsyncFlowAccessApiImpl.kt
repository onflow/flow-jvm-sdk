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
) : AsyncFlowAccessApi, Closeable {
    override fun close() {
        val chan = api.channel
        if (chan is ManagedChannel) {
            chan.shutdownNow()
        }
    }

    override fun ping(): CompletableFuture<Unit> {
        return completableFuture(
            api.ping(
                Access.PingRequest.newBuilder()
                    .build()
            )
        ).thenApply {
        }
    }

    override fun getLatestBlockHeader(sealed: Boolean): CompletableFuture<FlowBlockHeader> {
        return completableFuture(
            api.getLatestBlockHeader(
                Access.GetLatestBlockHeaderRequest.newBuilder()
                    .setIsSealed(sealed)
                    .build()
            )
        ).thenApply {
            FlowBlockHeader.of(it.block)
        }
    }

    override fun getBlockHeaderById(id: FlowId): CompletableFuture<FlowBlockHeader?> {
        return completableFuture(
            api.getBlockHeaderByID(
                Access.GetBlockHeaderByIDRequest.newBuilder()
                    .setId(id.byteStringValue)
                    .build()
            )
        ).thenApply {
            if (it.hasBlock()) {
                FlowBlockHeader.of(it.block)
            } else {
                null
            }
        }
    }

    override fun getBlockHeaderByHeight(height: Long): CompletableFuture<FlowBlockHeader?> {
        return completableFuture(
            api.getBlockHeaderByHeight(
                Access.GetBlockHeaderByHeightRequest.newBuilder()
                    .setHeight(height)
                    .build()
            )
        ).thenApply {
            if (it.hasBlock()) {
                FlowBlockHeader.of(it.block)
            } else {
                null
            }
        }
    }

    override fun getLatestBlock(sealed: Boolean): CompletableFuture<FlowBlock> {
        return completableFuture(
            api.getLatestBlock(
                Access.GetLatestBlockRequest.newBuilder()
                    .setIsSealed(sealed)
                    .build()
            )
        ).thenApply {
            FlowBlock.of(it.block)
        }
    }

    override fun getBlockById(id: FlowId): CompletableFuture<FlowBlock?> {
        return completableFuture(
            api.getBlockByID(
                Access.GetBlockByIDRequest.newBuilder()
                    .setId(id.byteStringValue)
                    .build()
            )
        ).thenApply {
            if (it.hasBlock()) {
                FlowBlock.of(it.block)
            } else {
                null
            }
        }
    }

    override fun getBlockByHeight(height: Long): CompletableFuture<FlowBlock?> {
        return completableFuture(
            api.getBlockByHeight(
                Access.GetBlockByHeightRequest.newBuilder()
                    .setHeight(height)
                    .build()
            )
        ).thenApply {
            if (it.hasBlock()) {
                FlowBlock.of(it.block)
            } else {
                null
            }
        }
    }

    override fun getCollectionById(id: FlowId): CompletableFuture<FlowCollection?> {
        return completableFuture(
            api.getCollectionByID(
                Access.GetCollectionByIDRequest.newBuilder()
                    .setId(id.byteStringValue)
                    .build()
            )
        ).thenApply {
            if (it.hasCollection()) {
                FlowCollection.of(it.collection)
            } else {
                null
            }
        }
    }

    override fun sendTransaction(transaction: FlowTransaction): CompletableFuture<FlowId> {
        return completableFuture(
            api.sendTransaction(
                Access.SendTransactionRequest.newBuilder()
                    .setTransaction(transaction.builder().build())
                    .build()
            )
        ).thenApply {
            FlowId.of(it.id.toByteArray())
        }
    }

    override fun getTransactionById(id: FlowId): CompletableFuture<FlowTransaction?> {
        return completableFuture(
            api.getTransaction(
                Access.GetTransactionRequest.newBuilder()
                    .setId(id.byteStringValue)
                    .build()
            )
        ).thenApply {
            if (it.hasTransaction()) {
                FlowTransaction.of(it.transaction)
            } else {
                null
            }
        }
    }

    override fun getTransactionResultById(id: FlowId): CompletableFuture<FlowTransactionResult?> {
        return completableFuture(
            api.getTransactionResult(
                Access.GetTransactionRequest.newBuilder()
                    .setId(id.byteStringValue)
                    .build()
            )
        ).thenApply {
            FlowTransactionResult.of(it)
        }
    }

    override fun getTransactionsByBlockId(id: FlowId): CompletableFuture<List<FlowTransaction>> {
        return completableFuture(
            api.getTransactionsByBlockID(
                Access.GetTransactionsByBlockIDRequest.newBuilder()
                    .setBlockId(id.byteStringValue)
                    .build()
            )
        ).thenApply { transactionsResponse ->
            transactionsResponse.transactionsList.map { FlowTransaction.of(it) }
        }
    }

    override fun getTransactionResultsByBlockId(id: FlowId): CompletableFuture<List<FlowTransactionResult>> {
        return completableFuture(
            api.getTransactionResultsByBlockID(
                Access.GetTransactionsByBlockIDRequest.newBuilder()
                    .setBlockId(id.byteStringValue)
                    .build()
            )
        ).thenApply { transactionResultsResponse ->
            transactionResultsResponse.transactionResultsList.map { FlowTransactionResult.of(it) }
        }
    }

    override fun getExecutionResultByBlockId(id: FlowId): CompletableFuture<ExecutionResult?> {
        return completableFuture(
            api.getExecutionResultByID(
                Access.GetExecutionResultByIDRequest.newBuilder()
                    .setId(id.byteStringValue)
                    .build()
            )
        ).thenApply {
            if (it.hasExecutionResult()) {
                ExecutionResult.of(it)
            } else {
                null
            }
        }
    }

    @Deprecated("Behaves identically to getAccountAtLatestBlock", replaceWith = ReplaceWith("getAccountAtLatestBlock"))
    override fun getAccountByAddress(addresss: FlowAddress): CompletableFuture<FlowAccount?> {
        return completableFuture(
            api.getAccount(
                Access.GetAccountRequest.newBuilder()
                    .setAddress(addresss.byteStringValue)
                    .build()
            )
        ).thenApply {
            if (it.hasAccount()) {
                FlowAccount.of(it.account)
            } else {
                null
            }
        }
    }

    override fun getAccountAtLatestBlock(addresss: FlowAddress): CompletableFuture<FlowAccount?> {
        return completableFuture(
            api.getAccountAtLatestBlock(
                Access.GetAccountAtLatestBlockRequest.newBuilder()
                    .setAddress(addresss.byteStringValue)
                    .build()
            )
        ).thenApply {
            if (it.hasAccount()) {
                FlowAccount.of(it.account)
            } else {
                null
            }
        }
    }

    override fun getAccountByBlockHeight(addresss: FlowAddress, height: Long): CompletableFuture<FlowAccount?> {
        return completableFuture(
            api.getAccountAtBlockHeight(
                Access.GetAccountAtBlockHeightRequest.newBuilder()
                    .setAddress(addresss.byteStringValue)
                    .setBlockHeight(height)
                    .build()
            )
        ).thenApply {
            if (it.hasAccount()) {
                FlowAccount.of(it.account)
            } else {
                null
            }
        }
    }

    override fun executeScriptAtLatestBlock(script: FlowScript, arguments: Iterable<ByteString>): CompletableFuture<FlowScriptResponse> {
        return completableFuture(
            api.executeScriptAtLatestBlock(
                Access.ExecuteScriptAtLatestBlockRequest.newBuilder()
                    .setScript(script.byteStringValue)
                    .addAllArguments(arguments)
                    .build()
            )
        ).thenApply {
            FlowScriptResponse(it.value.toByteArray())
        }
    }

    override fun executeScriptAtBlockId(script: FlowScript, blockId: FlowId, arguments: Iterable<ByteString>): CompletableFuture<FlowScriptResponse> {
        return completableFuture(
            api.executeScriptAtBlockID(
                Access.ExecuteScriptAtBlockIDRequest.newBuilder()
                    .setBlockId(blockId.byteStringValue)
                    .setScript(script.byteStringValue)
                    .addAllArguments(arguments)
                    .build()
            )
        ).thenApply {
            FlowScriptResponse(it.value.toByteArray())
        }
    }

    override fun executeScriptAtBlockHeight(
        script: FlowScript,
        height: Long,
        arguments: Iterable<ByteString>
    ): CompletableFuture<FlowScriptResponse> {
        return completableFuture(
            api.executeScriptAtBlockHeight(
                Access.ExecuteScriptAtBlockHeightRequest.newBuilder()
                    .setBlockHeight(height)
                    .setScript(script.byteStringValue)
                    .addAllArguments(arguments)
                    .build()
            )
        ).thenApply {
            FlowScriptResponse(it.value.toByteArray())
        }
    }

    override fun getEventsForHeightRange(
        type: String,
        range: ClosedRange<Long>
    ): CompletableFuture<List<FlowEventResult>> {
        return completableFuture(
            api.getEventsForHeightRange(
                Access.GetEventsForHeightRangeRequest.newBuilder()
                    .setType(type)
                    .setStartHeight(range.start)
                    .setEndHeight(range.endInclusive)
                    .build()
            )
        ).thenApply {
            it.resultsList
                .map { FlowEventResult.of(it) }
        }
    }

    override fun getEventsForBlockIds(type: String, ids: Set<FlowId>): CompletableFuture<List<FlowEventResult>> {
        return completableFuture(
            api.getEventsForBlockIDs(
                Access.GetEventsForBlockIDsRequest.newBuilder()
                    .setType(type)
                    .addAllBlockIds(ids.map { it.byteStringValue })
                    .build()
            )
        ).thenApply {
            it.resultsList
                .map { FlowEventResult.of(it) }
        }
    }

    override fun getNetworkParameters(): CompletableFuture<FlowChainId> {
        return completableFuture(
            api.getNetworkParameters(
                Access.GetNetworkParametersRequest.newBuilder()
                    .build()
            )
        ).thenApply {
            FlowChainId.of(it.chainId)
        }
    }

    override fun getLatestProtocolStateSnapshot(): CompletableFuture<FlowSnapshot> {
        return completableFuture(
            api.getLatestProtocolStateSnapshot(
                Access.GetLatestProtocolStateSnapshotRequest.newBuilder()
                    .build()
            )
        ).thenApply {
            FlowSnapshot(it.serializedSnapshot.toByteArray())
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

