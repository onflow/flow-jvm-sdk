package org.onflow.examples.java.executeScript;

import org.onflow.examples.java.ExamplesUtils;
import org.onflow.flow.sdk.*;
import org.onflow.flow.sdk.cadence.JsonCadenceBuilder;

import java.math.BigDecimal;

import static org.onflow.flow.sdk.Script_dslKt.simpleFlowScript;

public class ExecuteScriptAccessAPIConnector {

    private final FlowAccessApi accessAPI;

    public ExecuteScriptAccessAPIConnector(FlowAccessApi accessAPI) {
        this.accessAPI = accessAPI;
    }

    public FlowScriptResponse executeSimpleScript() {
        String loadedScript = ExamplesUtils.loadScriptContent("cadence/execute_simple_script_example.cdc");

        FlowAccessApi.AccessApiCallResponse<FlowScriptResponse> response = simpleFlowScript(loadedScript, JsonCadenceBuilder.int(5))

        if (response instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            return ((FlowAccessApi.AccessApiCallResponse.Success<FlowScriptResponse>) response).getData();
        } else if (response instanceof FlowAccessApi.AccessApiCallResponse.Error) {
            FlowAccessApi.AccessApiCallResponse.Error error = (FlowAccessApi.AccessApiCallResponse.Error) response;
            throw new RuntimeException(error.getMessage(), error.getThrowable());
        }
        throw new RuntimeException("Unexpected response type");
    }

    public User executeComplexScript() {
        String loadedScript = ExamplesUtils.loadScriptContent("cadence/execute_complex_script_example.cdc");

        FlowAccessApi.AccessApiCallResponse<FlowScriptResponse> response = simpleFlowScript(loadedScript, JsonCadenceBuilder.string("my_name"));

        FlowScriptResponse value;
        if (response instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            value = ((FlowAccessApi.AccessApiCallResponse.Success<FlowScriptResponse>) response).getData();
        } else if (response instanceof FlowAccessApi.AccessApiCallResponse.Error) {
            FlowAccessApi.AccessApiCallResponse.Error error = (FlowAccessApi.AccessApiCallResponse.Error) response;
            throw new RuntimeException(error.getMessage(), error.getThrowable());
        } else {
            throw new RuntimeException("Unexpected response type");
        }

        return value.getJsonCadence().decode(User.class);
    }

    public static class User {
        private BigDecimal balance;
        private FlowAddress address;
        private String name;

        public BigDecimal getBalance() {
            return balance;
        }

        public void setBalance(BigDecimal balance) {
            this.balance = balance;
        }

        public FlowAddress getAddress() {
            return address;
        }

        public void setAddress(FlowAddress address) {
            this.address = address;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}

