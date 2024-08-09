package org.onflow.examples.java;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.onflow.flow.sdk.FlowAccessApi;
import org.onflow.flow.sdk.FlowAccount;
import org.onflow.flow.sdk.FlowAccountKey;
import org.onflow.flow.sdk.FlowAddress;
import org.onflow.flow.sdk.FlowArgument;
import org.onflow.flow.sdk.FlowBlockHeader;
import org.onflow.flow.sdk.FlowId;
import org.onflow.flow.sdk.FlowScript;
import org.onflow.flow.sdk.FlowTransaction;
import org.onflow.flow.sdk.FlowTransactionProposalKey;
import org.onflow.flow.sdk.FlowTransactionResult;
import org.onflow.flow.sdk.FlowTransactionStatus;
import org.onflow.flow.sdk.Signer;
import org.onflow.flow.sdk.cadence.AddressField;
import org.onflow.flow.sdk.cadence.StringField;
import org.onflow.flow.sdk.cadence.UFix64NumberField;
import org.onflow.flow.sdk.crypto.Crypto;
import org.onflow.flow.sdk.crypto.PrivateKey;

public final class AccessAPIConnector {
    private final FlowAccessApi accessAPI;
    private final PrivateKey privateKey;

    public AccessAPIConnector(PrivateKey privateKey, FlowAccessApi accessApiConnection) {
        this.accessAPI = accessApiConnection;
        this.privateKey = privateKey;
    }

    private FlowId getLatestBlockID() {
        FlowAccessApi.AccessApiCallResponse<FlowBlockHeader> response = accessAPI.getLatestBlockHeader(true);
        if (response instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            return ((FlowAccessApi.AccessApiCallResponse.Success<FlowBlockHeader>) response).getData().getId();
        } else {
            throw new RuntimeException(((FlowAccessApi.AccessApiCallResponse.Error) response).getMessage(), ((FlowAccessApi.AccessApiCallResponse.Error) response).getThrowable());
        }
    }

    private FlowAccount getAccount(FlowAddress address) {
        FlowAccessApi.AccessApiCallResponse<FlowAccount> response = accessAPI.getAccountAtLatestBlock(address);
        if (response instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            return ((FlowAccessApi.AccessApiCallResponse.Success<FlowAccount>) response).getData();
        } else {
            throw new RuntimeException(((FlowAccessApi.AccessApiCallResponse.Error) response).getMessage(), ((FlowAccessApi.AccessApiCallResponse.Error) response).getThrowable());
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
            throw new RuntimeException(((FlowAccessApi.AccessApiCallResponse.Error) response).getMessage(), ((FlowAccessApi.AccessApiCallResponse.Error) response).getThrowable());
        }
    }

    private FlowTransactionResult waitForSeal(FlowId txID) {
        while (true) {
            FlowTransactionResult txResult = getTransactionResult(txID);
            if (txResult.getStatus() == FlowTransactionStatus.SEALED) {
                return txResult;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private FlowAddress getAccountCreatedAddress(FlowTransactionResult txResult) {
        String addressHex = (String) txResult.getEvents().get(0).getEvent().getValue().getFields()[0].getValue().getValue();
        return new FlowAddress(addressHex.split("\\.")[1]);
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

        FlowScript script = new FlowScript(loadScript("cadence/create_account.cdc"));

        FlowTransaction tx = new FlowTransaction(
                script,
                List.of(new FlowArgument(new StringField(publicKeyHex))),
                getLatestBlockID(),
                100L,
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

        FlowId txID = sendTransaction(tx);

        FlowTransactionResult txResult = waitForSeal(txID);
        return getAccountCreatedAddress(txResult);
    }

    public void transferTokens(FlowAddress senderAddress, FlowAddress recipientAddress, BigDecimal amount) {
        if (amount.scale() != 8) {
            throw new RuntimeException("FLOW amount must have exactly 8 decimal places of precision (e.g. 10.00000000)");
        }

        FlowAccountKey senderAccountKey = getAccountKey(senderAddress, 0);

        FlowScript script = new FlowScript(loadScript("cadence/transfer_flow.cdc"));

        List<FlowArgument> arguments = List.of(
                new FlowArgument(new UFix64NumberField(amount.toPlainString())),
                new FlowArgument(new AddressField(recipientAddress.getBase16Value()))
        );

        FlowTransaction tx = new FlowTransaction(
                script,
                arguments,
                getLatestBlockID(),
                100L,
                new FlowTransactionProposalKey(
                        senderAddress,
                        senderAccountKey.getId(),
                        senderAccountKey.getSequenceNumber()
                ),
                senderAddress,
                Collections.singletonList(senderAddress),
                Collections.emptyList(),
                Collections.emptyList()
        );

        Signer signer = Crypto.getSigner(privateKey, senderAccountKey.getHashAlgo());
        tx = tx.addEnvelopeSignature(senderAddress, senderAccountKey.getId(), signer);

        FlowId txID = sendTransaction(tx);
        waitForSeal(txID);
    }

    private FlowId sendTransaction(FlowTransaction tx) {
        FlowAccessApi.AccessApiCallResponse<FlowId> response = accessAPI.sendTransaction(tx);
        if (response instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            return ((FlowAccessApi.AccessApiCallResponse.Success<FlowId>) response).getData();
        } else {
            throw new RuntimeException(((FlowAccessApi.AccessApiCallResponse.Error) response).getMessage(), ((FlowAccessApi.AccessApiCallResponse.Error) response).getThrowable());
        }
    }
}
