package org.onflow.examples.java.getCollection;

import org.onflow.flow.sdk.FlowAccessApi;
import org.onflow.flow.sdk.FlowAccessApi.AccessApiCallResponse;
import org.onflow.flow.sdk.FlowCollection;
import org.onflow.flow.sdk.FlowId;
import org.onflow.flow.sdk.FlowTransaction;

import java.util.List;

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

    public List<FlowTransaction> getFullCollectionById(FlowId collectionId) {
        AccessApiCallResponse<List<FlowTransaction>> response = accessAPI.getFullCollectionById(collectionId);
        if (response instanceof AccessApiCallResponse.Success) {
            return ((AccessApiCallResponse.Success<List<FlowTransaction>>) response).getData();
        } else {
            AccessApiCallResponse.Error errorResponse = (AccessApiCallResponse.Error) response;
            throw new RuntimeException(errorResponse.getMessage(), errorResponse.getThrowable());
        }
    }
}
