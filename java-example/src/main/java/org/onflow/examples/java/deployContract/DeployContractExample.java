package org.onflow.examples.java.deployContract;

import org.jetbrains.annotations.NotNull;
import org.onflow.examples.java.AccessAPIConnector;
import org.onflow.examples.java.ExamplesUtils;
import org.onflow.flow.sdk.*;
import org.onflow.flow.sdk.crypto.Crypto;
import org.onflow.flow.sdk.crypto.PrivateKey;

import java.util.Collections;
import java.util.List;

public class DeployContractExample {
    private final PrivateKey privateKey;
    private final FlowAccessApi accessAPI;
    private final AccessAPIConnector connector;

    public DeployContractExample(PrivateKey privateKey, FlowAccessApi accessApiConnection) {
        this.privateKey = privateKey;
        this.accessAPI = accessApiConnection;
        this.connector = new AccessAPIConnector(privateKey, accessAPI);
    }

    public FlowTransactionResult deployContract(
            FlowAddress payerAddress,
            String contractName,
            String scriptName,
            long gasLimit
    ) throws Exception {
        FlowAccountKey payerAccountKey = connector.getAccountKey(payerAddress, 0);
        Signer signer = Crypto.getSigner(privateKey, payerAccountKey.getHashAlgo());

        String contractCode = ExamplesUtils.loadScriptContent(scriptName)
                .replace("\"", "\\\"") // Escape double quotes
                .replace("\n", "\\n");  // Escape newlines

        String contractScript = String.format(
                "transaction() {" +
                        "    prepare(signer: auth(AddContract) &Account) {" +
                        "        signer.contracts.add(" +
                        "            name: \"%s\"," +
                        "            code: \"%s\".utf8" +
                        "        )" +
                        "    }" +
                        "}", contractName, contractCode);

        FlowTransaction tx = new FlowTransaction(
                new FlowScript(contractScript.getBytes()),
                List.of(),
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

        tx = tx.addEnvelopeSignature(payerAddress, payerAccountKey.getId(), signer);

        return getTransactionResult(tx);
    }

    @NotNull
    private FlowTransactionResult getTransactionResult(FlowTransaction tx) throws Exception {
        return ExamplesUtils.getFlowTransactionResult(tx, accessAPI, connector);
    }
}