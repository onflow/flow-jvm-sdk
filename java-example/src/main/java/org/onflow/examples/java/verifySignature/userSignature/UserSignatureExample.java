package org.onflow.examples.java.verifySignature.userSignature;

import org.onflow.examples.java.ExamplesUtils;
import org.onflow.flow.sdk.*;
import org.onflow.flow.sdk.cadence.*;
import org.onflow.flow.sdk.crypto.Crypto;
import org.onflow.flow.sdk.crypto.KeyPair;
import org.onflow.flow.sdk.crypto.PrivateKey;
import org.onflow.flow.sdk.crypto.PublicKey;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

public class UserSignatureExample {
    private final FlowAccessApi accessAPI;

    public UserSignatureExample(FlowAccessApi accessAPI) {
        this.accessAPI = accessAPI;
    }

    public Field<?> verifyUserSignature(FlowAddress aliceAddress, FlowAddress bobAddress) throws Exception {
        // Create the keys
        KeyPair keyPairAlice = Crypto.generateKeyPair(SignatureAlgorithm.ECDSA_P256);
        PrivateKey privateKeyAlice = keyPairAlice.getPrivate();
        PublicKey publicKeyAlice = keyPairAlice.getPublic();

        KeyPair keyPairBob = Crypto.generateKeyPair(SignatureAlgorithm.ECDSA_P256);
        PrivateKey privateKeyBob = keyPairBob.getPrivate();
        PublicKey publicKeyBob = keyPairBob.getPublic();

        // Create the message that will be signed
        UFix64NumberField amount = new UFix64NumberField("100.00");
        byte[] amountBigEndianBytes = toBigEndianBytes(Objects.requireNonNull(amount.getValue()));

        byte[] message = ExamplesUtils.toUnsignedByteArray(concatenate(
                aliceAddress.getBytes(),
                bobAddress.getBytes(),
                amountBigEndianBytes
        ));

        Signer signerAlice = Crypto.getSigner(privateKeyAlice, HashAlgorithm.SHA3_256);
        Signer signerBob = Crypto.getSigner(privateKeyBob, HashAlgorithm.SHA3_256);

        // Sign the message with Alice and Bob
        byte[] signatureAlice = signerAlice.sign(message);
        byte[] signatureBob = signerBob.sign(message);

        // Each signature has half weight
        UFix64NumberField weightAlice = new UFix64NumberField("0.5");
        UFix64NumberField weightBob = new UFix64NumberField("0.5");

        // Call the script to verify the signatures on-chain
        FlowAccessApi.AccessApiCallResponse<FlowScriptResponse> response = FlowScriptHelper.simpleFlowScript(accessAPI, builder -> {
            String scriptCode = ExamplesUtils.loadScriptContent("cadence/user_signature.cdc");

            builder.script(new FlowScript(scriptCode));

            builder.arguments(args -> Arrays.asList(
                    new ArrayField(Arrays.asList(
                            new StringField(publicKeyAlice.getHex()),
                            new StringField(publicKeyBob.getHex())
                    )),
                    new ArrayField(Arrays.asList(
                            new UFix64NumberField(Objects.requireNonNull(weightAlice.getValue())),
                            new UFix64NumberField(Objects.requireNonNull(weightBob.getValue()))
                    )),
                    new ArrayField(Arrays.asList(
                            new StringField(ExamplesUtils.toHexString(signatureAlice)),
                            new StringField(ExamplesUtils.toHexString(signatureBob))
                    )),
                    new AddressField(aliceAddress.getBytes()),
                    new AddressField(bobAddress.getBytes()),
                    new UFix64NumberField(amount.getValue())
            ));
            return null;
        });

        if (response instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            return ((FlowAccessApi.AccessApiCallResponse.Success<FlowScriptResponse>) response).getData().getJsonCadence();
        } else {
            throw new Exception(((FlowAccessApi.AccessApiCallResponse.Error) response).getMessage(),
                    ((FlowAccessApi.AccessApiCallResponse.Error) response).getThrowable());
        }
    }

    private byte[] toBigEndianBytes(String value) {
        BigInteger ufix64Value = new BigInteger(value.replace(".", ""))
                .multiply(BigInteger.TEN.pow(8 - value.split("\\.")[1].length()));

        byte[] byteArray = ufix64Value.toByteArray();
        if (byteArray.length < 8) {
            byte[] result = new byte[8];
            System.arraycopy(byteArray, 0, result, 8 - byteArray.length, byteArray.length);
            return result;
        } else {
            return byteArray;
        }
    }

    private byte[] concatenate(byte[]... arrays) {
        int length = 0;
        for (byte[] array : arrays) {
            length += array.length;
        }

        byte[] result = new byte[length];
        int pos = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, result, pos, array.length);
            pos += array.length;
        }

        return result;
    }
}
