package org.onflow.examples.java.signTransaction;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onflow.flow.common.test.*;
import org.onflow.flow.sdk.*;

import java.util.List;

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
public class SignTransactionExampleTest {
    @FlowServiceAccountCredentials
    private TestAccount serviceAccount;

    @FlowTestAccount
    private TestAccount testAccount;

    @FlowTestAccount
    private TestAccount testAccount2;

    @FlowTestClient
    private FlowAccessApi accessAPI;

    private SignTransactionExample transactionConnector;

    @BeforeEach
    public void setup() {
        transactionConnector = new SignTransactionExample(serviceAccount.getPrivateKey(), accessAPI);
    }

    @Test
    public void canSignSinglePartySingleSigTransaction() throws Exception {
        FlowTransactionResult txResult = transactionConnector.singlePartySingleSignature(serviceAccount.getFlowAddress(), "cadence/simple_transaction.cdc", 500L);

        Assertions.assertNotNull(txResult);
        Assertions.assertSame(txResult.getStatus(), FlowTransactionStatus.SEALED, "Transaction should be sealed");
    }

    @Test
    public void canSignSinglePartyMultiSigTransaction() throws Exception {
        transactionConnector = new SignTransactionExample(testAccount.getPrivateKey(), accessAPI);
        FlowTransactionResult txResult = transactionConnector.singlePartyMultiSignature(testAccount.getFlowAddress(), "cadence/simple_transaction.cdc", 500L);

        Assertions.assertNotNull(txResult);
        Assertions.assertSame(txResult.getStatus(), FlowTransactionStatus.SEALED, "Transaction should be sealed");
    }

    @Test
    public void canSignMultiPartySingleSigTransaction() throws Exception {
        FlowTransactionResult txResult = transactionConnector.multiPartySingleSignature(
                serviceAccount.getPrivateKey(),
                testAccount.getPrivateKey(),
                testAccount.getFlowAddress(),
                serviceAccount.getFlowAddress(),
                "cadence/simple_transaction.cdc",
                500L
        );

        Assertions.assertNotNull(txResult);
        Assertions.assertSame(txResult.getStatus(), FlowTransactionStatus.SEALED, "Transaction should be sealed");
    }

    @Test
    public void canSignMultiPartyMultiSigTransaction() throws Exception {
        FlowTransactionResult txResult = transactionConnector.multiPartyMultiSignature(
                List.of(testAccount2.getPrivateKey(), testAccount2.getPrivateKey()),
                List.of(testAccount.getPrivateKey(), testAccount.getPrivateKey()),
                testAccount.getFlowAddress(),
                testAccount2.getFlowAddress(),
                "cadence/simple_transaction.cdc",
                500L
        );

        Assertions.assertNotNull(txResult);
        Assertions.assertSame(txResult.getStatus(), FlowTransactionStatus.SEALED, "Transaction should be sealed");
    }

    @Test
    public void canSignMultiParty2AuthorizersTransaction() throws Exception {
        FlowTransactionResult txResult = transactionConnector.multiParty2Authorizers(
                serviceAccount.getPrivateKey(),
                testAccount.getPrivateKey(),
                testAccount.getFlowAddress(),
                serviceAccount.getFlowAddress(),
                testAccount.getFlowAddress(),
                "cadence/simple_transaction_2_authorizers.cdc",
                500L
        );

        Assertions.assertNotNull(txResult);
        Assertions.assertSame(txResult.getStatus(), FlowTransactionStatus.SEALED, "Transaction should be sealed");
    }
}
