package org.onflow.examples.java;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onflow.flow.sdk.FlowAccessApi;
import org.onflow.flow.sdk.FlowAddress;
import org.onflow.flow.sdk.crypto.Crypto;
import org.onflow.flow.sdk.crypto.KeyPair;
import org.onflow.flow.sdk.test.*;

import java.math.BigDecimal;

@FlowEmulatorTest
public class AccessAPIConnectorTest {
    private String userPublicKeyHex = "";

    @FlowServiceAccountCredentials
    private TestAccount serviceAccount;

    @FlowTestClient
    private FlowAccessApi accessAPI;

    @FlowTestAccount
    private TestAccount recipientAccount;

    @BeforeEach
    public void setupUser() {
        KeyPair keyPair = Crypto.generateKeyPair();
        keyPair.getPrivate().getHex();
        userPublicKeyHex = keyPair.getPublic().getHex();
    }

    @Test
    public void canCreateAnAccount() {
        AccessAPIConnector accessAPIConnector = new AccessAPIConnector(serviceAccount.getPrivateKey(), accessAPI);
        FlowAddress account = accessAPIConnector.createAccount(serviceAccount.getFlowAddress(), userPublicKeyHex);
        Assertions.assertNotNull(account);
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
