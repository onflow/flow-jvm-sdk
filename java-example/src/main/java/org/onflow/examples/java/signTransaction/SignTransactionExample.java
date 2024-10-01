package org.onflow.examples.java.signTransaction;

import org.jetbrains.annotations.NotNull;
import org.onflow.examples.java.AccessAPIConnector;
import org.onflow.examples.java.ExamplesUtils;
import org.onflow.flow.sdk.*;
import org.onflow.flow.sdk.crypto.Crypto;
import org.onflow.flow.sdk.crypto.PrivateKey;

import java.util.Collections;
import java.util.List;

public class SignTransactionExample {
    private final PrivateKey privateKey;
    private final FlowAccessApi accessAPI;
    private final AccessAPIConnector connector;

    public SignTransactionExample(PrivateKey privateKey, FlowAccessApi accessApiConnection) {
        this.privateKey = privateKey;
        this.accessAPI = accessApiConnection;
        this.connector = new AccessAPIConnector(privateKey, accessAPI);
    }

    public FlowTransactionResult singlePartySingleSignature(
            FlowAddress payerAddress,
            String scriptName,
            long gasLimit
    ) throws Exception {
        FlowAccountKey payerAccountKey = connector.getAccountKey(payerAddress, 0);

        FlowTransaction tx = new FlowTransaction(
                new FlowScript(ExamplesUtils.loadScript(scriptName)),
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

        Signer signer = Crypto.getSigner(privateKey, payerAccountKey.getHashAlgo());
        tx = tx.addEnvelopeSignature(payerAddress, payerAccountKey.getId(), signer);

        return getTransactionResult(tx);
    }

    public FlowTransactionResult singlePartyMultiSignature(
            FlowAddress payerAddress,
            String scriptName,
            long gasLimit
    ) throws Exception {
        FlowAccountKey payerAccountKey = connector.getAccountKey(payerAddress, 0);
        FlowAccountKey payerAccountKey2 = connector.getAccountKey(payerAddress, 1);

        FlowTransaction tx = new FlowTransaction(
                new FlowScript(ExamplesUtils.loadScript(scriptName)),
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

        Signer signer = Crypto.getSigner(privateKey, payerAccountKey.getHashAlgo());

        // Account 1 signs the envelope with key 1
        tx = tx.addEnvelopeSignature(payerAddress, payerAccountKey.getId(), signer);

        // Account 1 signs the envelope with key 2
        tx = tx.addEnvelopeSignature(payerAddress, payerAccountKey2.getId(), signer);

        return getTransactionResult(tx);
    }

    public FlowTransactionResult multiPartySingleSignature(
            PrivateKey account1PrivateKey,
            PrivateKey account2PrivateKey,
            FlowAddress payerAddress,
            FlowAddress authorizerAddress,
            String scriptName,
            long gasLimit
    ) throws Exception {
        FlowAccountKey account1Key = connector.getAccountKey(authorizerAddress, 0);
        FlowAccountKey account2Key = connector.getAccountKey(payerAddress, 0);

        FlowTransaction tx = new FlowTransaction(
                new FlowScript(ExamplesUtils.loadScript(scriptName)),
                List.of(),
                connector.getLatestBlockID(),
                gasLimit,
                new FlowTransactionProposalKey(
                        authorizerAddress,
                        account1Key.getId(),
                        account1Key.getSequenceNumber()
                ),
                payerAddress,
                List.of(authorizerAddress),
                Collections.emptyList(),
                Collections.emptyList()
        );

        // Account 1 signs the payload with key 1
        Signer account1Signer = Crypto.getSigner(account1PrivateKey, account1Key.getHashAlgo());
        tx = tx.addPayloadSignature(authorizerAddress, account1Key.getId(), account1Signer);

        // Account 2 signs the envelope with key 2
        Signer account2Signer = Crypto.getSigner(account2PrivateKey, account2Key.getHashAlgo());
        tx = tx.addEnvelopeSignature(payerAddress, account2Key.getId(), account2Signer);

        return getTransactionResult(tx);
    }

    public FlowTransactionResult multiPartyMultiSignature(
            List<PrivateKey> account1PrivateKeys,
            List<PrivateKey> account2PrivateKeys,
            FlowAddress payerAddress,
            FlowAddress authorizerAddress,
            String scriptName,
            long gasLimit
    ) throws Exception {
        FlowAccountKey account1Key1 = connector.getAccountKey(authorizerAddress, 0);
        FlowAccountKey account1Key2 = connector.getAccountKey(authorizerAddress, 1);
        FlowAccountKey account2Key1 = connector.getAccountKey(payerAddress, 0);
        FlowAccountKey account2Key2 = connector.getAccountKey(payerAddress, 1);

        FlowTransaction tx = new FlowTransaction(
                new FlowScript(ExamplesUtils.loadScript(scriptName)),
                List.of(),
                connector.getLatestBlockID(),
                gasLimit,
                new FlowTransactionProposalKey(
                        authorizerAddress,
                        account1Key1.getId(),
                        account1Key1.getSequenceNumber()
                ),
                payerAddress,
                List.of(authorizerAddress),
                Collections.emptyList(),
                Collections.emptyList()
        );

        // Account 1 signs the payload with key 1
        Signer account1Signer1 = Crypto.getSigner(account1PrivateKeys.get(0), account1Key1.getHashAlgo());
        tx = tx.addPayloadSignature(authorizerAddress, account1Key1.getId(), account1Signer1);

        // Account 1 signs the payload with key 2
        Signer account1Signer2 = Crypto.getSigner(account1PrivateKeys.get(1), account1Key2.getHashAlgo());
        tx = tx.addPayloadSignature(authorizerAddress, account1Key2.getId(), account1Signer2);

        // Account 2 signs the envelope with key 1
        Signer account2Signer1 = Crypto.getSigner(account2PrivateKeys.get(0), account2Key1.getHashAlgo());
        tx = tx.addEnvelopeSignature(payerAddress, account2Key1.getId(), account2Signer1);

        // Account 2 signs the envelope with key 2
        Signer account2Signer2 = Crypto.getSigner(account2PrivateKeys.get(1), account2Key2.getHashAlgo());
        tx = tx.addEnvelopeSignature(payerAddress, account2Key2.getId(), account2Signer2);

        return getTransactionResult(tx);
    }

    public FlowTransactionResult multiParty2Authorizers(
            PrivateKey account1PrivateKey,
            PrivateKey account2PrivateKey,
            FlowAddress payerAddress,
            FlowAddress authorizer1Address,
            FlowAddress authorizer2Address,
            String scriptName,
            long gasLimit
    ) throws Exception {
        FlowAccountKey account1Key = connector.getAccountKey(authorizer1Address, 0);
        FlowAccountKey account2Key = connector.getAccountKey(authorizer2Address, 0);

        FlowTransaction tx = new FlowTransaction(
                new FlowScript(ExamplesUtils.loadScript(scriptName)),
                List.of(),
                connector.getLatestBlockID(),
                gasLimit,
                new FlowTransactionProposalKey(
                        authorizer1Address,
                        account1Key.getId(),
                        account1Key.getSequenceNumber()
                ),
                payerAddress,
                List.of(authorizer1Address, authorizer2Address),
                Collections.emptyList(),
                Collections.emptyList()
        );

        // Account 1 signs the payload with key 1
        Signer account1Signer = Crypto.getSigner(account1PrivateKey, account1Key.getHashAlgo());
        tx = tx.addPayloadSignature(authorizer1Address, account1Key.getId(), account1Signer);

        // Account 2 signs the envelope with key 2
        Signer account2Signer = Crypto.getSigner(account2PrivateKey, account2Key.getHashAlgo());
        tx = tx.addEnvelopeSignature(authorizer2Address, account2Key.getId(), account2Signer);

        return getTransactionResult(tx);
    }

    @NotNull
    private FlowTransactionResult getTransactionResult(FlowTransaction tx) throws Exception {
        return ExamplesUtils.getFlowTransactionResult(tx, accessAPI, connector);
    }
}

