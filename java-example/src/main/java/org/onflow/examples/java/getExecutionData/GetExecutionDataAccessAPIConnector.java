package org.onflow.examples.java.getExecutionData;

import org.onflow.flow.sdk.FlowAccessApi;
import org.onflow.flow.sdk.FlowExecutionResult;
import org.onflow.flow.sdk.FlowId;

public class GetExecutionDataAccessAPIConnector {
    private final FlowAccessApi accessAPI;

    public GetExecutionDataAccessAPIConnector(FlowAccessApi accessAPI) {
        this.accessAPI = accessAPI;
    }

    public FlowExecutionResult getExecutionDataByBlockId(FlowId blockId) {
        FlowAccessApi.AccessApiCallResponse<FlowExecutionResult> response = accessAPI.getExecutionResultByBlockId(blockId);
        if (response instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            return ((FlowAccessApi.AccessApiCallResponse.Success<FlowExecutionResult>) response).getData();
        } else {
            FlowAccessApi.AccessApiCallResponse.Error errorResponse = (FlowAccessApi.AccessApiCallResponse.Error) response;
            throw new RuntimeException(errorResponse.getMessage(), errorResponse.getThrowable());
        }
    }
}

