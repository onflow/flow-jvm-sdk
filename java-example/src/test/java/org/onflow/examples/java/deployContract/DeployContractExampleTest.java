package org.onflow.examples.java.deployContract;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onflow.examples.java.AccessAPIConnector;
import org.onflow.flow.common.test.*;
import org.onflow.flow.sdk.*;

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
public class DeployContractExampleTest {
    @FlowServiceAccountCredentials
    private TestAccount serviceAccount;
    @FlowTestClient
    private FlowAccessApi accessAPI;
    private AccessAPIConnector accessAPIConnector;
    private DeployContractExample deployContractExample;

    @BeforeEach
    public void setup() {
        deployContractExample = new DeployContractExample(serviceAccount.getPrivateKey(), accessAPI);
        accessAPIConnector = new AccessAPIConnector(serviceAccount.getPrivateKey(), accessAPI);
    }

    @Test
    public void canDeployContractToAccount() throws Exception {
        FlowAccount initialAccount = accessAPIConnector.getAccount(serviceAccount.getFlowAddress());
        Assertions.assertEquals(16, initialAccount.getContracts().size(), "The service account should initially have 16 contracts");

        FlowTransactionResult txResult = deployContractExample.deployContract(
                serviceAccount.getFlowAddress(),
                "GreatToken",
                "cadence/great_token.cdc",
                1000L
        );

        Assertions.assertNotNull(txResult, "Transaction result should not be null");
        Assertions.assertSame(txResult.getStatus(), FlowTransactionStatus.SEALED, "Transaction should be sealed");

        // Verify the contract was added to the account
        FlowAccount updatedAccount = accessAPIConnector.getAccount(serviceAccount.getFlowAddress());
        Assertions.assertTrue(updatedAccount.getContracts().containsKey("GreatToken"), "The account should now have the contract deployed");
        Assertions.assertEquals(17, updatedAccount.getContracts().size(), "The service account should now have 17 contracts");
    }
}
