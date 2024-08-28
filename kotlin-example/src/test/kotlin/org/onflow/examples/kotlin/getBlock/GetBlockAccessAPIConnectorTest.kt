package org.onflow.examples.kotlin.getBlock

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.onflow.flow.common.test.FlowEmulatorProjectTest
import org.onflow.flow.common.test.FlowServiceAccountCredentials
import org.onflow.flow.common.test.FlowTestClient
import org.onflow.flow.common.test.TestAccount
import org.onflow.flow.sdk.FlowAccessApi
import java.time.LocalDateTime

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
internal class GetBlockAccessAPIConnectorTest {
    @FlowServiceAccountCredentials
    lateinit var serviceAccount: TestAccount

    @FlowTestClient
    lateinit var accessAPI: FlowAccessApi

    private lateinit var blockAPIConnector: GetBlockAccessAPIConnector

    @BeforeEach
    fun setup() {
        blockAPIConnector = GetBlockAccessAPIConnector(accessAPI)
    }

    @Test
    fun `Can fetch the latest sealed block`() {
        val block = blockAPIConnector.getLatestSealedBlock()

        Assertions.assertNotNull(block.id, "Block ID should not be null")
        Assertions.assertTrue(block.height >= 0, "Block height should be non-negative")
        Assertions.assertTrue(block.timestamp.isBefore(LocalDateTime.now()), "Block timestamp should be in the past")
    }

    @Test
    fun `Can fetch a block by ID`() {
        val latestBlock = blockAPIConnector.getLatestSealedBlock()
        val blockByID = blockAPIConnector.getBlockByID(latestBlock.id)

        Assertions.assertEquals(latestBlock.id, blockByID.id, "Block IDs should match")
        Assertions.assertEquals(latestBlock.height, blockByID.height, "Block heights should match")
        Assertions.assertEquals(latestBlock.timestamp, blockByID.timestamp, "Block timestamps should match")
        Assertions.assertEquals(latestBlock.collectionGuarantees.size, blockByID.collectionGuarantees.size, "Block should have the same number of collection guarantees")
    }

    @Test
    fun `Can fetch a block by height`() {
        val latestBlock = blockAPIConnector.getLatestSealedBlock()
        val blockByHeight = blockAPIConnector.getBlockByHeight(latestBlock.height)

        Assertions.assertEquals(latestBlock.id, blockByHeight.id, "Block ID fetched by height should match the latest block ID")
        Assertions.assertEquals(latestBlock.timestamp, blockByHeight.timestamp, "Block timestamps should match for the latest block and the block fetched by height")
        Assertions.assertEquals(latestBlock.collectionGuarantees.size, blockByHeight.collectionGuarantees.size, "Block should have the same number of collection guarantees")
    }
}
