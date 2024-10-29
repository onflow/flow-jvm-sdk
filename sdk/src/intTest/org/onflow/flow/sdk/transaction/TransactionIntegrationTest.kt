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

    @Test
    fun `Can connect to emulator and ping access API`() {
        try {
            handleResult(accessAPI.ping(), "Failed to ping")
        } catch (e: Exception) {
            fail("Failed to ping emulator: ${e.message}")
        }

        val address = serviceAccount.flowAddress
        val account = try {
            handleResult(
                accessAPI.getAccountAtLatestBlock(address),
                "Failed to get account"
            )
        } catch (e: Exception) {
            fail("Failed to retrieve account: ${e.message}")
        }

        assertThat(account).isNotNull
        assertThat(account.keys).isNotEmpty()
    }

    @Test
    fun `Can get network parameters`() {
        val networkParams = try {
            handleResult(
                accessAPI.getNetworkParameters(),
                "Failed to get network parameters"
            )
        } catch (e: Exception) {
            fail("Failed to retrieve network parameters: ${e.message}")
        }

        assertThat(networkParams).isEqualTo(FlowChainId.EMULATOR)
    }

    @Test
    fun `Can get account key at latest block`() {
        val address = serviceAccount.flowAddress
        val keyIndex = 0

        val accountKey = try {
            handleResult(
                accessAPI.getAccountKeyAtLatestBlock(address, keyIndex),
                "Failed to get account key at latest block"
            )
        } catch (e: Exception) {
            fail("Failed to retrieve account key at latest block: ${e.message}")
        }

        assertThat(accountKey).isNotNull
        assertThat(accountKey.sequenceNumber).isEqualTo(keyIndex)
    }

    @Test
    fun `Can get account key at block height`() {
        val address = serviceAccount.flowAddress
        val keyIndex = 0

        val latestBlock = try {
            handleResult(
                accessAPI.getLatestBlock(true),
                "Failed to get latest block"
            )
        } catch (e: Exception) {
            fail("Failed to retrieve latest block: ${e.message}")
        }

        val accountKey = try {
            handleResult(
                accessAPI.getAccountKeyAtBlockHeight(address, keyIndex, latestBlock.height),
                "Failed to get account key at block height"
            )
        } catch (e: Exception) {
            fail("Failed to retrieve account key at block height: ${e.message}")
        }

        assertThat(accountKey).isNotNull
        assertThat(accountKey.sequenceNumber).isEqualTo(keyIndex)
    }

    @Test
    fun `Can get account keys at latest block`() {
        val address = serviceAccount.flowAddress

        val accountKeys = try {
            handleResult(
                accessAPI.getAccountKeysAtLatestBlock(address),
                "Failed to get account keys at latest block"
            )
        } catch (e: Exception) {
            fail("Failed to retrieve account keys at latest block: ${e.message}")
        }

        assertThat(accountKeys).isNotNull
        assertThat(accountKeys).isNotEmpty
    }

    @Test
    fun `Can get account keys at block height`() {
        val address = serviceAccount.flowAddress

        val latestBlock = try {
            handleResult(
                accessAPI.getLatestBlock(true),
                "Failed to get latest block"
            )
        } catch (e: Exception) {
            fail("Failed to retrieve latest block: ${e.message}")
        }

        val accountKeys = try {
            handleResult(
                accessAPI.getAccountKeysAtBlockHeight(address, latestBlock.height),
                "Failed to get account keys at block height"
            )
        } catch (e: Exception) {
            fail("Failed to retrieve account keys at block height: ${e.message}")
        }

        assertThat(accountKeys).isNotNull
        assertThat(accountKeys).isNotEmpty
    }

    @Test
    fun `Can get node version info`() {
        val nodeVersionInfo = try {
            handleResult(
                accessAPI.getNodeVersionInfo(),
                "Failed to get network parameters"
            )
        } catch (e: Exception) {
            fail("Failed to retrieve network parameters: ${e.message}")
        }

        assertThat(nodeVersionInfo).isNotNull()
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

        val latestBlock = try {
            handleResult(
                accessAPI.getLatestBlock(true),
                "Failed to get latest block"
            )
        } catch (e: Exception) {
            fail("Failed to retrieve latest block: ${e.message}")
        }

        val txResultById = try {
            handleResult(
                accessAPI.getTransactionResultById(txResult.transactionId),
                "Failed to get tx result by id"
            )
        } catch (e: Exception) {
            fail("Failed to retrieve tx result by id: ${e.message}")
        }

        assertThat(txResultById).isNotNull
        assertThat(txResultById.status).isEqualTo(FlowTransactionStatus.SEALED)
        assertThat(txResultById.transactionId).isEqualTo(txResult.transactionId)

        val txResultByIndex = try {
            handleResult(
                accessAPI.getTransactionResultByIndex(latestBlock.id, 0),
                "Failed to get tx result by index"
            )
        } catch (e: Exception) {
            fail("Failed to retrieve tx result by index: ${e.message}")
        }

        assertThat(txResultByIndex).isNotNull
        assertThat(txResultByIndex.status).isEqualTo(FlowTransactionStatus.SEALED)
        assertThat(txResultByIndex.transactionId).isEqualTo(txResultByIndex.transactionId)
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
        val latestBlock = try {
            handleResult(
                accessAPI.getLatestBlock(true),
                "Failed to get latest block"
            )
        } catch (e: Exception) {
            fail("Failed to retrieve latest block: ${e.message}")
        }

        assertThat(latestBlock).isNotNull

        val blockHeaderById = try {
            handleResult(
                accessAPI.getBlockHeaderById(latestBlock.id),
                "Failed to get block header by ID"
            )
        } catch (e: Exception) {
            fail("Failed to retrieve block header by ID: ${e.message}")
        }

        assertThat(blockHeaderById).isNotNull
    }

    @Test
    fun `Can get block header by height`() {
        val latestBlock = try {
            handleResult(
                accessAPI.getLatestBlock(true),
                "Failed to get latest block"
            )
        } catch (e: Exception) {
            fail("Failed to retrieve latest block: ${e.message}")
        }

        assertThat(latestBlock).isNotNull

        val blockHeader = try {
            handleResult(
                accessAPI.getBlockHeaderByHeight(latestBlock.height),
                "Failed to get block header by height"
            )
        } catch (e: Exception) {
            fail("Failed to retrieve block header by height: ${e.message}")
        }

        assertThat(blockHeader).isNotNull
        assertThat(blockHeader.height).isEqualTo(latestBlock.height)
    }

    @Test
    fun `Can get account balance at latest block`() {
        val address = serviceAccount.flowAddress

        val balanceResponse = try {
            handleResult(
                accessAPI.getAccountBalanceAtLatestBlock(address),
                "Failed to get account balance at latest block"
            )
        } catch (e: Exception) {
            fail("Failed to retrieve account balance at latest block: ${e.message}")
        }

        assertThat(balanceResponse).isNotNull

        val account = try {
            handleResult(
                accessAPI.getAccountAtLatestBlock(address),
                "Failed to get account at latest block"
            )
        } catch (e: Exception) {
            fail("Failed to retrieve account at latest block: ${e.message}")
        }

        val normalizedBalance = balanceResponse / 100_000_000L

        assertThat(normalizedBalance).isEqualTo(account.balance.toBigInteger().longValueExact())
    }

    @Test
    fun `Can get account balance at block height`() {
        val address = serviceAccount.flowAddress

        val latestBlock = try {
            handleResult(
                accessAPI.getLatestBlock(true),
                "Failed to get latest block"
            )
        } catch (e: Exception) {
            fail("Failed to retrieve latest block: ${e.message}")
        }

        val height = latestBlock.height

        val balanceResponse = try {
            handleResult(
                accessAPI.getAccountBalanceAtBlockHeight(address, height),
                "Failed to get account balance at block height"
            )
        } catch (e: Exception) {
            fail("Failed to retrieve account balance at block height: ${e.message}")
        }

        assertThat(balanceResponse).isNotNull

        val account = try {
            handleResult(
                accessAPI.getAccountByBlockHeight(address, height),
                "Failed to get account by block height"
            )
        } catch (e: Exception) {
            fail("Failed to retrieve account by block height: ${e.message}")
        }

        val normalizedBalance = balanceResponse / 100_000_000L

        assertThat(normalizedBalance).isEqualTo(account.balance.toBigInteger().longValueExact())
    }

    @Test
    fun `Can get latest block`() {
        val latestBlock = try {
            handleResult(
                accessAPI.getLatestBlock(true),
                "Failed to get latest block"
            )
        } catch (e: Exception) {
            fail("Failed to retrieve latest block: ${e.message}")
        }

        assertThat(latestBlock).isNotNull
    }

    @Test
    fun `Can get block by id`() {
        val latestBlock = try {
            handleResult(
                accessAPI.getLatestBlock(true),
                "Failed to get latest block"
            )
        } catch (e: Exception) {
            fail("Failed to retrieve latest block: ${e.message}")
        }

        assertThat(latestBlock).isNotNull

        val blockById = try {
            handleResult(
                accessAPI.getBlockById(latestBlock.id),
                "Failed to get block by ID"
            )
        } catch (e: Exception) {
            fail("Failed to retrieve block by ID: ${e.message}")
        }

        assertThat(blockById).isNotNull
        assertThat(blockById.id).isEqualTo(latestBlock.id)
    }

    @Test
    fun `Can get block by height`() {
        val latestBlock = try {
            handleResult(
                accessAPI.getLatestBlock(true),
                "Failed to get latest block"
            )
        } catch (e: Exception) {
            fail("Failed to retrieve latest block: ${e.message}")
        }

        assertThat(latestBlock).isNotNull

        val blockByHeight = try {
            handleResult(
                accessAPI.getBlockByHeight(latestBlock.height),
                "Failed to get block by height"
            )
        } catch (e: Exception) {
            fail("Failed to retrieve block by height: ${e.message}")
        }

        assertThat(blockByHeight).isNotNull
        assertThat(blockByHeight.height).isEqualTo(latestBlock.height)
    }

    @Test
    fun `Can get account by address`() {
        val address = serviceAccount.flowAddress
        val account = try {
            handleResult(
                accessAPI.getAccountByAddress(address),
                "Failed to get account by address"
            )
        } catch (e: Exception) {
            fail("Failed to retrieve account by address: ${e.message}")
        }

        assertThat(account).isNotNull
        assertThat(account.address).isEqualTo(address)
    }

    @Test
    fun `Can get account by address at latest block`() {
        val address = serviceAccount.flowAddress
        val account = try {
            handleResult(
                accessAPI.getAccountAtLatestBlock(address),
                "Failed to get account at latest block"
            )
        } catch (e: Exception) {
            fail("Failed to retrieve account at latest block: ${e.message}")
        }

        assertThat(account).isNotNull
        assertThat(account.address).isEqualTo(address)
    }

    @Test
    fun `Can get account by block height`() {
        val latestBlock = try {
            handleResult(
                accessAPI.getLatestBlock(true),
                "Failed to get latest block"
            )
        } catch (e: Exception) {
            fail("Failed to retrieve latest block: ${e.message}")
        }

        val blockHeader = try {
            handleResult(
                accessAPI.getBlockHeaderByHeight(latestBlock.height),
                "Failed to get block header by height"
            )
        } catch (e: Exception) {
            fail("Failed to retrieve block header by height: ${e.message}")
        }

        val address = serviceAccount.flowAddress
        val account = try {
            handleResult(
                accessAPI.getAccountByBlockHeight(address, blockHeader.height),
                "Failed to get account by block height"
            )
        } catch (e: Exception) {
            fail("Failed to retrieve account by block height: ${e.message}")
        }

        assertThat(account).isNotNull
        assertThat(account.address).isEqualTo(address)
    }
}
