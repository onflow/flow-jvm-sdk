package org.onflow.examples.java.executeScript;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onflow.flow.common.test.FlowEmulatorProjectTest;
import org.onflow.flow.common.test.FlowTestClient;
import org.onflow.flow.sdk.FlowAccessApi;
import org.onflow.flow.sdk.FlowScriptResponse;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
class ExecuteScriptAccessAPIConnectorTest {

    @FlowTestClient
    private FlowAccessApi accessAPI;

    private ExecuteScriptAccessAPIConnector scriptExecutionExample;

    @BeforeEach
    void setup() {
        scriptExecutionExample = new ExecuteScriptAccessAPIConnector(accessAPI);
    }

    @Test
    void canExecuteSimpleScript() {
        FlowScriptResponse result = scriptExecutionExample.executeSimpleScript();

        assertNotNull(result, "Result should not be null");
        assertEquals(15, result.getJsonCadence().decode(Integer.class));
    }

    @Test
    void canExecuteComplexScript() {
        ExecuteScriptAccessAPIConnector.User user = scriptExecutionExample.executeComplexScript();

        assertNotNull(user, "User should not be null");
        assertEquals("my_name", user.getName());
        assertEquals("0x1", user.getAddress().getBase16Value());
        assertEquals(new BigDecimal("10.0"), user.getBalance());
    }
}
