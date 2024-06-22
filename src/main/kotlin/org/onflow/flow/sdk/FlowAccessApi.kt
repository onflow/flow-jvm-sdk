package org.onflow.flow.sdk

import com.google.protobuf.ByteString

interface FlowAccessApi {
    sealed class FlowResult<out T> {
        data class Success<out T>(val data: T) : FlowResult<T>()
        data class Error(val message: String, val throwable: Throwable? = null) : FlowResult<Nothing>()
    }

    fun ping(): FlowResult<Unit>

    fun getLatestBlockHeader(sealed: Boolean = true): FlowResult<FlowBlockHeader>

    fun getBlockHeaderById(id: FlowId): FlowResult<FlowBlockHeader>

    fun getBlockHeaderByHeight(height: Long): FlowResult<FlowBlockHeader>

    fun getLatestBlock(sealed: Boolean = true): FlowResult<FlowBlock>

    fun getBlockById(id: FlowId): FlowResult<FlowBlock>

    fun getTransactionsByBlockId(id: FlowId): FlowResult<List<FlowTransaction>>

    fun getTransactionResultsByBlockId(id: FlowId): FlowResult<List<FlowTransactionResult>>

    fun getExecutionResultByBlockId(id: FlowId): FlowResult<ExecutionResult>

    fun getBlockByHeight(height: Long): FlowResult<FlowBlock>

    fun getCollectionById(id: FlowId): FlowResult<FlowCollection>

    fun sendTransaction(transaction: FlowTransaction): FlowResult<FlowId>

    fun getTransactionById(id: FlowId): FlowResult<FlowTransaction>

    fun getTransactionResultById(id: FlowId): FlowResult<FlowTransactionResult>

    @Deprecated(
        message = "Behaves identically to getAccountAtLatestBlock",
        replaceWith = ReplaceWith("getAccountAtLatestBlock")
    )
    fun getAccountByAddress(addresss: FlowAddress): FlowResult<FlowAccount>

    fun getAccountAtLatestBlock(addresss: FlowAddress): FlowResult<FlowAccount>

    fun getAccountByBlockHeight(addresss: FlowAddress, height: Long): FlowResult<FlowAccount>

    fun executeScriptAtLatestBlock(script: FlowScript, arguments: Iterable<ByteString> = emptyList()): FlowResult<FlowScriptResponse>

    fun executeScriptAtBlockId(script: FlowScript, blockId: FlowId, arguments: Iterable<ByteString> = emptyList()): FlowResult<FlowScriptResponse>

    fun executeScriptAtBlockHeight(script: FlowScript, height: Long, arguments: Iterable<ByteString> = emptyList()): FlowResult<FlowScriptResponse>

    fun getEventsForHeightRange(type: String, range: ClosedRange<Long>): FlowResult<List<FlowEventResult>>

    fun getEventsForBlockIds(type: String, ids: Set<FlowId>): FlowResult<List<FlowEventResult>>

    fun getNetworkParameters(): FlowResult<FlowChainId>

    fun getLatestProtocolStateSnapshot(): FlowResult<FlowSnapshot>
}
