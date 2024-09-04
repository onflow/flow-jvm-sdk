package org.onflow.examples.java.verifySignature.userSignatureValidateAll;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onflow.flow.common.test.FlowEmulatorProjectTest;
import org.onflow.flow.common.test.FlowTestAccount;
import org.onflow.flow.common.test.FlowTestClient;
import org.onflow.flow.common.test.TestAccount;
import org.onflow.flow.sdk.FlowAccessApi;
import org.onflow.flow.sdk.cadence.BooleanField;
import org.onflow.flow.sdk.cadence.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
public class UserSignatureValidateAllExampleTest {
    @FlowTestAccount
    private TestAccount testAccount;
    @FlowTestClient
    private FlowAccessApi accessAPI;
    private UserSignatureValidateAllExample connector;

    @BeforeEach
    public void setup() {
        connector = new UserSignatureValidateAllExample(accessAPI);
    }

    @Test
    public void canVerifyUserSignature() {
        try {
            Field<?> txResult = connector.verifyUserSignatureValidateAll(testAccount.getFlowAddress(), testAccount.getPrivateKey());

            if (txResult instanceof BooleanField) {
                assertEquals(Boolean.TRUE, ((BooleanField) txResult).getValue(), "Signature verification failed");
            } else {
                fail("Expected BooleanField but got " + txResult.getClass().getSimpleName());
            }
        } catch (Exception e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }
}
