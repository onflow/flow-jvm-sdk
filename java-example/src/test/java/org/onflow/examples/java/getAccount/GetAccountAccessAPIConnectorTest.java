package org.onflow.examples.java.getAccount;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onflow.flow.common.test.FlowEmulatorProjectTest;
import org.onflow.flow.common.test.FlowServiceAccountCredentials;
import org.onflow.flow.common.test.FlowTestClient;
import org.onflow.flow.common.test.TestAccount;
import org.onflow.flow.sdk.FlowAccessApi;
import org.onflow.flow.sdk.FlowAccount;
import org.onflow.flow.sdk.FlowAddress;

import java.math.BigDecimal;

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
public class GetAccountAccessAPIConnectorTest {
    @FlowServiceAccountCredentials
    private TestAccount testAccount;
    @FlowTestClient
    private FlowAccessApi accessAPI;
    private GetAccountAccessAPIConnector accountConnector;

    @BeforeEach
    public void setup() {
        accountConnector = new GetAccountAccessAPIConnector(accessAPI);
    }

    @Test
    public void testCanFetchAccountFromLatestBlock() {
        FlowAddress address = testAccount.getFlowAddress();
        FlowAccount account = accountConnector.getAccountAtLatestBlock(address);

        Assertions.assertNotNull(account, "Account should not be null");
        Assertions.assertEquals(address.getBase16Value(), account.getAddress().getBase16Value(), "Address should match");
        Assertions.assertTrue(account.getBalance().compareTo(BigDecimal.ZERO) >= 0, "Account balance should be non-negative");
    }

    @Test
    public void testCanFetchAccountFromBlockByHeight0() {
        FlowAddress address = testAccount.getFlowAddress();
        FlowAccount account = accountConnector.getAccountAtBlockHeight(address, 0);

        Assertions.assertNotNull(account, "Account should not be null");
        Assertions.assertEquals(address.getBase16Value(), account.getAddress().getBase16Value(), "Address should match");
        Assertions.assertTrue(account.getBalance().compareTo(BigDecimal.ZERO) >= 0, "Account balance should be non-negative");
    }

    @Test
    public void testCanFetchAccountBalance() {
        FlowAddress address = testAccount.getFlowAddress();
        BigDecimal balance = accountConnector.getAccountBalance(address);

        Assertions.assertNotNull(balance, "Balance should not be null");
        Assertions.assertTrue(balance.compareTo(BigDecimal.ZERO) >= 0, "Balance should be non-negative");
    }
}
