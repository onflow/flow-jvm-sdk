package org.onflow.examples.java.getProtocolState;

import org.onflow.flow.sdk.FlowAccessApi;
import org.onflow.flow.sdk.FlowId;
import org.onflow.flow.sdk.FlowSnapshot;

public class GetProtocolStateAccessAPIConnector {
    private final FlowAccessApi accessAPI;

    public GetProtocolStateAccessAPIConnector(FlowAccessApi accessAPI) {
        this.accessAPI = accessAPI;
    }

    public FlowSnapshot getLatestProtocolStateSnapshot() {
        FlowAccessApi.AccessApiCallResponse<FlowSnapshot> response = accessAPI.getLatestProtocolStateSnapshot();
        if (response instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            return ((FlowAccessApi.AccessApiCallResponse.Success<FlowSnapshot>) response).getData();
        } else {
            FlowAccessApi.AccessApiCallResponse.Error errorResponse = (FlowAccessApi.AccessApiCallResponse.Error) response;
            throw new RuntimeException(errorResponse.getMessage(), errorResponse.getThrowable());
        }
    }

    public FlowSnapshot getProtocolStateSnapshotByBlockId(FlowId blockId) {
        FlowAccessApi.AccessApiCallResponse<FlowSnapshot> response = accessAPI.getProtocolStateSnapshotByBlockId(blockId);
        if (response instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            return ((FlowAccessApi.AccessApiCallResponse.Success<FlowSnapshot>) response).getData();
        } else {
            FlowAccessApi.AccessApiCallResponse.Error errorResponse = (FlowAccessApi.AccessApiCallResponse.Error) response;
            throw new RuntimeException(errorResponse.getMessage(), errorResponse.getThrowable());
        }
    }

    public FlowSnapshot getProtocolStateSnapshotByHeight(Long height) {
        FlowAccessApi.AccessApiCallResponse<FlowSnapshot> response = accessAPI.getProtocolStateSnapshotByHeight(height);
        if (response instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            return ((FlowAccessApi.AccessApiCallResponse.Success<FlowSnapshot>) response).getData();
        } else {
            FlowAccessApi.AccessApiCallResponse.Error errorResponse = (FlowAccessApi.AccessApiCallResponse.Error) response;
            throw new RuntimeException(errorResponse.getMessage(), errorResponse.getThrowable());
        }
    }
}
