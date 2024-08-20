package org.onflow.examples.java.getCollection;

import org.onflow.flow.sdk.FlowAccessApi;
import org.onflow.flow.sdk.FlowAccessApi.AccessApiCallResponse;
import org.onflow.flow.sdk.FlowCollection;
import org.onflow.flow.sdk.FlowId;

public class GetCollectionAccessAPIConnector {
    private final FlowAccessApi accessAPI;

    public GetCollectionAccessAPIConnector(FlowAccessApi accessAPI) {
        this.accessAPI = accessAPI;
    }

    public FlowCollection getCollectionById(FlowId collectionId) {
        AccessApiCallResponse<FlowCollection> response = accessAPI.getCollectionById(collectionId);
        if (response instanceof AccessApiCallResponse.Success) {
            return ((AccessApiCallResponse.Success<FlowCollection>) response).getData();
        } else {
            AccessApiCallResponse.Error errorResponse = (AccessApiCallResponse.Error) response;
            throw new RuntimeException(errorResponse.getMessage(), errorResponse.getThrowable());
        }
    }
}
