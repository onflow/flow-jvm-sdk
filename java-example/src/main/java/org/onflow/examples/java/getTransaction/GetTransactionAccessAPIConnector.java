package org.onflow.examples.java.getTransaction;

import org.onflow.flow.sdk.FlowAccessApi;
import org.onflow.flow.sdk.FlowId;
import org.onflow.flow.sdk.FlowTransaction;
import org.onflow.flow.sdk.FlowTransactionResult;

public class GetTransactionAccessAPIConnector {

    private final FlowAccessApi accessAPI;

    public GetTransactionAccessAPIConnector(FlowAccessApi accessAPI) {
        this.accessAPI = accessAPI;
    }

    public FlowTransaction getTransaction(FlowId txID) {
        FlowAccessApi.AccessApiCallResponse<FlowTransaction> response = accessAPI.getTransactionById(txID);
        if (response instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            return ((FlowAccessApi.AccessApiCallResponse.Success<FlowTransaction>) response).getData();
        } else {
            FlowAccessApi.AccessApiCallResponse.Error errorResponse = (FlowAccessApi.AccessApiCallResponse.Error) response;
            throw new RuntimeException(errorResponse.getMessage(), errorResponse.getThrowable());
        }
    }

    public FlowTransactionResult getTransactionResult(FlowId txID) {
        FlowAccessApi.AccessApiCallResponse<FlowTransactionResult> response = accessAPI.getTransactionResultById(txID);
        if (response instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            return ((FlowAccessApi.AccessApiCallResponse.Success<FlowTransactionResult>) response).getData();
        } else {
            FlowAccessApi.AccessApiCallResponse.Error errorResponse = (FlowAccessApi.AccessApiCallResponse.Error) response;
            throw new RuntimeException(errorResponse.getMessage(), errorResponse.getThrowable());
        }
    }
}