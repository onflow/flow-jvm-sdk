package org.onflow.flow.sdk

import com.google.protobuf.ByteString
import java.util.concurrent.CompletableFuture

interface AsyncFlowAccessApi {
    fun ping(): CompletableFuture<Unit>

    fun getLatestBlockHeader(sealed: Boolean = true): CompletableFuture<FlowBlockHeader>

    fun getBlockHeaderById(id: FlowId): CompletableFuture<FlowBlockHeader?>

    fun getBlockHeaderByHeight(height: Long): CompletableFuture<FlowBlockHeader?>

    fun getLatestBlock(sealed: Boolean = true): CompletableFuture<FlowBlock>

    fun getBlockById(id: FlowId): CompletableFuture<FlowBlock?>

    fun getBlockByHeight(height: Long): CompletableFuture<FlowBlock?>

    fun getTransactionsByBlockId(id: FlowId): CompletableFuture<List<FlowTransaction>?>

    fun getTransactionResultsByBlockId(id: FlowId): CompletableFuture<List<FlowTransactionResult>?>

    fun getExecutionResultByBlockId(id: FlowId): CompletableFuture<ExecutionResult?>

    fun getCollectionById(id: FlowId): CompletableFuture<FlowCollection?>

    fun sendTransaction(transaction: FlowTransaction): CompletableFuture<FlowId>

    fun getTransactionById(id: FlowId): CompletableFuture<FlowTransaction?>

    fun getTransactionResultById(id: FlowId): CompletableFuture<FlowTransactionResult?>

    @Deprecated(
        message = "Behaves identically to getAccountAtLatestBlock",
        replaceWith = ReplaceWith("getAccountAtLatestBlock")
    )
    fun getAccountByAddress(addresss: FlowAddress): CompletableFuture<FlowAccount?>

    fun getAccountAtLatestBlock(addresss: FlowAddress): CompletableFuture<FlowAccount?>

    fun getAccountByBlockHeight(addresss: FlowAddress, height: Long): CompletableFuture<FlowAccount?>

    fun executeScriptAtLatestBlock(script: FlowScript, arguments: Iterable<ByteString> = emptyList()): CompletableFuture<FlowScriptResponse>

    fun executeScriptAtBlockId(script: FlowScript, blockId: FlowId, arguments: Iterable<ByteString> = emptyList()): CompletableFuture<FlowScriptResponse>

    fun executeScriptAtBlockHeight(script: FlowScript, height: Long, arguments: Iterable<ByteString> = emptyList()): CompletableFuture<FlowScriptResponse>

    fun getEventsForHeightRange(type: String, range: ClosedRange<Long>): CompletableFuture<List<FlowEventResult>>

    fun getEventsForBlockIds(type: String, ids: Set<FlowId>): CompletableFuture<List<FlowEventResult>>

    fun getNetworkParameters(): CompletableFuture<FlowChainId>

    fun getLatestProtocolStateSnapshot(): CompletableFuture<FlowSnapshot>
}
