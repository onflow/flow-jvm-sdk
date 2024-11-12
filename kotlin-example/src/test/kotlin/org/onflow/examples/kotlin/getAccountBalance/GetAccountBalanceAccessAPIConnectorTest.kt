package org.onflow.examples.kotlin.getAccountBalance

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.onflow.examples.kotlin.getTransaction.GetTransactionAccessAPIConnectorTest
import org.onflow.flow.common.test.FlowEmulatorProjectTest
import org.onflow.flow.common.test.FlowServiceAccountCredentials
import org.onflow.flow.common.test.FlowTestClient
import org.onflow.flow.common.test.TestAccount
import org.onflow.flow.sdk.FlowAccessApi
import org.onflow.flow.sdk.FlowBlock

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
internal class GetAccountBalanceAccessAPIConnectorTest {
    @FlowServiceAccountCredentials
    lateinit var serviceAccount: TestAccount

    @FlowTestClient
    lateinit var accessAPI: FlowAccessApi

    private lateinit var balanceAPIConnector: GetAccountBalanceAccessAPIConnector
    private lateinit var latestBlock: FlowBlock

    @BeforeEach
    fun setup() {
        balanceAPIConnector = GetAccountBalanceAccessAPIConnector(accessAPI)
        latestBlock = GetTransactionAccessAPIConnectorTest.fetchLatestBlockWithRetries(accessAPI)
    }

    @Test
    fun `Can fetch account balance at the latest block`() {
        val address = serviceAccount.flowAddress
        val balance = balanceAPIConnector.getBalanceAtLatestBlock(address)

        Assertions.assertNotNull(balance, "Balance should not be null")
        Assertions.assertTrue(balance >= 0, "Balance should be non-negative")
    }

    @Test
    fun `Can fetch account balance at a specific block height`() {
        val address = serviceAccount.flowAddress
        val balanceAtHeight = balanceAPIConnector.getBalanceAtBlockHeight(address, latestBlock.height)

        Assertions.assertNotNull(balanceAtHeight, "Balance at specific block height should not be null")
        Assertions.assertTrue(balanceAtHeight >= 0, "Balance at specific block height should be non-negative")
    }

    @Test
    fun `Balances at the latest block and specific block height should match`() {
        val address = serviceAccount.flowAddress

        // Fetch balance at latest block
        val balanceAtLatest = balanceAPIConnector.getBalanceAtLatestBlock(address)

        val blockHeight = latestBlock.height

        // Fetch balance at the same block height
        val balanceAtHeight = balanceAPIConnector.getBalanceAtBlockHeight(address, blockHeight)

        Assertions.assertEquals(balanceAtLatest, balanceAtHeight, "Balance at latest block and specific block height should match")
    }
}
