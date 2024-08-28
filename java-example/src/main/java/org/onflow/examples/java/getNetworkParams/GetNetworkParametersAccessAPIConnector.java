package org.onflow.examples.java.getNetworkParams;

import org.onflow.flow.sdk.FlowAccessApi;
import org.onflow.flow.sdk.FlowChainId;

public class GetNetworkParametersAccessAPIConnector {
    private final FlowAccessApi accessAPI;

    public GetNetworkParametersAccessAPIConnector(FlowAccessApi accessAPI) {
        this.accessAPI = accessAPI;
    }

    public FlowChainId getNetworkParameters() {
        FlowAccessApi.AccessApiCallResponse<FlowChainId> response = accessAPI.getNetworkParameters();
        if (response instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            return ((FlowAccessApi.AccessApiCallResponse.Success<FlowChainId>) response).getData();
        } else {
            FlowAccessApi.AccessApiCallResponse.Error errorResponse = (FlowAccessApi.AccessApiCallResponse.Error) response;
            throw new RuntimeException(errorResponse.getMessage(), errorResponse.getThrowable());
        }
    }
}

