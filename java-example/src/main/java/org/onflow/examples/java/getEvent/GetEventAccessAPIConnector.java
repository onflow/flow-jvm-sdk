package org.onflow.examples.java.getEvent;

import kotlin.ranges.LongRange;
import org.onflow.flow.sdk.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GetEventAccessAPIConnector {
    private final FlowAccessApi accessAPI;

    public GetEventAccessAPIConnector(FlowAccessApi accessAPI) {
        this.accessAPI = accessAPI;
    }

    public List<FlowEventResult> getEventsForHeightRange(String eventType, long startHeight, long endHeight) {
        LongRange range = new LongRange(startHeight, endHeight);
        FlowAccessApi.AccessApiCallResponse<List<FlowEventResult>> response = accessAPI.getEventsForHeightRange(eventType, range);
        if (response instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            return ((FlowAccessApi.AccessApiCallResponse.Success<List<FlowEventResult>>) response).getData();
        } else if (response instanceof FlowAccessApi.AccessApiCallResponse.Error) {
            FlowAccessApi.AccessApiCallResponse.Error errorResponse = (FlowAccessApi.AccessApiCallResponse.Error) response;
            throw new RuntimeException(errorResponse.getMessage(), errorResponse.getThrowable());
        } else {
            throw new RuntimeException("Unknown response type");
        }
    }

    public List<FlowEventResult> getEventsForBlockIds(String eventType, List<FlowId> blockIds) {
        Set<FlowId> blockIdSet = new HashSet<>(blockIds);
        FlowAccessApi.AccessApiCallResponse<List<FlowEventResult>> response = accessAPI.getEventsForBlockIds(eventType, blockIdSet);
        if (response instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            return ((FlowAccessApi.AccessApiCallResponse.Success<List<FlowEventResult>>) response).getData();
        } else if (response instanceof FlowAccessApi.AccessApiCallResponse.Error errorResponse) {
            throw new RuntimeException(errorResponse.getMessage(), errorResponse.getThrowable());
        } else {
            throw new RuntimeException("Unknown response type");
        }
    }

    public FlowTransactionResult getTransactionResult(FlowId txID) {
        FlowAccessApi.AccessApiCallResponse<FlowTransactionResult> response = accessAPI.getTransactionResultById(txID);
        if (response instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            return ((FlowAccessApi.AccessApiCallResponse.Success<FlowTransactionResult>) response).getData();
        } else if (response instanceof FlowAccessApi.AccessApiCallResponse.Error errorResponse) {
            throw new RuntimeException(errorResponse.getMessage(), errorResponse.getThrowable());
        } else {
            throw new RuntimeException("Unknown response type");
        }
    }

    public List<FlowEventResult> getAccountCreatedEvents(long startHeight, long endHeight) {
        return getEventsForHeightRange("flow.AccountCreated", startHeight, endHeight);
    }
}