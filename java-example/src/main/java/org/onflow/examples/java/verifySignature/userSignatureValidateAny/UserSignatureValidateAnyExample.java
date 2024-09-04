package org.onflow.examples.java.verifySignature.userSignatureValidateAny;

import org.onflow.examples.java.ExamplesUtils;
import org.onflow.flow.sdk.*;
import org.onflow.flow.sdk.cadence.*;
import org.onflow.flow.sdk.crypto.Crypto;
import org.onflow.flow.sdk.crypto.PrivateKey;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class UserSignatureValidateAnyExample {
    private final FlowAccessApi accessAPI;

    public UserSignatureValidateAnyExample(FlowAccessApi accessAPI) {
        this.accessAPI = accessAPI;
    }

    public BooleanField verifyUserSignatureValidateAny(
            FlowAddress aliceAddress,
            PrivateKey alicePrivateKey,
            String message
    ) throws Exception {

        // Convert the message to an unsigned byte array
        byte[] messageBytes = ExamplesUtils.toUnsignedByteArray(message.getBytes(StandardCharsets.UTF_8));

        // Sign the message with Alice's key
        Signer signerAlice = Crypto.getSigner(alicePrivateKey, HashAlgorithm.SHA3_256);
        byte[] signatureAlice = signerAlice.sign(messageBytes);
        String signatureAliceHex = ExamplesUtils.toHexString(signatureAlice);

        // Execute the script to verify the signature on-chain
        FlowAccessApi.AccessApiCallResponse<FlowScriptResponse> response = FlowScriptHelper.simpleFlowScript(accessAPI, builder -> {
            builder.script(new FlowScript(ExamplesUtils.loadScriptContent("cadence/user_signature_validate_any.cdc")));
            builder.arguments(Arrays.asList(
                    new AddressField(aliceAddress.getBytes()),
                    new StringField(signatureAliceHex),
                    new StringField(message)
            ));
            return null;
        });

        if (response instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            return (BooleanField) ((FlowAccessApi.AccessApiCallResponse.Success<FlowScriptResponse>) response).getData().getJsonCadence();
        } else if (response instanceof FlowAccessApi.AccessApiCallResponse.Error) {
            throw new Exception(((FlowAccessApi.AccessApiCallResponse.Error) response).getMessage(),
                    ((FlowAccessApi.AccessApiCallResponse.Error) response).getThrowable());
        } else {
            throw new Exception("Unknown response type");
        }
    }
}