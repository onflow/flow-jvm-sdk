package org.onflow.flow.sdk.transaction

import org.onflow.flow.sdk.*
import org.onflow.flow.common.test.FlowEmulatorTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.onflow.flow.sdk.IntegrationTestUtils.handleResult
import org.onflow.flow.sdk.IntegrationTestUtils.newMainnetAccessApi
import org.onflow.flow.sdk.IntegrationTestUtils.newTestnetAccessApi

@FlowEmulatorTest
class TransactionIntegrationTest {
    @Test
    fun wut() {
        val account = try {
            handleResult(
                newTestnetAccessApi().getAccountAtLatestBlock(FlowAddress("0x6bd3869f2631beb3")),
                "Failed to get account"
            )
        } catch (e: Exception) {
            fail("Failed to retrieve account: ${e.message}")
        }
        assertThat(account.keys).isNotEmpty()
    }

    @Test
    fun `Can connect to mainnet`() {
        val accessAPI = newMainnetAccessApi()
        try {
            handleResult(accessAPI.ping(), "Failed to ping")
        } catch (e: Exception) {
            fail("Failed to ping mainnet: ${e.message}")
        }

        val address = FlowAddress("e467b9dd11fa00df")
        val account = try {
            handleResult(
                accessAPI.getAccountAtLatestBlock(address),
                "Failed to get account"
            )
        } catch (e: Exception) {
            fail("Failed to retrieve account: ${e.message}")
        }

        assertThat(account).isNotNull
        println(account)
        assertThat(account.keys).isNotEmpty()
    }

    @Test
    fun `Can get network parameters`() {
        val accessAPI = newMainnetAccessApi()
        val networkParams = try {
            handleResult(
                accessAPI.getNetworkParameters(),
                "Failed to get network parameters"
            )
        } catch (e: Exception) {
            fail("Failed to retrieve network parameters: ${e.message}")
        }

        assertThat(networkParams).isEqualTo(FlowChainId.MAINNET)
    }

    @Test
    fun `Can get latest protocol state snapshot`() {
        val accessAPI = newMainnetAccessApi()
        val snapshot = try {
            handleResult(
                accessAPI.getLatestProtocolStateSnapshot(),
                "Failed to get latest protocol state snapshot"
            )
        } catch (e: Exception) {
            fail("Failed to retrieve latest protocol state snapshot: ${e.message}")
        }

        assertThat(snapshot).isNotNull
    }

    @Test
    fun `Can parse events`() {
        val accessApi = newMainnetAccessApi()

        // https://flowscan.org/transaction/8c2e9d37a063240f236aa181e1454eb62991b42302534d4d6dd3839c2df0ef14
        val tx = try {
            handleResult(
                accessApi.getTransactionById(FlowId("8c2e9d37a063240f236aa181e1454eb62991b42302534d4d6dd3839c2df0ef14")),
                "Failed to get transaction"
            )
        } catch (e: Exception) {
            fail("Failed to retrieve transaction: ${e.message}")
        }

        assertThat(tx).isNotNull

        val results = try {
            handleResult(
                accessApi.getTransactionResultById(FlowId("8c2e9d37a063240f236aa181e1454eb62991b42302534d4d6dd3839c2df0ef14")),
                "Failed to get transaction results"
            )
        } catch (e: Exception) {
            fail("Failed to retrieve transaction results: ${e.message}")
        }

        assertThat(results.events).hasSize(12)
        assertThat(results.events[0].event.id).isEqualTo("A.0b2a3299cc857e29.TopShot.Withdraw")
        assertThat(results.events[1].event.id).isEqualTo("A.0b2a3299cc857e29.TopShot.Deposit")
        assertThat(results.events[2].event.id).isEqualTo("A.ead892083b3e2c6c.DapperUtilityCoin.TokensWithdrawn")

        assertThat("from" in results.events[2].event).isTrue
        assertThat("amount" in results.events[2].event).isTrue

        assertThat(results.events[8].event.id).isEqualTo("A.b8ea91944fd51c43.OffersV2.OfferCompleted")
        assertThat("nftId" in results.events[8].event).isTrue
        assertThat("nftType" in results.events[8].event).isTrue
        assertThat("offerId" in results.events[8].event).isTrue
        assertThat("offerType" in results.events[8].event.value!!).isTrue
        assertThat("royalties" in results.events[8].event.value!!).isTrue
        assertThat("offerAddress" in results.events[8].event.value!!).isTrue
    }

    @Test
    fun `Can get block header by id`() {
        val accessAPI = newMainnetAccessApi()

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
        val accessAPI = newMainnetAccessApi()

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
        val accessAPI = newMainnetAccessApi()

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
        val accessAPI = newMainnetAccessApi()

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
        val accessAPI = newMainnetAccessApi()

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
        val accessAPI = newMainnetAccessApi()

        val address = FlowAddress("18eb4ee6b3c026d2")
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
        val accessAPI = newMainnetAccessApi()

        val address = FlowAddress("18eb4ee6b3c026d2")
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
        val accessAPI = newMainnetAccessApi()

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

        val address = FlowAddress("18eb4ee6b3c026d2")
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
