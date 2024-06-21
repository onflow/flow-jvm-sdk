package org.onflow.flow.sdk

import com.google.protobuf.ByteString

interface FlowAccessApi {

    sealed class FlowResult<out T> {
        data class Success<out T>(val data: T) : FlowResult<T>()
        data class Error(val message: String, val throwable: Throwable? = null) : FlowResult<Nothing>()
    }

    fun ping()

    fun getLatestBlockHeader(sealed: Boolean = true): FlowResult<FlowBlockHeader>

    fun getBlockHeaderById(id: FlowId): FlowResult<FlowBlockHeader>

    fun getBlockHeaderByHeight(height: Long): FlowBlockHeader?

    fun getLatestBlock(sealed: Boolean = true): FlowBlock

    fun getBlockById(id: FlowId): FlowBlock?

    fun getTransactionsByBlockId(id: FlowId): List<FlowTransaction>?

    fun getTransactionResultsByBlockId(id: FlowId): List<FlowTransactionResult>?

    fun getExecutionResultByBlockId(id: FlowId): ExecutionResult?

    fun getBlockByHeight(height: Long): FlowBlock?

    fun getCollectionById(id: FlowId): FlowCollection?

    fun sendTransaction(transaction: FlowTransaction): FlowId

    fun getTransactionById(id: FlowId): FlowTransaction?

    fun getTransactionResultById(id: FlowId): FlowTransactionResult?

    @Deprecated(
        message = "Behaves identically to getAccountAtLatestBlock",
        replaceWith = ReplaceWith("getAccountAtLatestBlock")
    )
    fun getAccountByAddress(addresss: FlowAddress): FlowAccount?

    fun getAccountAtLatestBlock(addresss: FlowAddress): FlowAccount?

    fun getAccountByBlockHeight(addresss: FlowAddress, height: Long): FlowAccount?

    fun executeScriptAtLatestBlock(script: FlowScript, arguments: Iterable<ByteString> = emptyList()): FlowScriptResponse

    fun executeScriptAtBlockId(script: FlowScript, blockId: FlowId, arguments: Iterable<ByteString> = emptyList()): FlowScriptResponse

    fun executeScriptAtBlockHeight(script: FlowScript, height: Long, arguments: Iterable<ByteString> = emptyList()): FlowScriptResponse

    fun getEventsForHeightRange(type: String, range: ClosedRange<Long>): List<FlowEventResult>

    fun getEventsForBlockIds(type: String, ids: Set<FlowId>): List<FlowEventResult>

    fun getNetworkParameters(): FlowChainId

    fun getLatestProtocolStateSnapshot(): FlowSnapshot
}
