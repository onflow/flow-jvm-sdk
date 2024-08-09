package org.onflow.examples.java;

import org.bouncycastle.util.encoders.Hex;
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

import java.math.BigDecimal;

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
public class AccessAPIConnectorTest {
    private String userPrivateKeyHex = "";
    private String userPublicKeyHex = "";

    @FlowServiceAccountCredentials
    private TestAccount serviceAccount;

    @FlowTestClient
    private FlowAccessApi accessAPI;

    @FlowTestAccount
    private TestAccount recipientAccount;

    @BeforeEach
    public void setupUser() {
        KeyPair keyPair = Crypto.generateKeyPair(SignatureAlgorithm.ECDSA_P256);
        userPrivateKeyHex = keyPair.getPrivate().getHex();
        userPublicKeyHex = keyPair.getPublic().getHex();
    }
    @Test
    public void canCreateAnAccount() {
        AccessAPIConnector accessAPIConnector = new AccessAPIConnector(serviceAccount.getPrivateKey(), accessAPI);

        // Use the service account as a payer to create a new user account with the specified public key
        FlowAddress account = accessAPIConnector.createAccount(serviceAccount.getFlowAddress(), userPublicKeyHex);
        Assertions.assertNotNull(account);

        // Retrieve the key from the newly created account
        FlowAccountKey key = accessAPIConnector.getAccountKey(account);
        String keyHex = Hex.toHexString(key.getPublicKey().getBytes());

        Assertions.assertEquals(userPublicKeyHex, keyHex);
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
