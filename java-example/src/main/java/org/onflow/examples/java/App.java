package org.onflow.examples.java;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.bouncycastle.util.encoders.Hex;
import org.onflow.flow.sdk.*;
import org.onflow.flow.sdk.cadence.AddressField;
import org.onflow.flow.sdk.cadence.StringField;
import org.onflow.flow.sdk.cadence.UFix64NumberField;
import org.onflow.flow.sdk.crypto.Crypto;
import org.onflow.flow.sdk.crypto.PrivateKey;
import java.util.logging.Logger;

public final class App {
    private static final Logger logger = Logger.getLogger(App.class.getName());
    private final FlowAccessApi accessAPI;
    private final PrivateKey privateKey;

    public App(String host, int port, String privateKeyHex) {
        this.accessAPI = Flow.newAccessApi(host, port);
        this.privateKey = Crypto.decodePrivateKey(privateKeyHex);
    }

    private boolean isValidHex(String hex) {
        return hex.matches("[0-9a-fA-F]+");
    }

    public FlowAddress createAccount(FlowAddress payerAddress, String publicKeyHex) {
        FlowAccountKey payerAccountKey = getAccountKey(payerAddress, 0);

        FlowAccountKey newAccountPublicKey = new FlowAccountKey(
                0,
                new FlowPublicKey(publicKeyHex),
                SignatureAlgorithm.ECDSA_P256,
                HashAlgorithm.SHA3_256,
                1,
                0,
                false);

        FlowTransaction tx = new FlowTransaction(
                new FlowScript(Objects.requireNonNull(loadScript("create_account.cdc"))),
                List.of(new FlowArgument(new StringField(Hex.toHexString(newAccountPublicKey.getEncoded())))),
                this.getLatestBlockID(),
                100L,
                new FlowTransactionProposalKey(
                        payerAddress,
                        payerAccountKey.getId(),
                        payerAccountKey.getSequenceNumber()),
                payerAddress,
                List.of(payerAddress),
                new ArrayList<>(),
                new ArrayList<>());

        Signer signer = Crypto.getSigner(privateKey, payerAccountKey.getHashAlgo());
        tx = tx.addEnvelopeSignature(payerAddress, payerAccountKey.getId(), signer);
        FlowId txID = accessAPI.sendTransaction(tx);

        FlowTransactionResult txResult = waitForSeal(txID);

        FlowAddress newAddress = getAccountCreatedAddress(txResult);

        return newAddress;
    }

    public void transferTokens(FlowAddress senderAddress, FlowAddress recipientAddress, BigDecimal amount) throws Exception {
        // exit early
        if (amount.scale() != 8) {
            throw new Exception("FLOW amount must have exactly 8 decimal places of precision (e.g. 10.00000000)");
        }

        FlowAccountKey senderAccountKey = this.getAccountKey(senderAddress, 0);
        FlowTransaction tx = new FlowTransaction(
                new FlowScript(Objects.requireNonNull(loadScript("transfer_flow.cdc"))),
                Arrays.asList(
                        new FlowArgument(new UFix64NumberField(amount.toPlainString())),
                        new FlowArgument(new AddressField(recipientAddress.getBase16Value()))),
                this.getLatestBlockID(),
                100L,
                new FlowTransactionProposalKey(
                        senderAddress,
                        senderAccountKey.getId(),
                        senderAccountKey.getSequenceNumber()),
                senderAddress,
                List.of(senderAddress),
                new ArrayList<>(),
                new ArrayList<>());

        Signer signer = Crypto.getSigner(this.privateKey, senderAccountKey.getHashAlgo());
        tx = tx.addEnvelopeSignature(senderAddress, senderAccountKey.getId(), signer);

        FlowId txID = this.accessAPI.sendTransaction(tx);
        this.waitForSeal(txID);
    }

    public FlowAccount getAccount(FlowAddress address) {
        return this.accessAPI.getAccountAtLatestBlock(address);
    }

    public BigDecimal getAccountBalance(FlowAddress address) {
        FlowAccount account = this.getAccount(address);
        return account.getBalance();
    }

    private FlowId getLatestBlockID() {
        return this.accessAPI.getLatestBlockHeader(true).getId();
    }

    private FlowAccountKey getAccountKey(FlowAddress address, int keyIndex) {
        FlowAccount account = getAccount(address);
        return account.getKeys().get(keyIndex);
    }

    private FlowTransactionResult getTransactionResult(FlowId txID) {
        return this.accessAPI.getTransactionResultById(txID);
    }

    private FlowTransactionResult waitForSeal(FlowId txID) {
        FlowTransactionResult txResult;

        while(true) {
            txResult = this.getTransactionResult(txID);
            if (txResult.getStatus().equals(FlowTransactionStatus.SEALED)) {
                return txResult;
            }

            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private FlowAddress getAccountCreatedAddress(FlowTransactionResult txResult) {
        if (!txResult.getStatus().equals(FlowTransactionStatus.SEALED)
            || !txResult.getErrorMessage().isEmpty()) {
            return null;
        }

        String rez = Objects.requireNonNull(Objects.requireNonNull(txResult
                        .getEvents()
                        .getFirst()
                        .getEvent()
                        .getValue())
                .getFields()[0]
                .getValue()
                .getValue()).toString();
        return new FlowAddress(rez.substring(2));
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
}
