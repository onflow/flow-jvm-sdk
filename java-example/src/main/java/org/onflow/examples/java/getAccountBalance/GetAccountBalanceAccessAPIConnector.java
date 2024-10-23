package org.onflow.examples.java.getAccountBalance;

import org.onflow.flow.sdk.FlowAccessApi;
import org.onflow.flow.sdk.FlowAddress;

public class GetAccountBalanceAccessAPIConnector {
    private final FlowAccessApi accessAPI;

    public GetAccountBalanceAccessAPIConnector(FlowAccessApi accessAPI) {
        this.accessAPI = accessAPI;
    }

    public long getBalanceAtLatestBlock(FlowAddress address) {
        FlowAccessApi.AccessApiCallResponse<Long> response = accessAPI.getAccountBalanceAtLatestBlock(address);

        if (response instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            return ((FlowAccessApi.AccessApiCallResponse.Success<Long>) response).getData();
        } else {
            FlowAccessApi.AccessApiCallResponse.Error errorResponse = (FlowAccessApi.AccessApiCallResponse.Error) response;
            throw new RuntimeException(errorResponse.getMessage(), errorResponse.getThrowable());
        }
    }

    public long getBalanceAtBlockHeight(FlowAddress address, long height) {
        FlowAccessApi.AccessApiCallResponse<Long> response = accessAPI.getAccountBalanceAtBlockHeight(address, height);

        if (response instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            return ((FlowAccessApi.AccessApiCallResponse.Success<Long>) response).getData();
        } else {
            FlowAccessApi.AccessApiCallResponse.Error errorResponse = (FlowAccessApi.AccessApiCallResponse.Error) response;
            throw new RuntimeException(errorResponse.getMessage(), errorResponse.getThrowable());
        }
    }
}
