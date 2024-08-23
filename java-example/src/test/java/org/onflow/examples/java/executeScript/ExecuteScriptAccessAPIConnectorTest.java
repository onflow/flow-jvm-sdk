package org.onflow.examples.java.executeScript;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onflow.flow.common.test.FlowEmulatorProjectTest;
import org.onflow.flow.common.test.FlowTestClient;
import org.onflow.flow.sdk.FlowAccessApi;
import org.onflow.flow.sdk.FlowScriptResponse;

import java.util.List;
import java.util.Map;

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
        assertEquals(15, result.getJsonCadence().decodeToAny());
    }

    @Test
    void canExecuteComplexScript() {
        FlowScriptResponse result = scriptExecutionExample.executeComplexScript();

        assertNotNull(result, "Result should not be null");

        // Decode the result as a list of maps
        List<Map<String, Object>> rawList = (List<Map<String, Object>>) result.getJsonCadence().decodeToAny();

        assertNotNull(rawList, "Decoded list should not be null");
        assertEquals(1, rawList.size(), "Expected exactly one StorageInfo object");

        // Manually map the LinkedHashMap to your StorageInfo class
        Map<String, Object> rawMap = rawList.get(0);
        ExecuteScriptAccessAPIConnector.StorageInfo storageInfo = new ExecuteScriptAccessAPIConnector.StorageInfo(
                (int) rawMap.get("capacity"),
                (int) rawMap.get("used"),
                (int) rawMap.get("available")
        );

        // Verify the fields of the StorageInfo object
        assertEquals(1, storageInfo.getCapacity(), "Expected capacity to be 1");
        assertEquals(2, storageInfo.getUsed(), "Expected used to be 2");
        assertEquals(3, storageInfo.getAvailable(), "Expected available to be 3");
    }
}
