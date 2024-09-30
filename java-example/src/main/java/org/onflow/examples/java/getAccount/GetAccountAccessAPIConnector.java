package org.onflow.examples.java.getAccount;

import org.onflow.flow.sdk.*;
import java.math.BigDecimal;

public class GetAccountAccessAPIConnector {
    private final FlowAccessApi accessAPI;

    public GetAccountAccessAPIConnector(FlowAccessApi accessAPI) {
        this.accessAPI = accessAPI;
    }

    public FlowAccount getAccountAtLatestBlock(FlowAddress address) {
        FlowAccessApi.AccessApiCallResponse<FlowAccount> response = accessAPI.getAccountAtLatestBlock(address);
        if (response instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            return ((FlowAccessApi.AccessApiCallResponse.Success<FlowAccount>) response).getData();
        } else {
            FlowAccessApi.AccessApiCallResponse.Error errorResponse = (FlowAccessApi.AccessApiCallResponse.Error) response;
            throw new RuntimeException(errorResponse.getMessage(), errorResponse.getThrowable());
        }
    }


    public FlowAccount getAccountAtBlockHeight(FlowAddress address, long height) {
        FlowAccessApi.AccessApiCallResponse<FlowAccount> response = accessAPI.getAccountByBlockHeight(address, height);
        if (response instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            return ((FlowAccessApi.AccessApiCallResponse.Success<FlowAccount>) response).getData();
        } else {
            FlowAccessApi.AccessApiCallResponse.Error errorResponse = (FlowAccessApi.AccessApiCallResponse.Error) response;
            throw new RuntimeException(errorResponse.getMessage(), errorResponse.getThrowable());
        }
    }

    public BigDecimal getAccountBalance(FlowAddress address) {
        FlowAccount account = getAccountAtLatestBlock(address);
        return account.getBalance();
    }
}
