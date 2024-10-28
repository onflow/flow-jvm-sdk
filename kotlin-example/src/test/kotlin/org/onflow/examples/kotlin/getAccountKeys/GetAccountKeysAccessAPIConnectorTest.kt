package org.onflow.examples.kotlin.getAccountKeys

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.onflow.flow.common.test.FlowEmulatorProjectTest
import org.onflow.flow.common.test.FlowServiceAccountCredentials
import org.onflow.flow.common.test.FlowTestClient
import org.onflow.flow.common.test.TestAccount
import org.onflow.flow.sdk.FlowAccessApi

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
internal class GetAccountKeysAccessAPIConnectorTest {
    @FlowServiceAccountCredentials
    lateinit var serviceAccount: TestAccount

    @FlowTestClient
    lateinit var accessAPI: FlowAccessApi

    private lateinit var keysAPIConnector: GetAccountKeysAccessAPIConnector

    @BeforeEach
    fun setup() {
        keysAPIConnector = GetAccountKeysAccessAPIConnector(accessAPI)
    }

    @Test
    fun `Can fetch account key at latest block`() {
        val address = serviceAccount.flowAddress
        val keyIndex = 0

        val accountKey = keysAPIConnector.getAccountKeyAtLatestBlock(address, keyIndex)

        Assertions.assertNotNull(accountKey, "Account key should not be null")
        Assertions.assertEquals(keyIndex, accountKey.sequenceNumber, "Account key index should match requested index")
    }

    @Test
    fun `Can fetch account key at a specific block height`() {
        val address = serviceAccount.flowAddress
        val keyIndex = 0
        when (val latestBlock = accessAPI.getLatestBlock(true)) {
            is FlowAccessApi.AccessApiCallResponse.Success -> {
                val accountKey = keysAPIConnector.getAccountKeyAtBlockHeight(address, keyIndex, latestBlock.data.height)

                Assertions.assertNotNull(accountKey, "Account key at specific block height should not be null")
                Assertions.assertEquals(keyIndex, accountKey.sequenceNumber, "Account key index at specific block height should match requested index")
            }
            is FlowAccessApi.AccessApiCallResponse.Error -> Assertions.fail("Failed to retrieve the latest block: ${latestBlock.message}")
        }
    }

    @Test
    fun `Can fetch all account keys at latest block`() {
        val address = serviceAccount.flowAddress
        val accountKeys = keysAPIConnector.getAccountKeysAtLatestBlock(address)

        Assertions.assertNotNull(accountKeys, "Account keys should not be null")
        Assertions.assertTrue(accountKeys.isNotEmpty(), "Account keys should not be empty")
    }

    @Test
    fun `Can fetch all account keys at a specific block height`() {
        val address = serviceAccount.flowAddress
        val latestBlock = accessAPI.getLatestBlock(true)

        when (latestBlock) {
            is FlowAccessApi.AccessApiCallResponse.Success -> {
                val accountKeys = keysAPIConnector.getAccountKeysAtBlockHeight(address, latestBlock.data.height)

                Assertions.assertNotNull(accountKeys, "Account keys at specific block height should not be null")
                Assertions.assertTrue(accountKeys.isNotEmpty(), "Account keys at specific block height should not be empty")
            }
            is FlowAccessApi.AccessApiCallResponse.Error -> Assertions.fail("Failed to retrieve the latest block: ${latestBlock.message}")
        }
    }

    @Test
    fun `Account keys at the latest block and specific block height should match`() {
        val address = serviceAccount.flowAddress

        // Fetch account keys at latest block
        val keysAtLatest = keysAPIConnector.getAccountKeysAtLatestBlock(address)

        // Fetch latest block height
        val latestBlock = accessAPI.getLatestBlock(true)
        when (latestBlock) {
            is FlowAccessApi.AccessApiCallResponse.Success -> {
                val blockHeight = latestBlock.data.height

                // Fetch account keys at the same block height
                val keysAtHeight = keysAPIConnector.getAccountKeysAtBlockHeight(address, blockHeight)

                Assertions.assertEquals(keysAtLatest.size, keysAtHeight.size, "Number of account keys should match at latest block and specific block height")

                keysAtLatest.forEachIndexed { index, key ->
                    Assertions.assertEquals(key, keysAtHeight[index], "Account key at index $index should match between latest block and specific block height")
                }
            }
            is FlowAccessApi.AccessApiCallResponse.Error -> Assertions.fail("Failed to retrieve the latest block: ${latestBlock.message}")
        }
    }
}
