package org.onflow.examples.java;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.onflow.flow.sdk.*;
import org.onflow.flow.sdk.cadence.AddressField;
import org.onflow.flow.sdk.cadence.StringField;
import org.onflow.flow.sdk.cadence.UFix64NumberField;
import org.onflow.flow.sdk.cadence.UInt8NumberField;
import org.onflow.flow.sdk.crypto.Crypto;
import org.onflow.flow.sdk.crypto.PrivateKey;
import org.onflow.flow.sdk.crypto.PublicKey;

public final class AccessAPIConnector {
    private final FlowAccessApi accessAPI;
    private final PrivateKey privateKey;

    public AccessAPIConnector(PrivateKey privateKey, FlowAccessApi accessApiConnection) {
        this.accessAPI = accessApiConnection;
        this.privateKey = privateKey;
    }

    public FlowId getLatestBlockID() {
        FlowAccessApi.AccessApiCallResponse<FlowBlockHeader> response = accessAPI.getLatestBlockHeader(true);
        if (response instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            return ((FlowAccessApi.AccessApiCallResponse.Success<FlowBlockHeader>) response).getData().getId();
        } else {
            throw new RuntimeException(((FlowAccessApi.AccessApiCallResponse.Error) response).getMessage(), ((FlowAccessApi.AccessApiCallResponse.Error) response).getThrowable());
        }
    }

    public FlowAccount getAccount(FlowAddress address) {
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

    public FlowAccountKey getAccountKey(FlowAddress address, int keyIndex) {
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

    public FlowTransactionResult waitForSeal(FlowId txID) {
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

    public FlowTransaction createTransaction(FlowAddress payerAddress, FlowAccountKey payerAccountKey, FlowScript script, List<FlowArgument> arguments) {
        return new FlowTransaction(
                script,
                arguments,
                getLatestBlockID(),
                500L,
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
    }

    public FlowId signAndSendTransaction(FlowTransaction tx, FlowAddress payerAddress, FlowAccountKey payerAccountKey) {
        Signer signer = Crypto.getSigner(privateKey, payerAccountKey.getHashAlgo());
        tx = tx.addEnvelopeSignature(payerAddress, payerAccountKey.getId(), signer);
        return sendTransaction(tx);
    }

    public void transferTokens(FlowAddress senderAddress, FlowAddress recipientAddress, BigDecimal amount) {
        if (amount.scale() != 8) {
            throw new RuntimeException("FLOW amount must have exactly 8 decimal places of precision (e.g. 10.00000000)");
        }

        FlowAccountKey senderAccountKey = getAccountKey(senderAddress, 0);
        FlowScript script = new FlowScript(ExamplesUtils.loadScript("cadence/transfer_flow.cdc"));
        List<FlowArgument> arguments = List.of(
                new FlowArgument(new UFix64NumberField(amount.toPlainString())),
                new FlowArgument(new AddressField(recipientAddress.getBase16Value()))
        );

        FlowTransaction tx = createTransaction(senderAddress, senderAccountKey, script, arguments);
        FlowId txID = signAndSendTransaction(tx, senderAddress, senderAccountKey);
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

    public FlowId sendSampleTransaction(FlowAddress payerAddress, PublicKey publicKey) {
        FlowAccountKey payerAccountKey = getAccountKey(payerAddress, 0);
        FlowScript script = new FlowScript(ExamplesUtils.loadScript("cadence/create_account.cdc"));
        List<FlowArgument> arguments = List.of(
                new FlowArgument(new StringField(publicKey.getHex())),
                new FlowArgument(new UInt8NumberField(Integer.toString(publicKey.getAlgo().getIndex())))
        );
        FlowTransaction tx = createTransaction(payerAddress, payerAccountKey, script, arguments);
        return signAndSendTransaction(tx, payerAddress, payerAccountKey);
    }
}
