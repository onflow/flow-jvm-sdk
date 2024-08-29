package org.onflow.examples.java.verifySignature.userSignatureValidateAny;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onflow.flow.common.test.FlowEmulatorProjectTest;
import org.onflow.flow.common.test.FlowTestAccount;
import org.onflow.flow.common.test.FlowTestClient;
import org.onflow.flow.common.test.TestAccount;
import org.onflow.flow.sdk.FlowAccessApi;
import org.onflow.flow.sdk.cadence.BooleanField;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
public class UserSignatureValidateAnyExampleTest {
    @FlowTestAccount
    private TestAccount testAccount;
    @FlowTestClient
    private FlowAccessApi accessAPI;
    private UserSignatureValidateAnyExample connector;

    @BeforeEach
    public void setup() {
        connector = new UserSignatureValidateAnyExample(accessAPI);
    }

    @Test
    public void canVerifyUserSignature() {
        try {
            BooleanField txResult = connector.verifyUserSignatureValidateAny(testAccount.getFlowAddress(), testAccount.getPrivateKey(), "ananas");

            if (txResult != null) {
                assertEquals(Boolean.TRUE, txResult.getValue(), "Signature verification failed");
            } else {
                fail("Signature verification failed, no transaction result received");
            }
        } catch (Exception e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }
}
