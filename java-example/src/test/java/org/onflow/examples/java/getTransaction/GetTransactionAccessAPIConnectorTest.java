package org.onflow.examples.java.getTransaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onflow.examples.java.AccessAPIConnector;
import org.onflow.flow.common.test.FlowEmulatorProjectTest;
import org.onflow.flow.common.test.FlowServiceAccountCredentials;
import org.onflow.flow.common.test.FlowTestClient;
import org.onflow.flow.common.test.TestAccount;
import org.onflow.flow.sdk.*;
import org.onflow.flow.sdk.crypto.Crypto;
import org.onflow.flow.sdk.crypto.PublicKey;

import static org.junit.jupiter.api.Assertions.*;

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
public class GetTransactionAccessAPIConnectorTest {
    @FlowServiceAccountCredentials
    private TestAccount serviceAccount;
    @FlowTestClient
    private FlowAccessApi accessAPI;
    private GetTransactionAccessAPIConnector connector;
    private FlowId txID;
    private FlowBlock block;

    @BeforeEach
    public void setup() {
        AccessAPIConnector accessAPIConnector = new AccessAPIConnector(serviceAccount.getPrivateKey(), accessAPI);
        connector = new GetTransactionAccessAPIConnector(accessAPI);

        // Send a sample transaction to create an account and capture the transaction ID
        PublicKey publicKey = Crypto.generateKeyPair(SignatureAlgorithm.ECDSA_P256).getPublic();
        txID = accessAPIConnector.sendSampleTransaction(
                serviceAccount.getFlowAddress(),
                publicKey
        );

        FlowAccessApi.AccessApiCallResponse<FlowBlock> response = accessAPI.getLatestBlock(true, false);
        if (response instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            block = ((FlowAccessApi.AccessApiCallResponse.Success<FlowBlock>) response).getData();
        } else {
            FlowAccessApi.AccessApiCallResponse.Error errorResponse = (FlowAccessApi.AccessApiCallResponse.Error) response;
            throw new RuntimeException(errorResponse.getMessage(), errorResponse.getThrowable());
        }
    }

    @Test
    public void canFetchTransaction() {
        FlowTransaction transaction = connector.getTransaction(txID);

        assertNotNull(transaction, "Transaction should not be null");
        assertEquals(txID, transaction.getId(), "Transaction ID should match");
    }

    @Test
    public void canFetchTransactionResult() {
        FlowTransactionResult transactionResult = connector.getTransactionResult(txID);

        assertNotNull(transactionResult, "Transaction result should not be null");
        assertSame(transactionResult.getStatus(), FlowTransactionStatus.SEALED, "Transaction should be sealed");
    }

    @Test
    public void canFetchTransactionResultByIndex() {
        FlowTransactionResult transactionResult = connector.getTransactionResultByIndex(block.getId(),0);

        assertNotNull(transactionResult, "Transaction result should not be null");
        assertSame(transactionResult.getStatus(), FlowTransactionStatus.SEALED, "Transaction should be sealed");
    }
}
