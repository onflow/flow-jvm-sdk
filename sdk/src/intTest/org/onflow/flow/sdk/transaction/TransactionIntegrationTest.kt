package org.onflow.flow.sdk.transaction

import org.onflow.flow.sdk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.onflow.flow.common.test.FlowEmulatorProjectTest
import org.onflow.flow.common.test.FlowServiceAccountCredentials
import org.onflow.flow.common.test.FlowTestClient
import org.onflow.flow.common.test.TestAccount
import org.onflow.flow.sdk.IntegrationTestUtils.createAndSubmitAccountCreationTransaction
import org.onflow.flow.sdk.IntegrationTestUtils.handleResult

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
class TransactionIntegrationTest {
    @FlowTestClient
    lateinit var accessAPI: FlowAccessApi

    @FlowServiceAccountCredentials
    lateinit var serviceAccount: TestAccount

    private fun <T> safelyHandle(action: () -> Result<T>, errorMessage: String): T =
        action().getOrElse { e ->
            fail("$errorMessage: ${e.message}")
        }

    private fun getLatestBlock(): FlowBlock =
        safelyHandle({ Result.success(handleResult(accessAPI.getLatestBlock(true), "Failed to get latest block")) }, "Failed to retrieve latest block")

    private fun getAccountAtLatestBlock(address: FlowAddress): FlowAccount =
        safelyHandle({ Result.success(handleResult(accessAPI.getAccountAtLatestBlock(address), "Failed to get account at latest block")) }, "Failed to retrieve account at latest block")

    private fun getAccountBalanceAtBlockHeight(address: FlowAddress, height: Long): Long =
        safelyHandle({ Result.success(handleResult(accessAPI.getAccountBalanceAtBlockHeight(address, height), "Failed to get account balance at block height")) }, "Failed to retrieve account balance at block height")

    private fun getBlockHeaderByHeight(height: Long): FlowBlockHeader =
        safelyHandle({ Result.success(handleResult(accessAPI.getBlockHeaderByHeight(height), "Failed to get block header by height")) }, "Failed to retrieve block header by height")

    private fun getAccountKeysAtLatestBlock(address: FlowAddress): List<FlowAccountKey> =
        safelyHandle({ Result.success(handleResult(accessAPI.getAccountKeysAtLatestBlock(address), "Failed to get account keys at latest block")) }, "Failed to retrieve account keys at latest block")

    private fun getAccountKeyAtBlockHeight(address: FlowAddress, keyIndex: Int, height: Long): FlowAccountKey =
        safelyHandle({ Result.success(handleResult(accessAPI.getAccountKeyAtBlockHeight(address, keyIndex, height), "Failed to get account key at block height")) }, "Failed to retrieve account key at block height")

    private fun getBlockHeaderById(blockId: FlowId): FlowBlockHeader =
        safelyHandle({ Result.success(handleResult(accessAPI.getBlockHeaderById(blockId), "Failed to get block header by ID")) }, "Failed to retrieve block header by ID")

    private fun getTransactionResultById(transactionId: FlowId): FlowTransactionResult =
        safelyHandle({ Result.success(handleResult(accessAPI.getTransactionResultById(transactionId), "Failed to get transaction result by ID")) }, "Failed to retrieve transaction result by ID")

    private fun getTransactionResultByIndex(blockId: FlowId, index: Int): FlowTransactionResult =
        safelyHandle({ Result.success(handleResult(accessAPI.getTransactionResultByIndex(blockId, index), "Failed to get transaction result by index")) }, "Failed to retrieve transaction result by index")


    @Test
    fun `Can connect to emulator and ping access API`() {
        safelyHandle({ handleResult(accessAPI.ping(), "Failed to ping") }, "Failed to ping emulator")

        val account = getAccountAtLatestBlock(serviceAccount.flowAddress)
        assertThat(account).isNotNull
        assertThat(account.keys).isNotEmpty()
    }

    @Test
    fun `Can get network parameters`() {
        val networkParams = safelyHandle(
            { Result.success(handleResult(accessAPI.getNetworkParameters(), "Failed to get network parameters")) },
            "Failed to retrieve network parameters"
        )

        assertThat(networkParams).isEqualTo(FlowChainId.EMULATOR)
    }

    @Test
    fun `Can get account key at latest block`() {
        val accountKey = safelyHandle(
            { Result.success(handleResult(accessAPI.getAccountKeyAtLatestBlock(serviceAccount.flowAddress, 0), "Failed to get account key at latest block")) },
            "Failed to retrieve account key at latest block"
        )

        assertThat(accountKey).isNotNull
        assertThat(accountKey!!.sequenceNumber).isEqualTo(0)
    }

    @Test
    fun `Can get account key at block height`() {
        val latestBlock = getLatestBlock()
        val accountKey = getAccountKeyAtBlockHeight(serviceAccount.flowAddress, 0, latestBlock.height)

        assertThat(accountKey).isNotNull
        assertThat(accountKey.sequenceNumber).isEqualTo(0)
    }

    @Test
    fun `Can get account keys at latest block`() {
        val accountKeys = getAccountKeysAtLatestBlock(serviceAccount.flowAddress)

        assertThat(accountKeys).isNotNull
        assertThat(accountKeys).isNotEmpty
    }

    @Test
    fun `Can get account keys at block height`() {
        val address = serviceAccount.flowAddress
        val latestBlock = getLatestBlock()

        val accountKeys = safelyHandle(
            { Result.success(handleResult(accessAPI.getAccountKeysAtBlockHeight(address, latestBlock.height), "Failed to get account keys at block height")) },
            "Failed to retrieve account keys at block height"
        )

        assertThat(accountKeys).isNotNull
        assertThat(accountKeys).isNotEmpty
    }

    @Test
    fun `Can get node version info`() {
        val nodeVersionInfo = safelyHandle(
            { Result.success(handleResult(accessAPI.getNodeVersionInfo(), "Failed to get node version info")) },
            "Failed to retrieve node version info"
        )

        assertThat(nodeVersionInfo).isNotNull
        assertThat(nodeVersionInfo.protocolVersion).isEqualTo(0)
        assertThat(nodeVersionInfo.sporkRootBlockHeight).isEqualTo(0)
        assertThat(nodeVersionInfo.nodeRootBlockHeight).isEqualTo(0)
        assertThat(nodeVersionInfo.compatibleRange).isEqualTo(null)
    }


    @Test
    fun `Can get transaction results`() {
        val txResult = createAndSubmitAccountCreationTransaction(
            accessAPI,
            serviceAccount,
            "cadence/transaction_creation/transaction_creation_simple_transaction.cdc"
        )
        assertThat(txResult).isNotNull
        assertThat(txResult.status).isEqualTo(FlowTransactionStatus.SEALED)

        val latestBlock = getLatestBlock()

        val txResultById = getTransactionResultById(txResult.transactionId)
        assertThat(txResultById).isNotNull
        assertThat(txResultById.status).isEqualTo(FlowTransactionStatus.SEALED)
        assertThat(txResultById.transactionId).isEqualTo(txResult.transactionId)

        val txResultByIndex = getTransactionResultByIndex(latestBlock.id, 0)
        assertThat(txResultByIndex).isNotNull
        assertThat(txResultByIndex.status).isEqualTo(FlowTransactionStatus.SEALED)
        assertThat(txResultByIndex.transactionId).isEqualTo(txResult.transactionId)
    }

    @Test
    fun `Can parse events`() {
        val txResult = createAndSubmitAccountCreationTransaction(
            accessAPI,
            serviceAccount,
            "cadence/transaction_creation/transaction_creation_simple_transaction.cdc"
        )
        assertThat(txResult).isNotNull
        assertThat(txResult.status).isEqualTo(FlowTransactionStatus.SEALED)

        assertThat(txResult.events).isNotEmpty
        assertThat(txResult.events).hasSize(1)
        assertThat(txResult.events[0].type).isEqualTo("flow.AccountKeyAdded")
        assertThat(txResult.events[0].event.id).contains("AccountKeyAdded")
    }

    @Test
    fun `Can get block header by id`() {
        val latestBlock = getLatestBlock()
        val blockHeader = getBlockHeaderById(latestBlock.id)

        assertThat(blockHeader).isNotNull
        assertThat(blockHeader.height).isEqualTo(latestBlock.height)
    }

    @Test
    fun `Can get block header by height`() {
        val latestBlock = getLatestBlock()
        val blockHeader = getBlockHeaderByHeight(latestBlock.height)

        assertThat(blockHeader).isNotNull
        assertThat(blockHeader.height).isEqualTo(latestBlock.height)
    }

    @Test
    fun `Can get account balance at latest block`() {
        val address = serviceAccount.flowAddress

        val balanceResponse = safelyHandle(
            { Result.success(handleResult(accessAPI.getAccountBalanceAtLatestBlock(address), "Failed to get account balance at latest block")) },
            "Failed to retrieve account balance at latest block"
        )

        val account = getAccountAtLatestBlock(address)
        val normalizedBalance = balanceResponse / 100_000_000L

        assertThat(normalizedBalance).isEqualTo(account.balance.toBigInteger().longValueExact())
    }

    @Test
    fun `Can get account balance at block height`() {
        val latestBlock = getLatestBlock()
        val balanceResponse = getAccountBalanceAtBlockHeight(serviceAccount.flowAddress, latestBlock.height)

        val account = safelyHandle(
            { Result.success(handleResult(accessAPI.getAccountByBlockHeight(serviceAccount.flowAddress, latestBlock.height), "Failed to get account by block height")) },
            "Failed to retrieve account by block height"
        )

        val normalizedBalance = balanceResponse / 100_000_000L
        assertThat(normalizedBalance).isEqualTo(account.balance.toBigInteger().longValueExact())
    }

    @Test
    fun `Can get latest block`() {
        val latestBlock = safelyHandle(
            { Result.success(handleResult(accessAPI.getLatestBlock(true), "Failed to get latest block")) },
            "Failed to retrieve latest block"
        )

        assertThat(latestBlock).isNotNull
    }

    @Test
    fun `Can get block by id`() {
        val latestBlock = getLatestBlock()

        val blockById = safelyHandle(
            { Result.success(handleResult(accessAPI.getBlockById(latestBlock.id), "Failed to get block by ID")) },
            "Failed to retrieve block by ID"
        )

        assertThat(blockById).isNotNull
        assertThat(blockById.id).isEqualTo(latestBlock.id)
    }

    @Test
    fun `Can get block by height`() {
        val latestBlock = getLatestBlock()
        val blockByHeight = safelyHandle(
            { Result.success(handleResult(accessAPI.getBlockByHeight(latestBlock.height), "Failed to get block by height")) },
            "Failed to retrieve block by height"
        )

        assertThat(blockByHeight).isNotNull
        assertThat(blockByHeight.height).isEqualTo(latestBlock.height)
    }

    @Test
    fun `Can get account by address`() {
        val address = serviceAccount.flowAddress
        val account = safelyHandle(
            { Result.success(handleResult(accessAPI.getAccountByAddress(address), "Failed to get account by address")) },
            "Failed to retrieve account by address"
        )

        assertThat(account).isNotNull
        assertThat(account.address).isEqualTo(address)
    }

    @Test
    fun `Can get account by address at latest block`() {
        val address = serviceAccount.flowAddress
        val account = safelyHandle(
            { Result.success(handleResult(accessAPI.getAccountAtLatestBlock(address), "Failed to get account at latest block")) },
            "Failed to retrieve account at latest block"
        )

        assertThat(account).isNotNull
        assertThat(account.address).isEqualTo(address)
    }

    @Test
    fun `Can get account by block height`() {
        val latestBlock = getLatestBlock()
        val account = safelyHandle(
            { Result.success(handleResult(accessAPI.getAccountByBlockHeight(serviceAccount.flowAddress, latestBlock.height), "Failed to get account by block height")) },
            "Failed to retrieve account by block height"
        )

        assertThat(account).isNotNull
        assertThat(account.address).isEqualTo(serviceAccount.flowAddress)
    }
}
