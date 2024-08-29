package org.onflow.examples.java.verifySignature.userSignatureValidateAll;

import org.onflow.examples.java.ExamplesUtils;
import org.onflow.flow.sdk.*;
import org.onflow.flow.sdk.cadence.*;
import org.onflow.flow.sdk.crypto.Crypto;
import org.onflow.flow.sdk.crypto.PrivateKey;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class UserSignatureValidateAllExample {
    private final FlowAccessApi accessAPI;

    public UserSignatureValidateAllExample(FlowAccessApi accessAPI) {
        this.accessAPI = accessAPI;
    }

    public Field<?> verifyUserSignatureValidateAll(
            FlowAddress aliceAddress,
            PrivateKey alicePrivateKey
    ) throws Exception {

        byte[] message = ExamplesUtils.toUnsignedByteArray("ananas".getBytes(StandardCharsets.UTF_8));

        Signer signerAlice1 = Crypto.getSigner(alicePrivateKey, HashAlgorithm.SHA3_256);
        Signer signerAlice2 = Crypto.getSigner(alicePrivateKey, HashAlgorithm.SHA3_256);

        byte[] signatureAlice1 = signerAlice1.sign(message);
        byte[] signatureAlice2 = signerAlice2.sign(message);

        // The signature indexes correspond to the key indexes on the address
        ArrayField keyIndexes = new ArrayField(Arrays.asList(
                new IntNumberField("0"),
                new IntNumberField("1")
        ));

        // Prepare the Cadence arguments
        ArrayField signatures = new ArrayField(Arrays.asList(
                new StringField(ExamplesUtils.toHexString(signatureAlice1)),
                new StringField(ExamplesUtils.toHexString(signatureAlice2))
        ));

        // Call the script to verify the signatures on-chain
        FlowAccessApi.AccessApiCallResponse<FlowScriptResponse> response = FlowScriptHelper.simpleFlowScript(accessAPI, builder -> {
            builder.script(new FlowScript(ExamplesUtils.loadScriptContent("cadence/user_signature_validate_all.cdc")));
            builder.arguments(Arrays.asList(
                    new AddressField(aliceAddress.getBytes()),
                    signatures,
                    keyIndexes,
                    new StringField(new String(message, StandardCharsets.UTF_8))
            ));
            return null;
        });

        if (response instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            return ((FlowAccessApi.AccessApiCallResponse.Success<FlowScriptResponse>) response).getData().getJsonCadence();
        } else if (response instanceof FlowAccessApi.AccessApiCallResponse.Error) {
            throw new Exception(((FlowAccessApi.AccessApiCallResponse.Error) response).getMessage(),
                    ((FlowAccessApi.AccessApiCallResponse.Error) response).getThrowable());
        } else {
            throw new Exception("Unknown response type");
        }
    }
}
