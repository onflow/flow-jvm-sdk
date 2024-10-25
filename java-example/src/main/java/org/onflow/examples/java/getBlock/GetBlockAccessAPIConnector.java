package org.onflow.examples.java.getBlock;

import org.onflow.flow.sdk.*;

public class GetBlockAccessAPIConnector {
    private final FlowAccessApi accessAPI;

    public GetBlockAccessAPIConnector(FlowAccessApi accessAPI) {
        this.accessAPI = accessAPI;
    }

    public FlowBlock getLatestSealedBlock() {
        boolean isSealed = true;
        boolean fullBlockResponse = false;
        FlowAccessApi.AccessApiCallResponse<FlowBlock> response = accessAPI.getLatestBlock(isSealed, fullBlockResponse);

        if (response instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            return ((FlowAccessApi.AccessApiCallResponse.Success<FlowBlock>) response).getData();
        } else {
            FlowAccessApi.AccessApiCallResponse.Error errorResponse = (FlowAccessApi.AccessApiCallResponse.Error) response;
            throw new RuntimeException(errorResponse.getMessage(), errorResponse.getThrowable());
        }
    }

    public FlowBlock getBlockByID(FlowId blockID) {
        boolean fullBlockResponse = false;
        FlowAccessApi.AccessApiCallResponse<FlowBlock> response = accessAPI.getBlockById(blockID, fullBlockResponse);

        if (response instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            return ((FlowAccessApi.AccessApiCallResponse.Success<FlowBlock>) response).getData();
        } else {
            FlowAccessApi.AccessApiCallResponse.Error errorResponse = (FlowAccessApi.AccessApiCallResponse.Error) response;
            throw new RuntimeException(errorResponse.getMessage(), errorResponse.getThrowable());
        }
    }

    public FlowBlock getBlockByHeight(long height) {
        boolean fullBlockResponse = false;
        FlowAccessApi.AccessApiCallResponse<FlowBlock> response = accessAPI.getBlockByHeight(height, fullBlockResponse);

        if (response instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            return ((FlowAccessApi.AccessApiCallResponse.Success<FlowBlock>) response).getData();
        } else {
            FlowAccessApi.AccessApiCallResponse.Error errorResponse = (FlowAccessApi.AccessApiCallResponse.Error) response;
            throw new RuntimeException(errorResponse.getMessage(), errorResponse.getThrowable());
        }
    }
}
