package org.onflow.examples.java.getAccountKeys;

import org.onflow.flow.sdk.FlowAccessApi;
import org.onflow.flow.sdk.FlowAccountKey;
import org.onflow.flow.sdk.FlowAddress;

import java.util.List;

public class GetAccountKeysAccessAPIConnector {
    private final FlowAccessApi accessAPI;

    public GetAccountKeysAccessAPIConnector(FlowAccessApi accessAPI) {
        this.accessAPI = accessAPI;
    }

    public FlowAccountKey getAccountKeyAtLatestBlock(FlowAddress address, int keyIndex) {
        FlowAccessApi.AccessApiCallResponse<FlowAccountKey> response = accessAPI.getAccountKeyAtLatestBlock(address, keyIndex);

        if (response instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            return ((FlowAccessApi.AccessApiCallResponse.Success<FlowAccountKey>) response).getData();
        } else {
            FlowAccessApi.AccessApiCallResponse.Error errorResponse = (FlowAccessApi.AccessApiCallResponse.Error) response;
            throw new RuntimeException(errorResponse.getMessage(), errorResponse.getThrowable());
        }
    }

    public FlowAccountKey getAccountKeyAtBlockHeight(FlowAddress address, int keyIndex, long height) {
        FlowAccessApi.AccessApiCallResponse<FlowAccountKey> response = accessAPI.getAccountKeyAtBlockHeight(address, keyIndex, height);

        if (response instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            return ((FlowAccessApi.AccessApiCallResponse.Success<FlowAccountKey>) response).getData();
        } else {
            FlowAccessApi.AccessApiCallResponse.Error errorResponse = (FlowAccessApi.AccessApiCallResponse.Error) response;
            throw new RuntimeException(errorResponse.getMessage(), errorResponse.getThrowable());
        }
    }

    public List<FlowAccountKey> getAccountKeysAtLatestBlock(FlowAddress address) {
        FlowAccessApi.AccessApiCallResponse<List<FlowAccountKey>> response = accessAPI.getAccountKeysAtLatestBlock(address);

        if (response instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            return ((FlowAccessApi.AccessApiCallResponse.Success<List<FlowAccountKey>>) response).getData();
        } else {
            FlowAccessApi.AccessApiCallResponse.Error errorResponse = (FlowAccessApi.AccessApiCallResponse.Error) response;
            throw new RuntimeException(errorResponse.getMessage(), errorResponse.getThrowable());
        }
    }

    public List<FlowAccountKey> getAccountKeysAtBlockHeight(FlowAddress address, long height) {
        FlowAccessApi.AccessApiCallResponse<List<FlowAccountKey>> response = accessAPI.getAccountKeysAtBlockHeight(address, height);

        if (response instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            return ((FlowAccessApi.AccessApiCallResponse.Success<List<FlowAccountKey>>) response).getData();
        } else {
            FlowAccessApi.AccessApiCallResponse.Error errorResponse = (FlowAccessApi.AccessApiCallResponse.Error) response;
            throw new RuntimeException(errorResponse.getMessage(), errorResponse.getThrowable());
        }
    }
}
