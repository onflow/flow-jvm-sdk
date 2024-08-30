package org.onflow.examples.java.createAccount;

import com.google.common.io.BaseEncoding;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.onflow.examples.java.AccessAPIConnector;
import org.onflow.flow.common.test.*;
import org.onflow.flow.sdk.FlowAccessApi;
import org.onflow.flow.sdk.FlowAddress;
import org.onflow.flow.sdk.SignatureAlgorithm;
import org.onflow.flow.sdk.crypto.Crypto;
import org.onflow.flow.sdk.crypto.KeyPair;
import org.onflow.flow.sdk.crypto.PublicKey;
import org.onflow.flow.sdk.FlowAccountKey;

import java.math.BigDecimal;

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
public class CreateAccountExampleTest {

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

    // create an account using the service account
    private FlowAddress createUserAccount(PublicKey userPublicKey) {
        CreateAccountExample createAccountExample = new CreateAccountExample(serviceAccount.getPrivateKey(), accessAPI);
        return createAccountExample.createAccount(serviceAccount.getFlowAddress(), userPublicKey);
    }

    private String bytesToHex(byte[] data) {
        return BaseEncoding.base16().lowerCase().encode(data);
    }

    @Test
    public void canCreateAnAccount() {
        for (int index = 0; index < userAccountAddress.length; index++) {
            FlowAddress address = userAccountAddress[index];
            if (address.equals(emptyAddress)) {
                // create test accounts
                userAccountAddress[index] = createUserAccount(userKeyPairs[index].getPublic());
            }
        }

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
    public void canGetAccountBalance() {
        AccessAPIConnector accessAPIConnector = new AccessAPIConnector(serviceAccount.getPrivateKey(), accessAPI);
        BigDecimal balance = accessAPIConnector.getAccountBalance(serviceAccount.getFlowAddress());
        Assertions.assertNotNull(balance);
    }
}
