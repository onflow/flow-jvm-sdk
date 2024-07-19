package org.onflow.examples.java;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onflow.flow.sdk.FlowAddress;
import org.onflow.flow.sdk.crypto.Crypto;
import org.onflow.flow.sdk.crypto.KeyPair;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AccessAPIConnectorTest {
    public static final String SERVICE_PRIVATE_KEY_HEX = "a2f983853e61b3e27d94b7bf3d7094dd756aead2a813dd5cf738e1da56fa9c17";

    private final FlowAddress serviceAccountAddress = new FlowAddress("f8d6e0586b0a20c7");
    private final FlowAddress testRecipientAddress = new FlowAddress("01cf0e2f2f715450");
    private String userPublicKeyHex;

    @BeforeEach
    void setupUser() {
        KeyPair keyPair = Crypto.generateKeyPair();
        this.userPublicKeyHex = keyPair.getPublic().getHex();
    }

    @Test
    void createAccount() {

        AccessAPIConnector accessAPIConnector = new AccessAPIConnector("localhost", 3569, SERVICE_PRIVATE_KEY_HEX);

        FlowAddress account = accessAPIConnector.createAccount(serviceAccountAddress, this.userPublicKeyHex);
        assertNotNull(account);
    }

    @Test
    void transferTokens() throws Exception {

        AccessAPIConnector accessAPIConnector = new AccessAPIConnector("localhost", 3569, SERVICE_PRIVATE_KEY_HEX);

        var recipient = testRecipientAddress;

        // FLOW amounts always have 8 decimal places
        var amount = new BigDecimal("10.00000001");

        var balance1 = accessAPIConnector.getAccountBalance(recipient);

        accessAPIConnector.transferTokens(serviceAccountAddress, recipient, amount);

        var balance2 = accessAPIConnector.getAccountBalance(recipient);

        assertEquals(balance1.add(amount), balance2);
    }

    @Test
    void getAccountBalance() {

        AccessAPIConnector accessAPIConnector = new AccessAPIConnector("localhost", 3569, SERVICE_PRIVATE_KEY_HEX);
        var balance = accessAPIConnector.getAccountBalance(serviceAccountAddress);
        assertNotNull(balance);
    }
}
