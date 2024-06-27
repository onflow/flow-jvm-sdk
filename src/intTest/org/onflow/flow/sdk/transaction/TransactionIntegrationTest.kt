package org.onflow.flow.sdk.transaction

import org.onflow.flow.sdk.*
import org.onflow.flow.sdk.test.FlowEmulatorTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@FlowEmulatorTest
class TransactionIntegrationTest {
    @Test
    fun wut() {
        val account = IntegrationTestUtils.handleResult(
            IntegrationTestUtils.newTestnetAccessApi().getAccountAtLatestBlock(FlowAddress("0x6bd3869f2631beb3")),
            "Failed to get account"
        )
        assertThat(account.keys).isNotEmpty()
    }

    @Test
    fun `Can connect to mainnet`() {
        val accessAPI = IntegrationTestUtils.newMainnetAccessApi()
        IntegrationTestUtils.handleResult(accessAPI.ping(), "Failed to ping")

        val address = FlowAddress("e467b9dd11fa00df")
        val account = IntegrationTestUtils.handleResult(
            accessAPI.getAccountAtLatestBlock(address),
            "Failed to get account"
        )

        assertThat(account).isNotNull
        println(account)
        assertThat(account.keys).isNotEmpty
    }

    @Test
    fun `Can get network parameters`() {
        val accessAPI = IntegrationTestUtils.newMainnetAccessApi()
        val networkParams = IntegrationTestUtils.handleResult(
            accessAPI.getNetworkParameters(),
            "Failed to get network parameters"
        )

        assertThat(networkParams).isEqualTo(FlowChainId.MAINNET)
    }

    @Test
    fun `Can get latest protocol state snapshot`() {
        val accessAPI = IntegrationTestUtils.newMainnetAccessApi()
        val snapshot = IntegrationTestUtils.handleResult(
            accessAPI.getLatestProtocolStateSnapshot(),
            "Failed to get latest protocol state snapshot"
        )

        assertThat(snapshot).isNotNull
    }

    @Test
    fun `Can parse events`() {
        val accessApi = IntegrationTestUtils.newMainnetAccessApi()

        // https://flowscan.org/transaction/8c2e9d37a063240f236aa181e1454eb62991b42302534d4d6dd3839c2df0ef14
        val tx = IntegrationTestUtils.handleResult(
            accessApi.getTransactionById(FlowId("8c2e9d37a063240f236aa181e1454eb62991b42302534d4d6dd3839c2df0ef14")),
            "Failed to get transaction"
        )

        assertThat(tx).isNotNull

        val results = IntegrationTestUtils.handleResult(
            accessApi.getTransactionResultById(FlowId("8c2e9d37a063240f236aa181e1454eb62991b42302534d4d6dd3839c2df0ef14")),
            "Failed to get transaction results"
        )

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
        val accessAPI = IntegrationTestUtils.newMainnetAccessApi()

        val latestBlock = IntegrationTestUtils.handleResult(
            accessAPI.getLatestBlock(true),
            "Failed to get latest block"
        )

        assertThat(latestBlock).isNotNull

        val blockHeaderById = IntegrationTestUtils.handleResult(
            accessAPI.getBlockHeaderById(latestBlock.id),
            "Failed to get block header by ID"
        )

        assertThat(blockHeaderById).isNotNull
    }

    @Test
    fun `Can get block header by height`() {
        val accessAPI = IntegrationTestUtils.newMainnetAccessApi()

        val latestBlock = IntegrationTestUtils.handleResult(
            accessAPI.getLatestBlock(true),
            "Failed to get latest block"
        )

        assertThat(latestBlock).isNotNull

        val blockHeader = IntegrationTestUtils.handleResult(
            accessAPI.getBlockHeaderByHeight(latestBlock.height),
            "Failed to get block header by height"
        )

        assertThat(blockHeader).isNotNull
        assertThat(blockHeader.height).isEqualTo(latestBlock.height)
    }

    @Test
    fun `Can get latest block`() {
        val accessAPI = IntegrationTestUtils.newMainnetAccessApi()

        val latestBlock = IntegrationTestUtils.handleResult(
            accessAPI.getLatestBlock(true),
            "Failed to get latest block"
        )

        assertThat(latestBlock).isNotNull
    }

    @Test
    fun `Can get block by id`() {
        val accessAPI = IntegrationTestUtils.newMainnetAccessApi()

        val latestBlock = IntegrationTestUtils.handleResult(
            accessAPI.getLatestBlock(true),
            "Failed to get latest block"
        )

        assertThat(latestBlock).isNotNull

        val blockById = IntegrationTestUtils.handleResult(
            accessAPI.getBlockById(latestBlock.id),
            "Failed to get block by ID"
        )

        assertThat(blockById).isNotNull
        assertThat(blockById.id).isEqualTo(latestBlock.id)
    }

    @Test
    fun `Can get block by height`() {
        val accessAPI = IntegrationTestUtils.newMainnetAccessApi()

        val latestBlock = IntegrationTestUtils.handleResult(
            accessAPI.getLatestBlock(true),
            "Failed to get latest block"
        )

        assertThat(latestBlock).isNotNull

        val blockByHeight = IntegrationTestUtils.handleResult(
            accessAPI.getBlockByHeight(latestBlock.height),
            "Failed to get block by height"
        )

        assertThat(blockByHeight).isNotNull
        assertThat(blockByHeight.height).isEqualTo(latestBlock.height)
    }

    @Test
    fun `Can get account by address`() {
        val accessAPI = IntegrationTestUtils.newMainnetAccessApi()

        val address = FlowAddress("18eb4ee6b3c026d2")
        val account = IntegrationTestUtils.handleResult(
            accessAPI.getAccountByAddress(address),
            "Failed to get account by address"
        )

        assertThat(account).isNotNull
        assertThat(account.address).isEqualTo(address)
    }

    @Test
    fun `Can get account by address at latest block`() {
        val accessAPI = IntegrationTestUtils.newMainnetAccessApi()

        val address = FlowAddress("18eb4ee6b3c026d2")
        val account = IntegrationTestUtils.handleResult(
            accessAPI.getAccountAtLatestBlock(address),
            "Failed to get account at latest block"
        )

        assertThat(account).isNotNull
        assertThat(account.address).isEqualTo(address)
    }

    @Test
    fun `Can get account by block height`() {
        val accessAPI = IntegrationTestUtils.newMainnetAccessApi()

        val latestBlock = IntegrationTestUtils.handleResult(
            accessAPI.getLatestBlock(true),
            "Failed to get latest block"
        )

        val blockHeader = IntegrationTestUtils.handleResult(
            accessAPI.getBlockHeaderByHeight(latestBlock.height),
            "Failed to get block header by height"
        )

        val address = FlowAddress("18eb4ee6b3c026d2")
        val accountResult = accessAPI.getAccountByBlockHeight(address, blockHeader.height)
        val account = IntegrationTestUtils.handleResult(
            accountResult,
            "Failed to get account by block height"
        )

        assertThat(account).isNotNull
        assertThat(account.address).isEqualTo(address)
    }
}
