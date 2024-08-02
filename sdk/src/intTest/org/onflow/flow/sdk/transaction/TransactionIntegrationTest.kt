package org.onflow.flow.sdk.transaction

import org.onflow.flow.sdk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.onflow.flow.common.test.*
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
            fail("Failed to ping mainnet: ${e.message}")
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
    fun `Can parse events`() {
        val txResult = createAndSubmitAccountCreationTransaction(
            accessAPI,
            serviceAccount,
            "cadence/transaction_creation/transaction_creation.cdc"
        )

        assertThat(txResult).isNotNull
        assertThat(txResult.status).isEqualTo(FlowTransactionStatus.SEALED)

        assertThat(txResult.events).isNotEmpty
        assertThat(txResult.events).hasSize(7)
        assertThat(txResult.events[0].event.id).contains("TokensWithdrawn")

        assertThat("from" in txResult.events[0].event).isTrue
        assertThat("amount" in txResult.events[0].event).isTrue

        assertThat(txResult.events[1].event.id).contains("TokensWithdrawn")

        assertThat("from" in txResult.events[1].event).isTrue
        assertThat("amount" in txResult.events[1].event).isTrue

        assertThat(txResult.events[2].event.id).contains("TokensDeposited")
        assertThat(txResult.events[3].event.id).contains("TokensDeposited")
        assertThat(txResult.events[4].event.id).contains("TokensDeposited")
        assertThat(txResult.events[5].event.id).contains("AccountCreated")
        assertThat(txResult.events[6].event.id).contains("AccountKeyAdded")
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
