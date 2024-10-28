package org.onflow.examples.java.getAccountKeys;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onflow.flow.common.test.FlowEmulatorProjectTest;
import org.onflow.flow.common.test.FlowServiceAccountCredentials;
import org.onflow.flow.common.test.FlowTestClient;
import org.onflow.flow.sdk.FlowAccessApi;
import org.onflow.flow.sdk.FlowAddress;
import org.onflow.flow.sdk.FlowAccountKey;
import org.onflow.flow.common.test.TestAccount;
import org.onflow.flow.sdk.FlowBlock;

import java.util.List;

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
public class GetAccountKeysAccessAPIConnectorTest {
    @FlowTestClient
    private FlowAccessApi accessAPI;

    @FlowServiceAccountCredentials
    private TestAccount serviceAccount;

    private GetAccountKeysAccessAPIConnector keysAPIConnector;

    @BeforeEach
    public void setup() {
        keysAPIConnector = new GetAccountKeysAccessAPIConnector(accessAPI);
    }

    @Test
    public void testCanFetchAccountKeyAtLatestBlock() {
        FlowAddress address = serviceAccount.getFlowAddress();
        int keyIndex = 0;

        FlowAccountKey accountKey = keysAPIConnector.getAccountKeyAtLatestBlock(address, keyIndex);

        Assertions.assertNotNull(accountKey, "Account key should not be null");
        Assertions.assertEquals(keyIndex, accountKey.getSequenceNumber(), "Account key index should match the requested index");
        Assertions.assertTrue(accountKey.getWeight() > 0, "Account key weight should be positive");
    }

    @Test
    public void testCanFetchAccountKeyAtSpecificBlockHeight() {
        FlowAddress address = serviceAccount.getFlowAddress();
        int keyIndex = 0;

        FlowAccessApi.AccessApiCallResponse<FlowBlock> latestBlockResponse = accessAPI.getLatestBlock(true, false);

        if (latestBlockResponse instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            FlowBlock latestBlock = ((FlowAccessApi.AccessApiCallResponse.Success<FlowBlock>) latestBlockResponse).getData();
            FlowAccountKey accountKey = keysAPIConnector.getAccountKeyAtBlockHeight(address, keyIndex, latestBlock.getHeight());

            Assertions.assertNotNull(accountKey, "Account key at specific block height should not be null");
            Assertions.assertEquals(keyIndex, accountKey.getSequenceNumber(), "Account key index at specific block height should match requested index");
            Assertions.assertTrue(accountKey.getWeight() > 0, "Account key weight should be positive");

        } else if (latestBlockResponse instanceof FlowAccessApi.AccessApiCallResponse.Error error) {
            throw new RuntimeException("Failed to retrieve the latest block: " + error.getMessage(), error.getThrowable());
        }
    }

    @Test
    public void testCanFetchAllAccountKeysAtLatestBlock() {
        FlowAddress address = serviceAccount.getFlowAddress();

        List<FlowAccountKey> accountKeys = keysAPIConnector.getAccountKeysAtLatestBlock(address);

        Assertions.assertNotNull(accountKeys, "Account keys list should not be null");
        Assertions.assertFalse(accountKeys.isEmpty(), "Account keys list should not be empty");
        accountKeys.forEach(key -> Assertions.assertTrue(key.getWeight() > 0, "Each account key weight should be positive"));
    }

    @Test
    public void testCanFetchAllAccountKeysAtSpecificBlockHeight() {
        FlowAddress address = serviceAccount.getFlowAddress();

        FlowAccessApi.AccessApiCallResponse<FlowBlock> latestBlockResponse = accessAPI.getLatestBlock(true, false);

        if (latestBlockResponse instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            FlowBlock latestBlock = ((FlowAccessApi.AccessApiCallResponse.Success<FlowBlock>) latestBlockResponse).getData();
            List<FlowAccountKey> accountKeys = keysAPIConnector.getAccountKeysAtBlockHeight(address, latestBlock.getHeight());

            Assertions.assertNotNull(accountKeys, "Account keys list at specific block height should not be null");
            Assertions.assertFalse(accountKeys.isEmpty(), "Account keys list at specific block height should not be empty");
            accountKeys.forEach(key -> Assertions.assertTrue(key.getWeight() > 0, "Each account key weight should be positive"));
        } else if (latestBlockResponse instanceof FlowAccessApi.AccessApiCallResponse.Error error) {
            throw new RuntimeException("Failed to retrieve the latest block: " + error.getMessage(), error.getThrowable());
        }
    }

    @Test
    public void testAccountKeysMatchAtLatestBlockAndSpecificBlockHeight() {
        FlowAddress address = serviceAccount.getFlowAddress();

        List<FlowAccountKey> keysAtLatestBlock = keysAPIConnector.getAccountKeysAtLatestBlock(address);

        FlowAccessApi.AccessApiCallResponse<FlowBlock> latestBlockResponse = accessAPI.getLatestBlock(true, false);

        if (latestBlockResponse instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            FlowBlock latestBlock = ((FlowAccessApi.AccessApiCallResponse.Success<FlowBlock>) latestBlockResponse).getData();
            List<FlowAccountKey> keysAtSpecificHeight = keysAPIConnector.getAccountKeysAtBlockHeight(address, latestBlock.getHeight());

            Assertions.assertEquals(keysAtLatestBlock.size(), keysAtSpecificHeight.size(), "Number of account keys should match at latest block and specific block height");

            for (int i = 0; i < keysAtLatestBlock.size(); i++) {
                Assertions.assertEquals(keysAtLatestBlock.get(i), keysAtSpecificHeight.get(i), "Account key at index " + i + " should match between latest block and specific block height");
            }
        } else if (latestBlockResponse instanceof FlowAccessApi.AccessApiCallResponse.Error error) {
            throw new RuntimeException("Failed to retrieve the latest block: " + error.getMessage(), error.getThrowable());
        }
    }
}
