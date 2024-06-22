package org.onflow.flow.sdk.impl

import com.google.protobuf.ByteString
import org.onflow.flow.sdk.*
import io.grpc.ManagedChannel
import org.onflow.protobuf.access.Access
import org.onflow.protobuf.access.AccessAPIGrpc
import java.io.Closeable

class FlowAccessApiImpl(
    private val api: AccessAPIGrpc.AccessAPIBlockingStub
) : FlowAccessApi, Closeable {
    override fun close() {
        val chan = api.channel
        if (chan is ManagedChannel) {
            chan.shutdownNow()
        }
    }

    override fun ping(): FlowAccessApi.FlowResult<Unit> {
        return try {
            api.ping(
                Access.PingRequest.newBuilder()
                    .build()
            )
            FlowAccessApi.FlowResult.Success(Unit)
        } catch (e: Exception) {
            FlowAccessApi.FlowResult.Error("Failed to ping", e)
        }
    }

    override fun getLatestBlockHeader(sealed: Boolean): FlowAccessApi.FlowResult<FlowBlockHeader> {
        return try {
            val ret = api.getLatestBlockHeader(
                Access.GetLatestBlockHeaderRequest.newBuilder()
                    .setIsSealed(sealed)
                    .build()
            )
            FlowAccessApi.FlowResult.Success(FlowBlockHeader.of(ret.block))
        } catch (e: Exception) {
            FlowAccessApi.FlowResult.Error("Failed to get latest block header", e)
        }
    }

    override fun getBlockHeaderById(id: FlowId): FlowAccessApi.FlowResult<FlowBlockHeader> {
        return try {
            val ret = api.getBlockHeaderByID(
                Access.GetBlockHeaderByIDRequest.newBuilder()
                    .setId(id.byteStringValue)
                    .build()
            )
            if (ret.hasBlock()) {
                FlowAccessApi.FlowResult.Success(FlowBlockHeader.of(ret.block))
            } else {
                FlowAccessApi.FlowResult.Error("Block header not found")
            }
        } catch (e: Exception) {
            FlowAccessApi.FlowResult.Error("Failed to get block header by ID", e)
        }
    }

    override fun getBlockHeaderByHeight(height: Long): FlowAccessApi.FlowResult<FlowBlockHeader> {
        return try {
            val ret = api.getBlockHeaderByHeight(
                Access.GetBlockHeaderByHeightRequest.newBuilder()
                    .setHeight(height)
                    .build()
            )
            if (ret.hasBlock()) {
                FlowAccessApi.FlowResult.Success(FlowBlockHeader.of(ret.block))
            } else {
                FlowAccessApi.FlowResult.Error("Block header not found")
            }
        } catch (e: Exception) {
            FlowAccessApi.FlowResult.Error("Failed to get block header by height", e)
        }
    }

    override fun getLatestBlock(sealed: Boolean): FlowAccessApi.FlowResult<FlowBlock> {
        return try {
            val ret = api.getLatestBlock(
                Access.GetLatestBlockRequest.newBuilder()
                    .setIsSealed(sealed)
                    .build()
            )
            FlowAccessApi.FlowResult.Success(FlowBlock.of(ret.block))
        } catch (e: Exception) {
            FlowAccessApi.FlowResult.Error("Failed to get latest block", e)
        }
    }

    override fun getBlockById(id: FlowId): FlowAccessApi.FlowResult<FlowBlock> {
        return try {
            val ret = api.getBlockByID(
                Access.GetBlockByIDRequest.newBuilder()
                    .setId(id.byteStringValue)
                    .build()
            )
            if (ret.hasBlock()) {
                FlowAccessApi.FlowResult.Success(FlowBlock.of(ret.block))
            } else {
                FlowAccessApi.FlowResult.Error("Block not found")
            }
        } catch (e: Exception) {
            FlowAccessApi.FlowResult.Error("Failed to get block by ID", e)
        }
    }

    override fun getTransactionsByBlockId(id: FlowId): FlowAccessApi.FlowResult<List<FlowTransaction>> {
        return try {
            val ret = api.getTransactionsByBlockID(
                Access.GetTransactionsByBlockIDRequest.newBuilder()
                    .setBlockId(id.byteStringValue)
                    .build()
            )
            FlowAccessApi.FlowResult.Success(ret.transactionsList.map { FlowTransaction.of(it) })
        } catch (e: Exception) {
            FlowAccessApi.FlowResult.Error("Failed to get transactions by block ID", e)
        }
    }

    override fun getTransactionResultsByBlockId(id: FlowId): FlowAccessApi.FlowResult<List<FlowTransactionResult>> {
        return try {
            val ret = api.getTransactionResultsByBlockID(
                Access.GetTransactionsByBlockIDRequest.newBuilder()
                    .setBlockId(id.byteStringValue)
                    .build()
            )
            FlowAccessApi.FlowResult.Success(ret.transactionResultsList.map { FlowTransactionResult.of(it) })
        } catch (e: Exception) {
            FlowAccessApi.FlowResult.Error("Failed to get transaction results by block ID", e)
        }
    }

    override fun getExecutionResultByBlockId(id: FlowId): FlowAccessApi.FlowResult<ExecutionResult> {
        return try {
            val ret = api.getExecutionResultByID(
                Access.GetExecutionResultByIDRequest.newBuilder()
                    .setId(id.byteStringValue)
                    .build()
            )
            if (ret.hasExecutionResult()) {
                FlowAccessApi.FlowResult.Success(ExecutionResult.of(ret))
            } else {
                FlowAccessApi.FlowResult.Error("Execution result not found")
            }
        } catch (e: Exception) {
            FlowAccessApi.FlowResult.Error("Failed to get execution result by block ID", e)
        }
    }

    override fun getBlockByHeight(height: Long): FlowAccessApi.FlowResult<FlowBlock> {
        return try {
            val ret = api.getBlockByHeight(
                Access.GetBlockByHeightRequest.newBuilder()
                    .setHeight(height)
                    .build()
            )
            if (ret.hasBlock()) {
                FlowAccessApi.FlowResult.Success(FlowBlock.of(ret.block))
            } else {
                FlowAccessApi.FlowResult.Error("Block not found")
            }
        } catch (e: Exception) {
            FlowAccessApi.FlowResult.Error("Failed to get block by height", e)
        }
    }

    override fun getCollectionById(id: FlowId): FlowAccessApi.FlowResult<FlowCollection> {
        return try {
            val ret = api.getCollectionByID(
                Access.GetCollectionByIDRequest.newBuilder()
                    .setId(id.byteStringValue)
                    .build()
            )
            if (ret.hasCollection()) {
                FlowAccessApi.FlowResult.Success(FlowCollection.of(ret.collection))
            } else {
                FlowAccessApi.FlowResult.Error("Collection not found")
            }
        } catch (e: Exception) {
            FlowAccessApi.FlowResult.Error("Failed to get collection by ID", e)
        }
    }

    override fun sendTransaction(transaction: FlowTransaction): FlowAccessApi.FlowResult<FlowId> {
        return try {
            val ret = api.sendTransaction(
                Access.SendTransactionRequest.newBuilder()
                    .setTransaction(transaction.builder().build())
                    .build()
            )
            FlowAccessApi.FlowResult.Success(FlowId.of(ret.id.toByteArray()))
        } catch (e: Exception) {
            FlowAccessApi.FlowResult.Error("Failed to send transaction", e)
        }
    }

    override fun getTransactionById(id: FlowId): FlowAccessApi.FlowResult<FlowTransaction> {
        return try {
            val ret = api.getTransaction(
                Access.GetTransactionRequest.newBuilder()
                    .setId(id.byteStringValue)
                    .build()
            )
            if (ret.hasTransaction()) {
                FlowAccessApi.FlowResult.Success(FlowTransaction.of(ret.transaction))
            } else {
                FlowAccessApi.FlowResult.Error("Transaction not found")
            }
        } catch (e: Exception) {
            FlowAccessApi.FlowResult.Error("Failed to get transaction by ID", e)
        }
    }

    override fun getTransactionResultById(id: FlowId): FlowAccessApi.FlowResult<FlowTransactionResult> {
        return try {
            val ret = api.getTransactionResult(
                Access.GetTransactionRequest.newBuilder()
                    .setId(id.byteStringValue)
                    .build()
            )
            FlowAccessApi.FlowResult.Success(FlowTransactionResult.of(ret))
        } catch (e: Exception) {
            FlowAccessApi.FlowResult.Error("Failed to get transaction result by ID", e)
        }
    }

    @Deprecated("Behaves identically to getAccountAtLatestBlock", replaceWith = ReplaceWith("getAccountAtLatestBlock"))
    override fun getAccountByAddress(addresss: FlowAddress): FlowAccessApi.FlowResult<FlowAccount> {
        return try {
            val ret = api.getAccount(
                Access.GetAccountRequest.newBuilder()
                    .setAddress(addresss.byteStringValue)
                    .build()
            )
            if (ret.hasAccount()) {
                FlowAccessApi.FlowResult.Success(FlowAccount.of(ret.account))
            } else {
                FlowAccessApi.FlowResult.Error("Account not found")
            }
        } catch (e: Exception) {
            FlowAccessApi.FlowResult.Error("Failed to get account by address", e)
        }
    }

    override fun getAccountAtLatestBlock(addresss: FlowAddress): FlowAccessApi.FlowResult<FlowAccount> {
        return try {
            val ret = api.getAccountAtLatestBlock(
                Access.GetAccountAtLatestBlockRequest.newBuilder()
                    .setAddress(addresss.byteStringValue)
                    .build()
            )
            if (ret.hasAccount()) {
                FlowAccessApi.FlowResult.Success(FlowAccount.of(ret.account))
            } else {
                FlowAccessApi.FlowResult.Error("Account not found")
            }
        } catch (e: Exception) {
            FlowAccessApi.FlowResult.Error("Failed to get account at latest block", e)
        }
    }

    override fun getAccountByBlockHeight(addresss: FlowAddress, height: Long): FlowAccessApi.FlowResult<FlowAccount> {
        return try {
            val ret = api.getAccountAtBlockHeight(
                Access.GetAccountAtBlockHeightRequest.newBuilder()
                    .setAddress(addresss.byteStringValue)
                    .setBlockHeight(height)
                    .build()
            )
            if (ret.hasAccount()) {
                FlowAccessApi.FlowResult.Success(FlowAccount.of(ret.account))
            } else {
                FlowAccessApi.FlowResult.Error("Account not found")
            }
        } catch (e: Exception) {
            FlowAccessApi.FlowResult.Error("Failed to get account by block height", e)
        }
    }

    override fun executeScriptAtLatestBlock(script: FlowScript, arguments: Iterable<ByteString>): FlowAccessApi.FlowResult<FlowScriptResponse> {
        return try {
            val request = Access.ExecuteScriptAtLatestBlockRequest.newBuilder()
                .setScript(script.byteStringValue)
                .addAllArguments(arguments)
                .build()

            val ret = api.executeScriptAtLatestBlock(request)

            FlowAccessApi.FlowResult.Success(FlowScriptResponse(ret.value.toByteArray()))
        } catch (e: Exception) {
            println("Error executing script: ${e.message}")
            e.printStackTrace()
            FlowAccessApi.FlowResult.Error("Failed to execute script at latest block", e)
        }
    }

    override fun executeScriptAtBlockId(script: FlowScript, blockId: FlowId, arguments: Iterable<ByteString>): FlowAccessApi.FlowResult<FlowScriptResponse> {
        return try {
            val ret = api.executeScriptAtBlockID(
                Access.ExecuteScriptAtBlockIDRequest.newBuilder()
                    .setBlockId(blockId.byteStringValue)
                    .setScript(script.byteStringValue)
                    .addAllArguments(arguments)
                    .build()
            )
            FlowAccessApi.FlowResult.Success(FlowScriptResponse(ret.value.toByteArray()))
        } catch (e: Exception) {
            FlowAccessApi.FlowResult.Error("Failed to execute script at block ID", e)
        }
    }

    override fun executeScriptAtBlockHeight(script: FlowScript, height: Long, arguments: Iterable<ByteString>): FlowAccessApi.FlowResult<FlowScriptResponse> {
        return try {
            val ret = api.executeScriptAtBlockHeight(
                Access.ExecuteScriptAtBlockHeightRequest.newBuilder()
                    .setBlockHeight(height)
                    .setScript(script.byteStringValue)
                    .addAllArguments(arguments)
                    .build()
            )
            FlowAccessApi.FlowResult.Success(FlowScriptResponse(ret.value.toByteArray()))
        } catch (e: Exception) {
            FlowAccessApi.FlowResult.Error("Failed to execute script at block height", e)
        }
    }

    override fun getEventsForHeightRange(type: String, range: ClosedRange<Long>): FlowAccessApi.FlowResult<List<FlowEventResult>> {
        return try {
            val ret = api.getEventsForHeightRange(
                Access.GetEventsForHeightRangeRequest.newBuilder()
                    .setType(type)
                    .setStartHeight(range.start)
                    .setEndHeight(range.endInclusive)
                    .build()
            )
            FlowAccessApi.FlowResult.Success(ret.resultsList.map { FlowEventResult.of(it) })
        } catch (e: Exception) {
            FlowAccessApi.FlowResult.Error("Failed to get events for height range", e)
        }
    }

    override fun getEventsForBlockIds(type: String, ids: Set<FlowId>): FlowAccessApi.FlowResult<List<FlowEventResult>> {
        return try {
            val ret = api.getEventsForBlockIDs(
                Access.GetEventsForBlockIDsRequest.newBuilder()
                    .setType(type)
                    .addAllBlockIds(ids.map { it.byteStringValue })
                    .build()
            )
            FlowAccessApi.FlowResult.Success(ret.resultsList.map { FlowEventResult.of(it) })
        } catch (e: Exception) {
            FlowAccessApi.FlowResult.Error("Failed to get events for block IDs", e)
        }
    }

    override fun getNetworkParameters(): FlowAccessApi.FlowResult<FlowChainId> {
        return try {
            val ret = api.getNetworkParameters(
                Access.GetNetworkParametersRequest.newBuilder()
                    .build()
            )
            FlowAccessApi.FlowResult.Success(FlowChainId.of(ret.chainId))
        } catch (e: Exception) {
            FlowAccessApi.FlowResult.Error("Failed to get network parameters", e)
        }
    }

    override fun getLatestProtocolStateSnapshot(): FlowAccessApi.FlowResult<FlowSnapshot> {
        return try {
            val ret = api.getLatestProtocolStateSnapshot(
                Access.GetLatestProtocolStateSnapshotRequest.newBuilder()
                    .build()
            )
            FlowAccessApi.FlowResult.Success(FlowSnapshot(ret.serializedSnapshot.toByteArray()))
        } catch (e: Exception) {
            FlowAccessApi.FlowResult.Error("Failed to get latest protocol state snapshot", e)
        }
    }
}
