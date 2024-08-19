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
            // emptyAddress
    };

    @FlowServiceAccountCredentials
    private TestAccount serviceAccount;

    @FlowTestClient
    private FlowAccessApi accessAPI;

    @FlowTestAccount
    private TestAccount recipientAccount;

    @BeforeEach
    public void setupUser() {
        for (int index = 0; index < userAccountAddress.length; index++) {
            FlowAddress address = userAccountAddress[index];
            if (address.equals(emptyAddress)) {
                // create test accounts
                userAccountAddress[index] = createUserAccount(userKeyPairs[index].getPublic());
                // make sure test accounts have enough tokens for the tests
                BigDecimal amount = new BigDecimal("10.00000001");
                transferTokens(serviceAccount.getFlowAddress(), serviceAccount.getPrivateKey(), userAccountAddress[index], amount);
            }
        }
    }

    // create an account using the service account
    private FlowAddress createUserAccount(PublicKey userPublicKey) {
        AccessAPIConnector accessAPIConnector = new AccessAPIConnector(serviceAccount.getPrivateKey(), accessAPI);
        FlowAddress account = accessAPIConnector.createAccount(serviceAccount.getFlowAddress(), userPublicKey);
        return account;
    }

    // create an account using the service account
    private void transferTokens(FlowAddress sender, PrivateKey senderKey, FlowAddress to, BigDecimal amount) {
        AccessAPIConnector accessAPIConnector = new AccessAPIConnector(senderKey, accessAPI);
        accessAPIConnector.transferTokens(sender, to, amount);
    }

    private String bytesToHex(byte[] data) {
        return BaseEncoding.base16().lowerCase().encode(data);
    }

    @Test
    public void canCreateAnAccount() {
        // accounts are already created in `setupUser`
        for (int index = 0; index < userAccountAddress.length; index++) {
            FlowAddress address = userAccountAddress[index];
            Assertions.assertNotNull(address);
            // check account key is the expected one
            AccessAPIConnector accessAPIConnector = new AccessAPIConnector(serviceAccount.getPrivateKey(), accessAPI);
            FlowAccountKey newAccountKey = accessAPIConnector.getAccountKey(address, 0);
            Assertions.assertEquals(userKeyPairs[index].getPublic().getHex(), bytesToHex(newAccountKey.getPublicKey().getBytes()));
        }
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
