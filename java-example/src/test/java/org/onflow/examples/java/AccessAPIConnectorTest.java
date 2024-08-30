package org.onflow.examples.java;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onflow.flow.common.test.*;
import org.onflow.flow.sdk.FlowAccessApi;
import org.onflow.flow.sdk.FlowAccountKey;
import org.onflow.flow.sdk.FlowAddress;
import org.onflow.flow.sdk.SignatureAlgorithm;
import org.onflow.flow.sdk.crypto.Crypto;
import org.onflow.flow.sdk.crypto.KeyPair;
import org.onflow.flow.sdk.crypto.PublicKey;
import org.onflow.flow.sdk.crypto.PrivateKey;
import java.math.BigDecimal;
import com.google.common.io.BaseEncoding;



@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
public class AccessAPIConnectorTest {
    // user key pairs using all supported signing algorithms
    private final KeyPair[] userKeyPairs = {
            Crypto.generateKeyPair(SignatureAlgorithm.ECDSA_P256),
            Crypto.generateKeyPair(SignatureAlgorithm.ECDSA_SECP256k1)
    };

    private final FlowAddress emptyAddress = new FlowAddress("");

    // user account addresses
    private final FlowAddress[] userAccountAddress = {
            emptyAddress,
            emptyAddress
    };

    @FlowServiceAccountCredentials
    private TestAccount serviceAccount;
    @FlowTestClient
    private FlowAccessApi accessAPI;
    @FlowTestAccount
    private TestAccount recipientAccount;

    @BeforeEach
    public void setupUser() {

    }

    @Test
    public void canTransferTokens() {
        AccessAPIConnector accessAPIConnector = new AccessAPIConnector(serviceAccount.getPrivateKey(), accessAPI);
        FlowAddress recipient = recipientAccount.getFlowAddress();
        BigDecimal amount = new BigDecimal("10.00000001");
        BigDecimal balance1 = accessAPIConnector.getAccountBalance(recipient);
        accessAPIConnector.transferTokens(serviceAccount.getFlowAddress(), recipient, amount);
        BigDecimal balance2 = accessAPIConnector.getAccountBalance(recipient);
        Assertions.assertEquals(balance1.add(amount), balance2);
    }

    @Test
    public void canGetAnAccountBalance() {
        AccessAPIConnector accessAPIConnector = new AccessAPIConnector(serviceAccount.getPrivateKey(), accessAPI);
        BigDecimal balance = accessAPIConnector.getAccountBalance(serviceAccount.getFlowAddress());
        Assertions.assertNotNull(balance);
    }
}
