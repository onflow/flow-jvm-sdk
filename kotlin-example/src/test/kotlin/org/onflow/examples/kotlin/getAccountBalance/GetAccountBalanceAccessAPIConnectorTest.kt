package org.onflow.examples.kotlin.getAccountBalance

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.onflow.flow.common.test.FlowEmulatorProjectTest
import org.onflow.flow.common.test.FlowServiceAccountCredentials
import org.onflow.flow.common.test.FlowTestClient
import org.onflow.flow.common.test.TestAccount
import org.onflow.flow.sdk.FlowAccessApi

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
internal class GetAccountBalanceAccessAPIConnectorTest {
    @FlowServiceAccountCredentials
    lateinit var serviceAccount: TestAccount

    @FlowTestClient
    lateinit var accessAPI: FlowAccessApi

    private lateinit var balanceAPIConnector: GetAccountBalanceAccessAPIConnector

    @BeforeEach
    fun setup() {
        balanceAPIConnector = GetAccountBalanceAccessAPIConnector(accessAPI)
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
        val latestBlock = accessAPI.getLatestBlock(true) // Fetch the latest sealed block

        when (latestBlock) {
            is FlowAccessApi.AccessApiCallResponse.Success -> {
                val balanceAtHeight = balanceAPIConnector.getBalanceAtBlockHeight(address, latestBlock.data.height)

                Assertions.assertNotNull(balanceAtHeight, "Balance at specific block height should not be null")
                Assertions.assertTrue(balanceAtHeight >= 0, "Balance at specific block height should be non-negative")
            }
            is FlowAccessApi.AccessApiCallResponse.Error -> Assertions.fail("Failed to retrieve the latest block: ${latestBlock.message}")
        }
    }

    @Test
    fun `Balances at the latest block and specific block height should match`() {
        val address = serviceAccount.flowAddress

        // Fetch balance at latest block
        val balanceAtLatest = balanceAPIConnector.getBalanceAtLatestBlock(address)

        // Fetch latest block height
        val latestBlock = accessAPI.getLatestBlock(true)
        when (latestBlock) {
            is FlowAccessApi.AccessApiCallResponse.Success -> {
                val blockHeight = latestBlock.data.height

                // Fetch balance at the same block height
                val balanceAtHeight = balanceAPIConnector.getBalanceAtBlockHeight(address, blockHeight)

                Assertions.assertEquals(balanceAtLatest, balanceAtHeight, "Balance at latest block and specific block height should match")
            }
            is FlowAccessApi.AccessApiCallResponse.Error -> Assertions.fail("Failed to retrieve the latest block: ${latestBlock.message}")
        }
    }
}
