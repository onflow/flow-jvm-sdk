package org.onflow.examples.java.addKey;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onflow.examples.java.AccessAPIConnector;
import org.onflow.flow.common.test.FlowEmulatorProjectTest;
import org.onflow.flow.common.test.FlowTestAccount;
import org.onflow.flow.common.test.FlowTestClient;
import org.onflow.flow.common.test.TestAccount;
import org.onflow.flow.sdk.*;

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
class AddAccountKeyExampleTest {
    @FlowTestAccount
    TestAccount testAccount;

    @FlowTestClient
    FlowAccessApi accessAPI;

    private AccessAPIConnector accessAPIConnector;
    private AddAccountKeyExample connector;

    @BeforeEach
    void setup() {
        connector = new AddAccountKeyExample(testAccount.getPrivateKey(), accessAPI);
        accessAPIConnector = new AccessAPIConnector(testAccount.getPrivateKey(), accessAPI);
    }

    @Test
    void canAddKeyToAccount() {
        FlowAccount initialKeys = accessAPIConnector.getAccount(testAccount.getFlowAddress());
        Assertions.assertEquals(2, initialKeys.getKeys().size(), "The account should initially have 2 keys");

        FlowTransactionResult txResult = null;
        try {
            txResult = connector.addKeyToAccount(testAccount.getFlowAddress(), "cadence/add_key.cdc", 500L);
        } catch (Exception e) {
            Assertions.fail("Transaction failed with exception: " + e.getMessage());
        }

        Assertions.assertNotNull(txResult, "Transaction result should not be null");
        Assertions.assertSame(txResult.getStatus(), FlowTransactionStatus.SEALED, "Transaction should be sealed");

        FlowAccount updatedAccount = accessAPIConnector.getAccount(testAccount.getFlowAddress());
        Assertions.assertEquals(3, updatedAccount.getKeys().size(), "The account should now have 3 keys");
    }
}
