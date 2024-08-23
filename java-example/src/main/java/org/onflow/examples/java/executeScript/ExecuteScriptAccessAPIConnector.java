package org.onflow.examples.java.executeScript;

import org.onflow.examples.java.ExamplesUtils;
import org.onflow.flow.sdk.*;
import org.onflow.flow.sdk.cadence.IntNumberField;
import org.onflow.flow.sdk.cadence.JsonCadenceBuilder;
import java.nio.charset.StandardCharsets;

import static org.onflow.flow.sdk.Script_dslKt.simpleFlowScript;

public class ExecuteScriptAccessAPIConnector {

    private final FlowAccessApi accessAPI;

    public ExecuteScriptAccessAPIConnector(FlowAccessApi accessAPI) {
        this.accessAPI = accessAPI;
    }

    public FlowScriptResponse executeSimpleScript() {
        String loadedScript = ExamplesUtils.loadScriptContent("cadence/execute_simple_script_example.cdc");
        FlowScript flowScript = new FlowScript(loadedScript.getBytes(StandardCharsets.UTF_8));

        FlowAccessApi.AccessApiCallResponse<FlowScriptResponse> response = simpleFlowScript(accessAPI, scriptBuilder -> {
            scriptBuilder.script(flowScript);
            scriptBuilder.getArguments().add(new IntNumberField("5"));
            return null;
        });

        if (response instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            return ((FlowAccessApi.AccessApiCallResponse.Success<FlowScriptResponse>) response).getData();
        } else if (response instanceof FlowAccessApi.AccessApiCallResponse.Error error) {
            throw new RuntimeException(error.getMessage(), error.getThrowable());
        }
        throw new RuntimeException("Unexpected response type");
    }

    public FlowScriptResponse executeComplexScript() {
        String loadedScript = ExamplesUtils.loadScriptContent("cadence/execute_complex_script_example.cdc");
        FlowScript flowScript = new FlowScript(loadedScript.getBytes(StandardCharsets.UTF_8));

        FlowAccessApi.AccessApiCallResponse<FlowScriptResponse> response = simpleFlowScript(accessAPI, scriptBuilder -> {
            scriptBuilder.setScript(flowScript);
            scriptBuilder.getArguments().add(new JsonCadenceBuilder().address("0x84221fe0294044d7"));
            return null;
        });

        FlowScriptResponse value;
        if (response instanceof FlowAccessApi.AccessApiCallResponse.Success) {
           return  ((FlowAccessApi.AccessApiCallResponse.Success<FlowScriptResponse>) response).getData();
        } else if (response instanceof FlowAccessApi.AccessApiCallResponse.Error error) {
            throw new RuntimeException(error.getMessage(), error.getThrowable());
        } else {
            throw new RuntimeException("Unexpected response type");
        }
    }

    public static class StorageInfo {
        private final int capacity;
        private final int used;
        private final int available;

        public StorageInfo(int capacity, int used, int available) {
            this.capacity = capacity;
            this.used = used;
            this.available = available;
        }

        public int getCapacity() {
            return capacity;
        }

        public int getUsed() {
            return used;
        }

        public int getAvailable() {
            return available;
        }
    }
}

