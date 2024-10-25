package org.onflow.examples.java.getAccountBalance;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onflow.flow.common.test.FlowEmulatorProjectTest;
import org.onflow.flow.common.test.FlowServiceAccountCredentials;
import org.onflow.flow.common.test.FlowTestClient;
import org.onflow.flow.common.test.TestAccount;
import org.onflow.flow.sdk.FlowAccessApi;
import org.onflow.flow.sdk.FlowAddress;
import org.onflow.flow.sdk.FlowBlock;

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
public class GetAccountBalanceAccessAPIConnectorTest {

    @FlowTestClient
    private FlowAccessApi accessAPI;

    @FlowServiceAccountCredentials
    private TestAccount serviceAccount;

    private GetAccountBalanceAccessAPIConnector balanceAPIConnector;

    @BeforeEach
    public void setup() {
        balanceAPIConnector = new GetAccountBalanceAccessAPIConnector(accessAPI);
    }

    @Test
    public void testCanFetchBalanceAtLatestBlock() {
        FlowAddress address = serviceAccount.getFlowAddress();
        long balance = balanceAPIConnector.getBalanceAtLatestBlock(address);

        Assertions.assertTrue(balance >= 0, "Balance at the latest block should be non-negative");
    }

    @Test
    public void testCanFetchBalanceAtSpecificBlockHeight() {
        FlowAddress address = serviceAccount.getFlowAddress();

        FlowAccessApi.AccessApiCallResponse<FlowBlock> latestBlockResponse = accessAPI.getLatestBlock(true, false);

        if (latestBlockResponse instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            FlowBlock latestBlock = ((FlowAccessApi.AccessApiCallResponse.Success<FlowBlock>) latestBlockResponse).getData();
            long blockHeight = latestBlock.getHeight();
            long balanceAtHeight = balanceAPIConnector.getBalanceAtBlockHeight(address, blockHeight);

            Assertions.assertTrue(balanceAtHeight >= 0, "Balance at specific block height should be non-negative");
        } else {
            FlowAccessApi.AccessApiCallResponse.Error errorResponse = (FlowAccessApi.AccessApiCallResponse.Error) latestBlockResponse;
            Assertions.fail("Failed to fetch the latest block: " + errorResponse.getMessage());
        }
    }

    @Test
    public void testBalancesAtLatestBlockAndSpecificHeightShouldMatch() {
        FlowAddress address = serviceAccount.getFlowAddress();

        long balanceAtLatest = balanceAPIConnector.getBalanceAtLatestBlock(address);
        FlowAccessApi.AccessApiCallResponse<FlowBlock> latestBlockResponse = accessAPI.getLatestBlock(true, false);

        if (latestBlockResponse instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            FlowBlock latestBlock = ((FlowAccessApi.AccessApiCallResponse.Success<FlowBlock>) latestBlockResponse).getData();
            long blockHeight = latestBlock.getHeight();

            // Fetch balance at the same block height
            long balanceAtHeight = balanceAPIConnector.getBalanceAtBlockHeight(address, blockHeight);

            // Ensure balances match
            Assertions.assertEquals(balanceAtLatest, balanceAtHeight, "Balance at latest block and specific block height should match");
        } else {
            FlowAccessApi.AccessApiCallResponse.Error errorResponse = (FlowAccessApi.AccessApiCallResponse.Error) latestBlockResponse;
            Assertions.fail("Failed to fetch the latest block: " + errorResponse.getMessage());
        }
    }
}
