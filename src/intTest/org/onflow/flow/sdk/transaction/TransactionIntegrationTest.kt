package org.onflow.flow.sdk.transaction

import org.onflow.flow.sdk.*
import org.onflow.flow.sdk.test.FlowEmulatorTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@FlowEmulatorTest
class TransactionIntegrationTest {
    @Test
    fun wut() {
        val account = IntegrationTestUtils.newTestnetAccessApi().getAccountAtLatestBlock(FlowAddress("0x6bd3869f2631beb3"))
        account?.keys?.isEmpty()
    }

    @Test
    fun `Can connect to mainnet`() {
        val accessAPI = IntegrationTestUtils.newMainnetAccessApi()
        accessAPI.ping()

        val address = FlowAddress("e467b9dd11fa00df")
        val account = accessAPI.getAccountAtLatestBlock(address)
        assertThat(account).isNotNull
        println(account!!)
        assertThat(account.keys).isNotEmpty
    }

    @Test
    fun `Can get network parameters`() {
        val accessAPI = IntegrationTestUtils.newMainnetAccessApi()
        val networkParams = accessAPI.getNetworkParameters()

        assertThat(networkParams).isEqualTo(FlowChainId.MAINNET)
    }

    @Test
    fun `Can get latest protocol state snapshot`() {
        val accessAPI = IntegrationTestUtils.newMainnetAccessApi()
        val snapshot = accessAPI.getLatestProtocolStateSnapshot()

        assertThat(snapshot).isNotNull
    }

    @Test
    fun `Can parse events`() {
        val accessApi = IntegrationTestUtils.newMainnetAccessApi()

        // https://flowscan.org/transaction/8c2e9d37a063240f236aa181e1454eb62991b42302534d4d6dd3839c2df0ef14
        val tx = accessApi.getTransactionById(FlowId("8c2e9d37a063240f236aa181e1454eb62991b42302534d4d6dd3839c2df0ef14"))
        assertThat(tx).isNotNull

        val results = accessApi.getTransactionResultById(FlowId("8c2e9d37a063240f236aa181e1454eb62991b42302534d4d6dd3839c2df0ef14"))!!
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

        val latestBlock = accessAPI.getLatestBlock(true)
        assertThat(latestBlock).isNotNull
        val blockHeaderById = accessAPI.getBlockHeaderById(latestBlock.id)
        assertThat(blockHeaderById).isNotNull
    }

    @Test
    fun `Can get block header by height`() {
        val accessAPI = IntegrationTestUtils.newMainnetAccessApi()

        val latestBlock = accessAPI.getLatestBlock(true)
        assertThat(latestBlock).isNotNull
        val blockHeader = accessAPI.getBlockHeaderByHeight(latestBlock.height)
        assertThat(blockHeader).isNotNull
        assertThat(blockHeader?.height).isEqualTo(latestBlock.height)
    }

    @Test
    fun `Can get latest block`() {
        val accessAPI = IntegrationTestUtils.newMainnetAccessApi()

        val latestBlock = accessAPI.getLatestBlock(true)
        assertThat(latestBlock).isNotNull
    }

    @Test
    fun `Can get block by id`() {
        val accessAPI = IntegrationTestUtils.newMainnetAccessApi()

        val latestBlock = accessAPI.getLatestBlock(true)
        assertThat(latestBlock).isNotNull

        val blockById = accessAPI.getBlockById(latestBlock.id)
        assertThat(blockById).isNotNull
        assertThat(blockById?.id).isEqualTo(latestBlock.id)
    }

    @Test
    fun `Can get block by height`() {
        val accessAPI = IntegrationTestUtils.newMainnetAccessApi()

        val latestBlock = accessAPI.getLatestBlock(true)
        assertThat(latestBlock).isNotNull

        val blockByHeight = accessAPI.getBlockByHeight(latestBlock.height)
        assertThat(blockByHeight).isNotNull
        assertThat(blockByHeight?.height).isEqualTo(latestBlock.height)
    }

    @Test
    fun `Can get account by address`() {
        val accessAPI = IntegrationTestUtils.newMainnetAccessApi()

        val address = FlowAddress("18eb4ee6b3c026d2")
        val account = accessAPI.getAccountByAddress(address)!!
        assertThat(account).isNotNull
        assertThat(account.address).isEqualTo(address)
        assertThat(account).isEqualTo(account)
    }

    @Test
    fun `Can get account by address at latest block`() {
        val accessAPI = IntegrationTestUtils.newMainnetAccessApi()

        val address = FlowAddress("18eb4ee6b3c026d2")
        val account = accessAPI.getAccountAtLatestBlock(address)!!
        assertThat(account).isNotNull
        assertThat(account.address).isEqualTo(address)
        assertThat(account).isEqualTo(account)
    }

    @Test
    fun `Can get account by block height`() {
        val accessAPI = IntegrationTestUtils.newMainnetAccessApi()

        val latestBlock = accessAPI.getLatestBlock(true)
        val blockHeader = accessAPI.getBlockHeaderByHeight(latestBlock.height)

        val address = FlowAddress("18eb4ee6b3c026d2")
        val account = blockHeader?.let { accessAPI.getAccountByBlockHeight(address, it.height) }!!

        assertThat(account).isNotNull
        assertThat(account.address).isEqualTo(address)
        assertThat(account).isEqualTo(account)
    }
}
