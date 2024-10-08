package org.onflow.examples.java.getNodeVersionInfo;

import org.onflow.flow.sdk.FlowAccessApi;
import org.onflow.flow.sdk.FlowNodeVersionInfo;

public class GetNodeVersionInfoAccessAPIConnector {
    private final FlowAccessApi accessAPI;

    public GetNodeVersionInfoAccessAPIConnector(FlowAccessApi accessAPI) {
        this.accessAPI = accessAPI;
    }

    public FlowNodeVersionInfo getNodeVersionInfo() {
        FlowAccessApi.AccessApiCallResponse<FlowNodeVersionInfo> response = accessAPI.getNodeVersionInfo();
        if (response instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            return ((FlowAccessApi.AccessApiCallResponse.Success<FlowNodeVersionInfo>) response).getData();
        } else {
            FlowAccessApi.AccessApiCallResponse.Error errorResponse = (FlowAccessApi.AccessApiCallResponse.Error) response;
            throw new RuntimeException(errorResponse.getMessage(), errorResponse.getThrowable());
        }
    }
}

