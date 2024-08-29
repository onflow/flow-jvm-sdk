package org.onflow.examples.java.verifySignature.userSignature;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onflow.flow.common.test.FlowEmulatorProjectTest;
import org.onflow.flow.common.test.FlowTestAccount;
import org.onflow.flow.common.test.FlowTestClient;
import org.onflow.flow.common.test.TestAccount;
import org.onflow.flow.sdk.FlowAccessApi;
import org.onflow.flow.sdk.cadence.BooleanField;
import org.onflow.flow.sdk.cadence.Field;

import static org.junit.jupiter.api.Assertions.*;

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
public class UserSignatureExampleTest {

    @FlowTestAccount
    private TestAccount testAccount;

    @FlowTestAccount
    private TestAccount testAccount2;

    @FlowTestClient
    private FlowAccessApi accessAPI;

    private UserSignatureExample connector;

    @BeforeEach
    public void setup() {
        connector = new UserSignatureExample(accessAPI);
    }

    @Test
    public void canVerifyUserSignature() {
        try {
            Field<?> txResult = connector.verifyUserSignature(testAccount.getFlowAddress(), testAccount2.getFlowAddress());

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
