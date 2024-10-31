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

    private fun <T, R> handleApiCall(
        apiCall: () -> ListenableFuture<T>,
        transform: (T) -> R,
        errorMessage: String
    ): CompletableFuture<FlowAccessApi.AccessApiCallResponse<R>> =
        try {
            completableFuture(apiCall()).handle { response, ex ->
                if (ex != null) {
                    FlowAccessApi.AccessApiCallResponse.Error(errorMessage, ex)
                } else {
                    try {
                        FlowAccessApi.AccessApiCallResponse.Success(transform(response))
                    } catch (e: Exception) {
                        FlowAccessApi.AccessApiCallResponse.Error(errorMessage, e)
                    }
                }
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(FlowAccessApi.AccessApiCallResponse.Error(errorMessage, e))
        }

    override fun ping(): CompletableFuture<FlowAccessApi.AccessApiCallResponse<Unit>> =
        handleApiCall(
            apiCall = { api.ping(Access.PingRequest.newBuilder().build()) },
            transform = { Unit },
            errorMessage = "Failed to ping"
        )

    @Deprecated("Behaves identically to getAccountAtLatestBlock", replaceWith = ReplaceWith("getAccountAtLatestBlock"))
    override fun getAccountByAddress(address: FlowAddress): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowAccount>> =
        handleApiCall(
            apiCall = {
                api.getAccount(
                    Access.GetAccountRequest
                        .newBuilder()
                        .setAddress(address.byteStringValue)
                        .build()
                )
            },
            transform = { if (it.hasAccount()) FlowAccount.of(it.account) else throw IllegalStateException("Account not found") },
            errorMessage = "Failed to get account by address"
        )

    override fun getAccountAtLatestBlock(address: FlowAddress): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowAccount>> =
        handleApiCall(
            apiCall = {
                api.getAccountAtLatestBlock(
                    Access.GetAccountAtLatestBlockRequest
                        .newBuilder()
                        .setAddress(address.byteStringValue)
                        .build()
                )
            },
            transform = { if (it.hasAccount()) FlowAccount.of(it.account) else throw IllegalStateException("Account not found") },
            errorMessage = "Failed to get account at latest block"
        )

    override fun getAccountByBlockHeight(address: FlowAddress, height: Long): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowAccount>> =
        handleApiCall(
            apiCall = {
                api.getAccountAtBlockHeight(
                    Access.GetAccountAtBlockHeightRequest
                        .newBuilder()
                        .setAddress(address.byteStringValue)
                        .setBlockHeight(height)
                        .build()
                )
            },
            transform = { if (it.hasAccount()) FlowAccount.of(it.account) else throw IllegalStateException("Account not found") },
            errorMessage = "Failed to get account by block height"
        )

    override fun getAccountKeyAtLatestBlock(address: FlowAddress, keyIndex: Int): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowAccountKey>> =
        handleApiCall(
            apiCall = {
                api.getAccountKeyAtLatestBlock(
                    Access.GetAccountKeyAtLatestBlockRequest
                        .newBuilder()
                        .setAddress(address.byteStringValue)
                        .setIndex(keyIndex)
                        .build()
                )
            },
            transform = { FlowAccountKey.of(it.accountKey) },
            errorMessage = "Failed to get account key at latest block"
        )

    override fun getAccountKeyAtBlockHeight(address: FlowAddress, keyIndex: Int, height: Long): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowAccountKey>> =
        handleApiCall(
            apiCall = {
                api.getAccountKeyAtBlockHeight(
                    Access.GetAccountKeyAtBlockHeightRequest
                        .newBuilder()
                        .setAddress(address.byteStringValue)
                        .setIndex(keyIndex)
                        .setBlockHeight(height)
                        .build()
                )
            },
            transform = { FlowAccountKey.of(it.accountKey) },
            errorMessage = "Failed to get account key at block height"
        )

    override fun getAccountKeysAtLatestBlock(address: FlowAddress): CompletableFuture<FlowAccessApi.AccessApiCallResponse<List<FlowAccountKey>>> =
        handleApiCall(
            apiCall = {
                api.getAccountKeysAtLatestBlock(
                    Access.GetAccountKeysAtLatestBlockRequest
                        .newBuilder()
                        .setAddress(address.byteStringValue)
                        .build()
                )
            },
            transform = { it.accountKeysList.map { FlowAccountKey.of(it) } },
            errorMessage = "Failed to get account keys at latest block"
        )

    override fun getAccountKeysAtBlockHeight(address: FlowAddress, height: Long): CompletableFuture<FlowAccessApi.AccessApiCallResponse<List<FlowAccountKey>>> =
        handleApiCall(
            apiCall = {
                api.getAccountKeysAtBlockHeight(
                    Access.GetAccountKeysAtBlockHeightRequest
                        .newBuilder()
                        .setAddress(address.byteStringValue)
                        .setBlockHeight(height)
                        .build()
                )
            },
            transform = { it.accountKeysList.map { FlowAccountKey.of(it) } },
            errorMessage = "Failed to get account keys at block height"
        )

    override fun getLatestBlockHeader(sealed: Boolean): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowBlockHeader>> =
        handleApiCall(
            apiCall = {
                api.getLatestBlockHeader(
                    Access.GetLatestBlockHeaderRequest
                        .newBuilder()
                        .setIsSealed(sealed)
                        .build()
                )
            },
            transform = { FlowBlockHeader.of(it.block) },
            errorMessage = "Failed to get latest block header"
        )

    override fun getBlockHeaderById(id: FlowId): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowBlockHeader?>> =
        handleApiCall(
            apiCall = {
                api.getBlockHeaderByID(
                    Access.GetBlockHeaderByIDRequest
                        .newBuilder()
                        .setId(id.byteStringValue)
                        .build()
                )
            },
            transform = { if (it.hasBlock()) FlowBlockHeader.of(it.block) else null },
            errorMessage = "Failed to get block header by ID"
        )

    override fun getBlockHeaderByHeight(height: Long): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowBlockHeader?>> =
        handleApiCall(
            apiCall = {
                api.getBlockHeaderByHeight(
                    Access.GetBlockHeaderByHeightRequest
                        .newBuilder()
                        .setHeight(height)
                        .build()
                )
            },
            transform = { if (it.hasBlock()) FlowBlockHeader.of(it.block) else null },
            errorMessage = "Failed to get block header by height"
        )

    override fun getLatestBlock(sealed: Boolean, fullBlockResponse: Boolean): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowBlock>> =
        handleApiCall(
            apiCall = {
                api.getLatestBlock(
                    Access.GetLatestBlockRequest
                        .newBuilder()
                        .setIsSealed(sealed)
                        .setFullBlockResponse(fullBlockResponse)
                        .build()
                )
            },
            transform = { FlowBlock.of(it.block) },
            errorMessage = "Failed to get latest block"
        )

    override fun getAccountBalanceAtLatestBlock(address: FlowAddress): CompletableFuture<FlowAccessApi.AccessApiCallResponse<Long>> =
        handleApiCall(
            apiCall = {
                api.getAccountBalanceAtLatestBlock(
                    Access.GetAccountBalanceAtLatestBlockRequest
                        .newBuilder()
                        .setAddress(address.byteStringValue)
                        .build()
                )
            },
            transform = { it.balance },
            errorMessage = "Failed to get account balance at latest block"
        )

    override fun getAccountBalanceAtBlockHeight(address: FlowAddress, height: Long): CompletableFuture<FlowAccessApi.AccessApiCallResponse<Long>> =
        handleApiCall(
            apiCall = {
                api.getAccountBalanceAtBlockHeight(
                    Access.GetAccountBalanceAtBlockHeightRequest
                        .newBuilder()
                        .setAddress(address.byteStringValue)
                        .setBlockHeight(height)
                        .build()
                )
            },
            transform = { it.balance },
            errorMessage = "Failed to get account balance at block height"
        )

    override fun getBlockById(id: FlowId, fullBlockResponse: Boolean): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowBlock?>> =
        handleApiCall(
            apiCall = {
                api.getBlockByID(
                    Access.GetBlockByIDRequest
                        .newBuilder()
                        .setId(id.byteStringValue)
                        .setFullBlockResponse(fullBlockResponse)
                        .build()
                )
            },
            transform = { if (it.hasBlock()) FlowBlock.of(it.block) else null },
            errorMessage = "Failed to get block by ID"
        )

    override fun getBlockByHeight(height: Long, fullBlockResponse: Boolean): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowBlock?>> =
        handleApiCall(
            apiCall = {
                api.getBlockByHeight(
                    Access.GetBlockByHeightRequest
                        .newBuilder()
                        .setHeight(height)
                        .setFullBlockResponse(fullBlockResponse)
                        .build()
                )
            },
            transform = { if (it.hasBlock()) FlowBlock.of(it.block) else null },
            errorMessage = "Failed to get block by height"
        )

    override fun getCollectionById(id: FlowId): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowCollection?>> =
        handleApiCall(
            apiCall = {
                api.getCollectionByID(
                    Access.GetCollectionByIDRequest
                        .newBuilder()
                        .setId(id.byteStringValue)
                        .build()
                )
            },
            transform = { if (it.hasCollection()) FlowCollection.of(it.collection) else null },
            errorMessage = "Failed to get collection by ID"
        )

    override fun sendTransaction(transaction: FlowTransaction): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowId>> =
        handleApiCall(
            apiCall = {
                api.sendTransaction(
                    Access.SendTransactionRequest
                        .newBuilder()
                        .setTransaction(transaction.builder().build())
                        .build()
                )
            },
            transform = { FlowId.of(it.id.toByteArray()) },
            errorMessage = "Failed to send transaction"
        )

    override fun getTransactionById(id: FlowId): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowTransaction?>> =
        handleApiCall(
            apiCall = {
                api.getTransaction(
                    Access.GetTransactionRequest
                        .newBuilder()
                        .setId(id.byteStringValue)
                        .build()
                )
            },
            transform = { if (it.hasTransaction()) FlowTransaction.of(it.transaction) else null },
            errorMessage = "Failed to get transaction by ID"
        )

    override fun getTransactionResultById(id: FlowId): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowTransactionResult?>> =
        handleApiCall(
            apiCall = {
                api.getTransactionResult(
                    Access.GetTransactionRequest
                        .newBuilder()
                        .setId(id.byteStringValue)
                        .build()
                )
            },
            transform = { FlowTransactionResult.of(it) },
            errorMessage = "Failed to get transaction result by ID"
        )

    override fun getTransactionResultByIndex(blockId: FlowId, index: Int): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowTransactionResult>> =
        handleApiCall(
            apiCall = {
                api.getTransactionResultByIndex(
                    Access.GetTransactionByIndexRequest
                        .newBuilder()
                        .setBlockId(blockId.byteStringValue)
                        .setIndex(index)
                        .build()
                )
            },
            transform = { FlowTransactionResult.of(it) },
            errorMessage = "Failed to get transaction result by index"
        )

    private fun executeScript(
        apiCall: () -> ListenableFuture<Access.ExecuteScriptResponse>,
        errorMessage: String
    ): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowScriptResponse>> =
        handleApiCall(
            apiCall = apiCall,
            transform = { FlowScriptResponse(it.value.toByteArray()) },
            errorMessage = errorMessage
        )

    override fun executeScriptAtLatestBlock(
        script: FlowScript, arguments: Iterable<ByteString>
    ): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowScriptResponse>> =
        executeScript(
            apiCall = {
                api.executeScriptAtLatestBlock(
                    Access.ExecuteScriptAtLatestBlockRequest.newBuilder()
                        .setScript(script.byteStringValue)
                        .addAllArguments(arguments)
                        .build()
                )
            },
            errorMessage = "Failed to execute script at latest block"
        )

    override fun executeScriptAtBlockId(
        script: FlowScript, blockId: FlowId, arguments: Iterable<ByteString>
    ): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowScriptResponse>> =
        executeScript(
            apiCall = {
                api.executeScriptAtBlockID(
                    Access.ExecuteScriptAtBlockIDRequest.newBuilder()
                        .setBlockId(blockId.byteStringValue)
                        .setScript(script.byteStringValue)
                        .addAllArguments(arguments)
                        .build()
                )
            },
            errorMessage = "Failed to execute script at block ID"
        )

    override fun executeScriptAtBlockHeight(
        script: FlowScript, height: Long, arguments: Iterable<ByteString>
    ): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowScriptResponse>> =
        executeScript(
            apiCall = {
                api.executeScriptAtBlockHeight(
                    Access.ExecuteScriptAtBlockHeightRequest.newBuilder()
                        .setBlockHeight(height)
                        .setScript(script.byteStringValue)
                        .addAllArguments(arguments)
                        .build()
                )
            },
            errorMessage = "Failed to execute script at block height"
        )


     override fun getEventsForHeightRange(type: String, range: ClosedRange<Long>): CompletableFuture<FlowAccessApi.AccessApiCallResponse<List<FlowEventResult>>> =
        handleApiCall(
            apiCall = {
                api.getEventsForHeightRange(
                    Access.GetEventsForHeightRangeRequest
                        .newBuilder()
                        .setType(type)
                        .setStartHeight(range.start)
                        .setEndHeight(range.endInclusive)
                        .build()
                )
            },
            transform = { it.resultsList.map { FlowEventResult.of(it) } },
            errorMessage = "Failed to get events for height range"
        )

    override fun getEventsForBlockIds(type: String, ids: Set<FlowId>): CompletableFuture<FlowAccessApi.AccessApiCallResponse<List<FlowEventResult>>> =
        handleApiCall(
            apiCall = {
                api.getEventsForBlockIDs(
                    Access.GetEventsForBlockIDsRequest
                        .newBuilder()
                        .setType(type)
                        .addAllBlockIds(ids.map { it.byteStringValue })
                        .build()
                )
            },
            transform = { it.resultsList.map { FlowEventResult.of(it) } },
            errorMessage = "Failed to get events for block IDs"
        )

    override fun getNetworkParameters(): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowChainId>> =
        handleApiCall(
            apiCall = { api.getNetworkParameters(Access.GetNetworkParametersRequest.newBuilder().build()) },
            transform = { FlowChainId.of(it.chainId) },
            errorMessage = "Failed to get network parameters"
        )

    override fun getLatestProtocolStateSnapshot(): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowSnapshot>> =
        handleApiCall(
            apiCall = { api.getLatestProtocolStateSnapshot(Access.GetLatestProtocolStateSnapshotRequest.newBuilder().build()) },
            transform = { FlowSnapshot(it.serializedSnapshot.toByteArray()) },
            errorMessage = "Failed to get latest protocol state snapshot"
        )

    override fun getNodeVersionInfo(): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowNodeVersionInfo>> =
        handleApiCall(
            apiCall = { api.getNodeVersionInfo(Access.GetNodeVersionInfoRequest.newBuilder().build()) },
            transform = { response ->
                val compatibleRange = if (response.info.hasCompatibleRange()) {
                    FlowCompatibleRange(response.info.compatibleRange.startHeight, response.info.compatibleRange.endHeight)
                } else { null }
                FlowNodeVersionInfo(response.info.semver, response.info.commit, response.info.sporkId.toByteArray(), response.info.protocolVersion, response.info.sporkRootBlockHeight, response.info.nodeRootBlockHeight, compatibleRange)
            },
            errorMessage = "Failed to get node version info"
        )

    override fun getTransactionsByBlockId(id: FlowId): CompletableFuture<FlowAccessApi.AccessApiCallResponse<List<FlowTransaction>>> =
        handleApiCall(
            apiCall = {
                api.getTransactionsByBlockID(
                    Access.GetTransactionsByBlockIDRequest
                        .newBuilder()
                        .setBlockId(id.byteStringValue)
                        .build()
                )
            },
            transform = { it.transactionsList.map { FlowTransaction.of(it) } },
            errorMessage = "Failed to get transactions by block ID"
        )

    override fun getTransactionResultsByBlockId(id: FlowId): CompletableFuture<FlowAccessApi.AccessApiCallResponse<List<FlowTransactionResult>>> =
        handleApiCall(
            apiCall = {
                api.getTransactionResultsByBlockID(
                    Access.GetTransactionsByBlockIDRequest
                        .newBuilder()
                        .setBlockId(id.byteStringValue)
                        .build()
                )
            },
            transform = { it.transactionResultsList.map { FlowTransactionResult.of(it) } },
            errorMessage = "Failed to get transaction results by block ID"
        )

    override fun getExecutionResultByBlockId(id: FlowId): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowExecutionResult?>> =
        handleApiCall(
            apiCall = {
                api.getExecutionResultByID(
                    Access.GetExecutionResultByIDRequest
                        .newBuilder()
                        .setId(id.byteStringValue)
                        .build()
                )
            },
            transform = { if (it.hasExecutionResult()) FlowExecutionResult.of(it) else null },
            errorMessage = "Failed to get execution result by block ID"
        )
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
