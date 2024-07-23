package org.onflow.examples.java;

import org.bouncycastle.util.encoders.Hex;
import org.onflow.flow.sdk.*;
import org.onflow.flow.sdk.cadence.AddressField;
import org.onflow.flow.sdk.cadence.StringField;
import org.onflow.flow.sdk.cadence.UFix64NumberField;
import org.onflow.flow.sdk.crypto.Crypto;
import org.onflow.flow.sdk.crypto.PrivateKey;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public final class AccessAPIConnector {
    private final FlowAccessApi accessAPI;
    private final PrivateKey privateKey;

    public AccessAPIConnector(String privateKeyHex, FlowAccessApi accessApiConnection) {
        this.accessAPI = accessApiConnection;
        this.privateKey = Crypto.decodePrivateKey(privateKeyHex);
    }

    private FlowId getLatestBlockID() {
        FlowAccessApi.AccessApiCallResponse<FlowBlockHeader> response = accessAPI.getLatestBlockHeader(true);
        if (response instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            return ((FlowAccessApi.AccessApiCallResponse.Success<FlowBlockHeader>) response).getData().getId();
        } else {
            throw new RuntimeException(((FlowAccessApi.AccessApiCallResponse.Error) response).getMessage());
        }
    }

    private FlowAccount getAccount(FlowAddress address) {
        FlowAccessApi.AccessApiCallResponse<FlowAccount> response = accessAPI.getAccountAtLatestBlock(address);
        if (response instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            return ((FlowAccessApi.AccessApiCallResponse.Success<FlowAccount>) response).getData();
        } else {
            throw new RuntimeException(((FlowAccessApi.AccessApiCallResponse.Error) response).getMessage());
        }
    }

    public BigDecimal getAccountBalance(FlowAddress address) {
        FlowAccount account = getAccount(address);
        return account.getBalance();
    }

    private FlowAccountKey getAccountKey(FlowAddress address, int keyIndex) {
        FlowAccount account = getAccount(address);
        return account.getKeys().get(keyIndex);
    }

    private FlowTransactionResult getTransactionResult(FlowId txID) {
        FlowAccessApi.AccessApiCallResponse<FlowTransactionResult> response = accessAPI.getTransactionResultById(txID);
        if (response instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            FlowTransactionResult result = ((FlowAccessApi.AccessApiCallResponse.Success<FlowTransactionResult>) response).getData();
            if (!result.getErrorMessage().isEmpty()) {
                throw new RuntimeException(result.getErrorMessage());
            }
            return result;
        } else {
            throw new RuntimeException(((FlowAccessApi.AccessApiCallResponse.Error) response).getMessage());
        }
    }

    private FlowTransactionResult waitForSeal(FlowId txID) {
        while (true) {
            FlowTransactionResult txResult = getTransactionResult(txID);
            if (txResult.getStatus().equals(FlowTransactionStatus.SEALED)) {
                return txResult;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }
    }

    private FlowAddress getAccountCreatedAddress(FlowTransactionResult txResult) {
        if (!txResult.getStatus().equals(FlowTransactionStatus.SEALED) || !txResult.getErrorMessage().isEmpty()) {
            throw new RuntimeException("Transaction failed: " + txResult.getErrorMessage());
        }
        String addressHex = (String) txResult.getEvents().get(0).getEvent().getValue().getFields()[0].getValue().getValue();
        return new FlowAddress(addressHex.substring(2));
    }

    private byte[] loadScript(String name) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(name)) {
            if (is == null) {
                throw new IOException("Script " + name + " not found.");
            }
            return is.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load script " + name, e);
        }
    }

    public FlowAddress createAccount(FlowAddress payerAddress, String publicKeyHex) {
        FlowAccountKey payerAccountKey = getAccountKey(payerAddress, 0);

        FlowAccountKey newAccountPublicKey = new FlowAccountKey(
                -1, // id
                new FlowPublicKey(publicKeyHex), // publicKey
                SignatureAlgorithm.ECDSA_P256, // signAlgo
                HashAlgorithm.SHA3_256, // hashAlgo
                1000, // weight
                -1, // sequenceNumber
                false // revoked
        );

        FlowTransaction tx = new FlowTransaction(
                new FlowScript(loadScript("create_account.cdc")),
                List.of(new FlowArgument(new StringField(Hex.toHexString(newAccountPublicKey.getPublicKey().getBytes())))),
                getLatestBlockID(),
                100L,
                new FlowTransactionProposalKey(
                        payerAddress,
                        payerAccountKey.getId(),
                        payerAccountKey.getSequenceNumber()
                ),
                payerAddress,
                List.of(payerAddress),
                Collections.emptyList(), // payloadSignatures
                Collections.emptyList() // envelopeSignatures
        );

        Signer signer = Crypto.getSigner(privateKey, payerAccountKey.getHashAlgo());
        tx = tx.addEnvelopeSignature(payerAddress, payerAccountKey.getId(), signer);

        FlowAccessApi.AccessApiCallResponse<FlowId> response = accessAPI.sendTransaction(tx);
        FlowId txID;
        if (response instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            txID = ((FlowAccessApi.AccessApiCallResponse.Success<FlowId>) response).getData();
        } else {
            throw new RuntimeException(((FlowAccessApi.AccessApiCallResponse.Error) response).getMessage());
        }

        FlowTransactionResult txResult = waitForSeal(txID);
        return getAccountCreatedAddress(txResult);
    }

    public void transferTokens(FlowAddress senderAddress, FlowAddress recipientAddress, BigDecimal amount) {
        if (amount.scale() != 8) {
            throw new IllegalArgumentException("FLOW amount must have exactly 8 decimal places of precision (e.g. 10.00000000)");
        }
        FlowAccountKey senderAccountKey = getAccountKey(senderAddress, 0);

        FlowTransaction tx = new FlowTransaction(
                new FlowScript(loadScript("transfer_flow.cdc")),
                List.of(
                        new FlowArgument(new UFix64NumberField(amount.toPlainString())),
                        new FlowArgument(new AddressField(recipientAddress.getBase16Value()))
                ),
                getLatestBlockID(),
                100L,
                new FlowTransactionProposalKey(
                        senderAddress,
                        senderAccountKey.getId(),
                        senderAccountKey.getSequenceNumber()
                ),
                senderAddress,
                List.of(senderAddress),
                Collections.emptyList(), // payloadSignatures
                Collections.emptyList() // envelopeSignatures
        );

        Signer signer = Crypto.getSigner(privateKey, senderAccountKey.getHashAlgo());
        tx = tx.addEnvelopeSignature(senderAddress, senderAccountKey.getId(), signer);

        FlowAccessApi.AccessApiCallResponse<FlowId> response = accessAPI.sendTransaction(tx);
        if (response instanceof FlowAccessApi.AccessApiCallResponse.Error) {
            throw new RuntimeException(((FlowAccessApi.AccessApiCallResponse.Error) response).getMessage());
        }

        waitForSeal(((FlowAccessApi.AccessApiCallResponse.Success<FlowId>) response).getData());
    }
}
