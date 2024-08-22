package org.onflow.examples.java.executeScript;

import org.onflow.examples.java.ExamplesUtils;
import org.onflow.flow.sdk.*;
import org.onflow.flow.sdk.cadence.JsonCadenceBuilder;

import java.math.BigDecimal;
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
            scriptBuilder.setScript(flowScript);
            scriptBuilder.getArguments().add(new JsonCadenceBuilder().int8(5));
            return null;
        });

        if (response instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            return ((FlowAccessApi.AccessApiCallResponse.Success<FlowScriptResponse>) response).getData();
        } else if (response instanceof FlowAccessApi.AccessApiCallResponse.Error error) {
            throw new RuntimeException(error.getMessage(), error.getThrowable());
        }
        throw new RuntimeException("Unexpected response type");
    }

    public User executeComplexScript() {
        String loadedScript = ExamplesUtils.loadScriptContent("cadence/execute_complex_script_example.cdc");
        FlowScript flowScript = new FlowScript(loadedScript.getBytes(StandardCharsets.UTF_8));

        FlowAccessApi.AccessApiCallResponse<FlowScriptResponse> response = simpleFlowScript(accessAPI, scriptBuilder -> {
            scriptBuilder.setScript(flowScript);
            scriptBuilder.getArguments().add(new JsonCadenceBuilder().string("my_name"));
            return null;
        });

        FlowScriptResponse value;
        if (response instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            value = ((FlowAccessApi.AccessApiCallResponse.Success<FlowScriptResponse>) response).getData();
        } else if (response instanceof FlowAccessApi.AccessApiCallResponse.Error error) {
            throw new RuntimeException(error.getMessage(), error.getThrowable());
        } else {
            throw new RuntimeException("Unexpected response type");
        }

        return (User) value.getJsonCadence().decodeToAny();
    }

    public static class User {
        private BigDecimal balance;
        private FlowAddress address;
        private String name;

        public BigDecimal getBalance() {
            return balance;
        }

        public FlowAddress getAddress() {
            return address;
        }

        public String getName() {
            return name;
        }

    }
}

