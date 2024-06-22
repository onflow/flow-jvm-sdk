package org.onflow.flow.sdk.transaction

import org.onflow.flow.sdk.*
import org.onflow.flow.sdk.test.FlowEmulatorTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@FlowEmulatorTest
class TransactionIntegrationTest {
    @Test
    fun wut() {
        val account = when (val result = IntegrationTestUtils.newTestnetAccessApi().getAccountAtLatestBlock(FlowAddress("0x6bd3869f2631beb3"))) {
            is FlowAccessApi.FlowResult.Success -> result.data
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to get account: ${result.message}", result.throwable)
        }
        assertThat(account.keys).isNotEmpty()
    }

    @Test
    fun `Can connect to mainnet`() {
        val accessAPI = IntegrationTestUtils.newMainnetAccessApi()
        when (val pingResult = accessAPI.ping()) {
            is FlowAccessApi.FlowResult.Success -> Unit
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to ping: ${pingResult.message}", pingResult.throwable)
        }

        val address = FlowAddress("e467b9dd11fa00df")
        val account = when (val accountResult = accessAPI.getAccountAtLatestBlock(address)) {
            is FlowAccessApi.FlowResult.Success -> accountResult.data
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to get account: ${accountResult.message}", accountResult.throwable)
        }

        assertThat(account).isNotNull
        println(account)
        assertThat(account.keys).isNotEmpty
    }

    @Test
    fun `Can get network parameters`() {
        val accessAPI = IntegrationTestUtils.newMainnetAccessApi()
        val networkParams = when (val networkParamsResult = accessAPI.getNetworkParameters()) {
            is FlowAccessApi.FlowResult.Success -> networkParamsResult.data
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to get network parameters: ${networkParamsResult.message}", networkParamsResult.throwable)
        }

        assertThat(networkParams).isEqualTo(FlowChainId.MAINNET)
    }

    @Test
    fun `Can get latest protocol state snapshot`() {
        val accessAPI = IntegrationTestUtils.newMainnetAccessApi()
        val snapshot = when (val snapshotResult = accessAPI.getLatestProtocolStateSnapshot()) {
            is FlowAccessApi.FlowResult.Success -> snapshotResult.data
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to get latest protocol state snapshot: ${snapshotResult.message}", snapshotResult.throwable)
        }

        assertThat(snapshot).isNotNull
    }

    @Test
    fun `Can parse events`() {
        val accessApi = IntegrationTestUtils.newMainnetAccessApi()

        // https://flowscan.org/transaction/8c2e9d37a063240f236aa181e1454eb62991b42302534d4d6dd3839c2df0ef14
        val tx = when (val txResult = accessApi.getTransactionById(FlowId("8c2e9d37a063240f236aa181e1454eb62991b42302534d4d6dd3839c2df0ef14"))) {
            is FlowAccessApi.FlowResult.Success -> txResult.data
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to get transaction: ${txResult.message}", txResult.throwable)
        }

        assertThat(tx).isNotNull

        val results = when (val resultsResult = accessApi.getTransactionResultById(FlowId("8c2e9d37a063240f236aa181e1454eb62991b42302534d4d6dd3839c2df0ef14"))) {
            is FlowAccessApi.FlowResult.Success -> resultsResult.data
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to get transaction results: ${resultsResult.message}", resultsResult.throwable)
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
        val accessAPI = IntegrationTestUtils.newMainnetAccessApi()

        val latestBlock = when (val latestBlockResult = accessAPI.getLatestBlock(true)) {
            is FlowAccessApi.FlowResult.Success -> latestBlockResult.data
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to get latest block: ${latestBlockResult.message}", latestBlockResult.throwable)
        }

        assertThat(latestBlock).isNotNull
        val blockHeaderById = when (val blockHeaderByIdResult = accessAPI.getBlockHeaderById(latestBlock.id)) {
            is FlowAccessApi.FlowResult.Success -> blockHeaderByIdResult.data
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to get block header by ID: ${blockHeaderByIdResult.message}", blockHeaderByIdResult.throwable)
        }

        assertThat(blockHeaderById).isNotNull
    }

    @Test
    fun `Can get block header by height`() {
        val accessAPI = IntegrationTestUtils.newMainnetAccessApi()

        val latestBlock = when (val latestBlockResult = accessAPI.getLatestBlock(true)) {
            is FlowAccessApi.FlowResult.Success -> latestBlockResult.data
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to get latest block: ${latestBlockResult.message}", latestBlockResult.throwable)
        }

        assertThat(latestBlock).isNotNull
        val blockHeader = when (val blockHeaderResult = accessAPI.getBlockHeaderByHeight(latestBlock.height)) {
            is FlowAccessApi.FlowResult.Success -> blockHeaderResult.data
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to get block header by height: ${blockHeaderResult.message}", blockHeaderResult.throwable)
        }

        assertThat(blockHeader).isNotNull
        assertThat(blockHeader.height).isEqualTo(latestBlock.height)
    }

    @Test
    fun `Can get latest block`() {
        val accessAPI = IntegrationTestUtils.newMainnetAccessApi()

        val latestBlock = when (val latestBlockResult = accessAPI.getLatestBlock(true)) {
            is FlowAccessApi.FlowResult.Success -> latestBlockResult.data
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to get latest block: ${latestBlockResult.message}", latestBlockResult.throwable)
        }

        assertThat(latestBlock).isNotNull
    }

    @Test
    fun `Can get block by id`() {
        val accessAPI = IntegrationTestUtils.newMainnetAccessApi()

        val latestBlock = when (val latestBlockResult = accessAPI.getLatestBlock(true)) {
            is FlowAccessApi.FlowResult.Success -> latestBlockResult.data
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to get latest block: ${latestBlockResult.message}", latestBlockResult.throwable)
        }

        assertThat(latestBlock).isNotNull

        val blockById = when (val blockByIdResult = accessAPI.getBlockById(latestBlock.id)) {
            is FlowAccessApi.FlowResult.Success -> blockByIdResult.data
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to get block by ID: ${blockByIdResult.message}", blockByIdResult.throwable)
        }

        assertThat(blockById).isNotNull
        assertThat(blockById.id).isEqualTo(latestBlock.id)
    }

    @Test
    fun `Can get block by height`() {
        val accessAPI = IntegrationTestUtils.newMainnetAccessApi()

        val latestBlock = when (val latestBlockResult = accessAPI.getLatestBlock(true)) {
            is FlowAccessApi.FlowResult.Success -> latestBlockResult.data
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to get latest block: ${latestBlockResult.message}", latestBlockResult.throwable)
        }

        assertThat(latestBlock).isNotNull

        val blockByHeight = when (val blockByHeightResult = accessAPI.getBlockByHeight(latestBlock.height)) {
            is FlowAccessApi.FlowResult.Success -> blockByHeightResult.data
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to get block by height: ${blockByHeightResult.message}", blockByHeightResult.throwable)
        }

        assertThat(blockByHeight).isNotNull
        assertThat(blockByHeight.height).isEqualTo(latestBlock.height)
    }

    @Test
    fun `Can get account by address`() {
        val accessAPI = IntegrationTestUtils.newMainnetAccessApi()

        val address = FlowAddress("18eb4ee6b3c026d2")
        val account = when (val accountResult = accessAPI.getAccountByAddress(address)) {
            is FlowAccessApi.FlowResult.Success -> accountResult.data
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to get account by address: ${accountResult.message}", accountResult.throwable)
        }

        assertThat(account).isNotNull
        assertThat(account.address).isEqualTo(address)
    }

    @Test
    fun `Can get account by address at latest block`() {
        val accessAPI = IntegrationTestUtils.newMainnetAccessApi()

        val address = FlowAddress("18eb4ee6b3c026d2")
        val account = when (val accountResult = accessAPI.getAccountAtLatestBlock(address)) {
            is FlowAccessApi.FlowResult.Success -> accountResult.data
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to get account at latest block: ${accountResult.message}", accountResult.throwable)
        }

        assertThat(account).isNotNull
        assertThat(account.address).isEqualTo(address)
    }

    @Test
    fun `Can get account by block height`() {
        val accessAPI = IntegrationTestUtils.newMainnetAccessApi()

        val latestBlock = when (val latestBlockResult = accessAPI.getLatestBlock(true)) {
            is FlowAccessApi.FlowResult.Success -> latestBlockResult.data
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to get latest block: ${latestBlockResult.message}", latestBlockResult.throwable)
        }

        val blockHeader = when (val blockHeaderResult = accessAPI.getBlockHeaderByHeight(latestBlock.height)) {
            is FlowAccessApi.FlowResult.Success -> blockHeaderResult.data
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to get block header by height: ${blockHeaderResult.message}", blockHeaderResult.throwable)
        }

        val address = FlowAddress("18eb4ee6b3c026d2")
        val accountResult = blockHeader.let { accessAPI.getAccountByBlockHeight(address, it.height) }
        val account = when (accountResult) {
            is FlowAccessApi.FlowResult.Success -> accountResult.data
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to get account by block height: ${accountResult.message}", accountResult.throwable)
        }

        assertThat(account).isNotNull
        assertThat(account.address).isEqualTo(address)
    }
}
