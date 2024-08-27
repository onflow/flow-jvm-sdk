package org.onflow.examples.java.addKey;

import org.jetbrains.annotations.NotNull;
import org.onflow.examples.java.AccessAPIConnector;
import org.onflow.examples.java.ExamplesUtils;
import org.onflow.flow.sdk.*;
import org.onflow.flow.sdk.cadence.StringField;
import org.onflow.flow.sdk.cadence.UFix64NumberField;
import org.onflow.flow.sdk.cadence.UInt8NumberField;
import org.onflow.flow.sdk.crypto.Crypto;
import org.onflow.flow.sdk.crypto.PrivateKey;
import java.util.Collections;
import java.util.List;

public class AddAccountKeyExample {

    private final PrivateKey privateKey;
    private final FlowAccessApi accessAPI;
    private final AccessAPIConnector connector;

    public AddAccountKeyExample(PrivateKey privateKey, FlowAccessApi accessApiConnection) {
        this.privateKey = privateKey;
        this.accessAPI = accessApiConnection;
        this.connector = new AccessAPIConnector(privateKey, accessAPI);
    }

    public FlowTransactionResult addKeyToAccount(
            FlowAddress payerAddress,
            String scriptName,
            long gasLimit
    ) throws Exception {
        FlowAccountKey payerAccountKey = connector.getAccountKey(payerAddress, 0);

        FlowTransaction tx = new FlowTransaction(
                new FlowScript(ExamplesUtils.loadScript(scriptName)),
                List.of(
                        new FlowArgument(new StringField(payerAccountKey.getPublicKey().getStringValue())),
                        new FlowArgument(new UInt8NumberField(String.valueOf(payerAccountKey.getSignAlgo().getIndex()))),
                        new FlowArgument(new UInt8NumberField(String.valueOf(payerAccountKey.getHashAlgo().getIndex()))),
                        new FlowArgument(new UFix64NumberField("1000"))
                ),
                connector.getLatestBlockID(),
                gasLimit,
                new FlowTransactionProposalKey(
                        payerAddress,
                        payerAccountKey.getId(),
                        payerAccountKey.getSequenceNumber()
                ),
                payerAddress,
                List.of(payerAddress),
                Collections.emptyList(),
                Collections.emptyList()
        );

        Signer signer = Crypto.getSigner(privateKey, payerAccountKey.getHashAlgo());
        tx = tx.addEnvelopeSignature(payerAddress, payerAccountKey.getId(), signer);

        return getTransactionResult(tx);
    }

    @NotNull
    private FlowTransactionResult getTransactionResult(FlowTransaction tx) throws Exception {
        return ExamplesUtils.getFlowTransactionResult(tx, accessAPI, connector);
    }
}
