package org.onflow.examples.java.sendTransaction;

import org.onflow.examples.java.AccessAPIConnector;
import org.onflow.examples.java.ExamplesUtils;
import org.onflow.flow.sdk.*;
import org.onflow.flow.sdk.cadence.StringField;
import org.onflow.flow.sdk.crypto.Crypto;
import org.onflow.flow.sdk.crypto.PrivateKey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SendTransactionExample {
    private final PrivateKey privateKey;
    private final FlowAccessApi accessAPI;
    private final AccessAPIConnector connector;

    public SendTransactionExample(PrivateKey privateKey, FlowAccessApi accessApiConnection) {
        this.privateKey = privateKey;
        this.accessAPI = accessApiConnection;
        this.connector = new AccessAPIConnector(privateKey, accessAPI);
    }

    public FlowTransactionResult sendSimpleTransaction(
            FlowAddress payerAddress,
            String scriptName,
            long gasLimit
    ) throws Exception {
        FlowAccountKey payerAccountKey = connector.getAccountKey(payerAddress, 0);

        FlowTransaction tx = new FlowTransaction(
                new FlowScript(ExamplesUtils.loadScript(scriptName)),
                new ArrayList<>(),
                connector.getLatestBlockID(),
                gasLimit,
                new FlowTransactionProposalKey(
                        payerAddress,
                        payerAccountKey.getId(),
                        payerAccountKey.getSequenceNumber()
                ),
                payerAddress,
                Collections.singletonList(payerAddress),
                Collections.emptyList(),
                Collections.emptyList()
        );

        Signer signer = Crypto.getSigner(privateKey, payerAccountKey.getHashAlgo());
        tx = tx.addEnvelopeSignature(payerAddress, payerAccountKey.getId(), signer);

        FlowId txID;
        FlowAccessApi.AccessApiCallResponse<?> response = accessAPI.sendTransaction(tx);
        if (response instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            txID = ((FlowAccessApi.AccessApiCallResponse.Success<FlowId>) response).getData();
        } else if (response instanceof FlowAccessApi.AccessApiCallResponse.Error) {
            throw new Exception(((FlowAccessApi.AccessApiCallResponse.Error) response).getMessage(),
                    ((FlowAccessApi.AccessApiCallResponse.Error) response).getThrowable());
        } else {
            throw new Exception("Unknown response type");
        }

        return connector.waitForSeal(txID);
    }

    public FlowTransactionResult sendComplexTransactionWithArguments(
            FlowAddress payerAddress,
            String scriptName,
            long gasLimit,
            String greeting
    ) throws Exception {
        FlowAccountKey payerAccountKey = connector.getAccountKey(payerAddress, 0);

        FlowTransaction tx = new FlowTransaction(
                new FlowScript(ExamplesUtils.loadScript(scriptName)),
                List.of(new FlowArgument(new StringField(greeting))),
                connector.getLatestBlockID(),
                gasLimit,
                new FlowTransactionProposalKey(
                        payerAddress,
                        payerAccountKey.getId(),
                        payerAccountKey.getSequenceNumber()
                ),
                payerAddress,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
        );

        Signer signer = Crypto.getSigner(privateKey, payerAccountKey.getHashAlgo());
        tx = tx.addEnvelopeSignature(payerAddress, payerAccountKey.getId(), signer);

        FlowId txID;
        FlowAccessApi.AccessApiCallResponse<?> response = accessAPI.sendTransaction(tx);
        if (response instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            txID = ((FlowAccessApi.AccessApiCallResponse.Success<FlowId>) response).getData();
        } else if (response instanceof FlowAccessApi.AccessApiCallResponse.Error) {
            throw new Exception(((FlowAccessApi.AccessApiCallResponse.Error) response).getMessage(),
                    ((FlowAccessApi.AccessApiCallResponse.Error) response).getThrowable());
        } else {
            throw new Exception("Unknown response type");
        }

        return connector.waitForSeal(txID);
    }
}