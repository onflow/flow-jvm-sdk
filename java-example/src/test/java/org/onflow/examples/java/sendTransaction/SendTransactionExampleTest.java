package org.onflow.examples.java.sendTransaction;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onflow.flow.common.test.*;
import org.onflow.flow.sdk.*;

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
class SendTransactionExampleTest {
    @FlowServiceAccountCredentials
    TestAccount serviceAccount;

    @FlowTestClient
    FlowAccessApi accessAPI;

    private SendTransactionExample transactionConnector;

    @BeforeEach
    void setup() {
        transactionConnector = new SendTransactionExample(serviceAccount.getPrivateKey(), accessAPI);
    }

    @Test
    void canSendSimpleTransaction() throws Exception {
        FlowTransactionResult txResult = transactionConnector.sendSimpleTransaction(serviceAccount.getFlowAddress(), "cadence/simple_transaction.cdc", 500);

        Assertions.assertNotNull(txResult);
        Assertions.assertSame(txResult.getStatus(), FlowTransactionStatus.SEALED, "Transaction should be sealed");
    }

    @Test
    void canSendComplexTransactionWithArguments() throws Exception {
        String greeting = "Hello Flow!";
        FlowTransactionResult txResult = transactionConnector.sendComplexTransactionWithArguments(serviceAccount.getFlowAddress(), "cadence/greeting_script.cdc", 500, greeting);

        Assertions.assertNotNull(txResult);
        Assertions.assertSame(txResult.getStatus(), FlowTransactionStatus.SEALED, "Transaction should be sealed");
    }
}