package org.onflow.examples.java;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onflow.flow.sdk.FlowAddress;
import org.onflow.flow.sdk.crypto.Crypto;
import org.onflow.flow.sdk.crypto.KeyPair;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AppTest {

    public static final String SERVICE_PRIVATE_KEY_HEX = "a2f983853e61b3e27d94b7bf3d7094dd756aead2a813dd5cf738e1da56fa9c17";

    private final FlowAddress serviceAccountAddress = new FlowAddress("f8d6e0586b0a20c7");
    private String userPublicKeyHex;

    @BeforeEach
    void setupUser() {
        KeyPair keyPair = Crypto.generateKeyPair();
        this.userPublicKeyHex = keyPair.getPublic().getHex();
    }

    @Test
    void createAccount() {

        App app = new App("localhost", 3569, SERVICE_PRIVATE_KEY_HEX);

        // service account address

        FlowAddress account = app.createAccount(serviceAccountAddress, this.userPublicKeyHex);
        assertNotNull(account);
    }

    @Test
    void transferTokens() throws Exception {

        App app = new App("localhost", 3569, SERVICE_PRIVATE_KEY_HEX);

        // service account address
        var sender = serviceAccountAddress;
        var recipient = app.createAccount(sender, this.userPublicKeyHex);

        // FLOW amounts always have 8 decimal places
        var amount = new BigDecimal("10.00000001");

        var balance1 = app.getAccountBalance(recipient);

        app.transferTokens(sender, recipient, amount);

        var balance2 = app.getAccountBalance(recipient);

        assertEquals(balance1.add(amount), balance2);
    }

    @Test
    void getAccountBalance() {

        App app = new App("localhost", 3569, SERVICE_PRIVATE_KEY_HEX);
        var balance = app.getAccountBalance(serviceAccountAddress);

        System.out.println(balance);
    }
}
