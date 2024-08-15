package org.onflow.examples.java.get_block;

import org.onflow.flow.sdk.*;

public class GetBlockAccessAPIConnector {

    private final FlowAccessApi accessAPI;

    public GetBlockAccessAPIConnector(FlowAccessApi accessAPI) {
        this.accessAPI = accessAPI;
    }

    public FlowBlock getLatestSealedBlock() {
        boolean isSealed = true;
        FlowAccessApi.AccessApiCallResponse<FlowBlock> response = accessAPI.getLatestBlock(isSealed);

        if (response instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            return ((FlowAccessApi.AccessApiCallResponse.Success<FlowBlock>) response).getData();
        } else {
            FlowAccessApi.AccessApiCallResponse.Error errorResponse = (FlowAccessApi.AccessApiCallResponse.Error) response;
            throw new RuntimeException(errorResponse.getMessage(), errorResponse.getThrowable());
        }
    }

    public FlowBlock getBlockByID(FlowId blockID) {
        FlowAccessApi.AccessApiCallResponse<FlowBlock> response = accessAPI.getBlockById(blockID);

        if (response instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            return ((FlowAccessApi.AccessApiCallResponse.Success<FlowBlock>) response).getData();
        } else {
            FlowAccessApi.AccessApiCallResponse.Error errorResponse = (FlowAccessApi.AccessApiCallResponse.Error) response;
            throw new RuntimeException(errorResponse.getMessage(), errorResponse.getThrowable());
        }
    }

    public FlowBlock getBlockByHeight(long height) {
        FlowAccessApi.AccessApiCallResponse<FlowBlock> response = accessAPI.getBlockByHeight(height);

        if (response instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            return ((FlowAccessApi.AccessApiCallResponse.Success<FlowBlock>) response).getData();
        } else {
            FlowAccessApi.AccessApiCallResponse.Error errorResponse = (FlowAccessApi.AccessApiCallResponse.Error) response;
            throw new RuntimeException(errorResponse.getMessage(), errorResponse.getThrowable());
        }
    }
}
