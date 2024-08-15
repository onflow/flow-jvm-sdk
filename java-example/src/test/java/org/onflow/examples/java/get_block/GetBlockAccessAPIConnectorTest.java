package org.onflow.examples.java.get_block;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onflow.flow.common.test.FlowEmulatorProjectTest;
import org.onflow.flow.common.test.FlowTestClient;
import org.onflow.flow.sdk.FlowAccessApi;
import org.onflow.flow.sdk.FlowBlock;

import java.time.LocalDateTime;

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
public class GetBlockAccessAPIConnectorTest {

    @FlowTestClient
    private FlowAccessApi accessAPI;

    private GetBlockAccessAPIConnector blockAPIConnector;

    @BeforeEach
    public void setup() {
        blockAPIConnector = new GetBlockAccessAPIConnector(accessAPI);
    }

    @Test
    public void testCanFetchLatestSealedBlock() {
        FlowBlock block = blockAPIConnector.getLatestSealedBlock();

        Assertions.assertNotNull(block.getId(), "Block ID should not be null");
        Assertions.assertTrue(block.getHeight() >= 0, "Block height should be non-negative");
        Assertions.assertTrue(block.getTimestamp().isBefore(LocalDateTime.now()), "Block timestamp should be in the past");
    }

    @Test
    public void testCanFetchBlockByID() {
        FlowBlock latestBlock = blockAPIConnector.getLatestSealedBlock();
        FlowBlock blockByID = blockAPIConnector.getBlockByID(latestBlock.getId());

        Assertions.assertEquals(latestBlock.getId(), blockByID.getId(), "Block IDs should match");
        Assertions.assertEquals(latestBlock.getHeight(), blockByID.getHeight(), "Block heights should match");
        Assertions.assertEquals(latestBlock.getTimestamp(), blockByID.getTimestamp(), "Block timestamps should match");
        Assertions.assertEquals(latestBlock.getCollectionGuarantees().size(), blockByID.getCollectionGuarantees().size(), "Block should have the same number of collection guarantees");
    }

    @Test
    public void testCanFetchBlockByHeight() {
        FlowBlock latestBlock = blockAPIConnector.getLatestSealedBlock();
        FlowBlock blockByHeight = blockAPIConnector.getBlockByHeight(latestBlock.getHeight());

        Assertions.assertEquals(latestBlock.getId(), blockByHeight.getId(), "Block ID fetched by height should match the latest block ID");
        Assertions.assertEquals(latestBlock.getTimestamp(), blockByHeight.getTimestamp(), "Block timestamps should match for the latest block and the block fetched by height");
        Assertions.assertEquals(latestBlock.getCollectionGuarantees().size(), blockByHeight.getCollectionGuarantees().size(), "Block should have the same number of collection guarantees");
    }
}
