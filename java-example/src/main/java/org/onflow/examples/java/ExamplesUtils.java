package org.onflow.examples.java;

import org.jetbrains.annotations.NotNull;
import org.onflow.flow.sdk.*;
import org.onflow.flow.sdk.cadence.StringField;
import org.onflow.flow.sdk.cadence.UInt8NumberField;
import org.onflow.flow.sdk.crypto.PublicKey;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ExamplesUtils {

    @NotNull
    public static FlowTransactionResult getFlowTransactionResult(FlowTransaction tx, FlowAccessApi accessAPI, AccessAPIConnector connector) throws Exception {
        FlowId txID;
        FlowAccessApi.AccessApiCallResponse<?> response = accessAPI.sendTransaction(tx);
        if (response instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            txID = ((FlowAccessApi.AccessApiCallResponse.Success<FlowId>) response).getData();
        } else if (response instanceof FlowAccessApi.AccessApiCallResponse.Error) {
            throw new Exception(((FlowAccessApi.AccessApiCallResponse.Error) response).getMessage(),
                    ((FlowAccessApi.AccessApiCallResponse.Error) response).getThrowable());
        } else {
            throw new Exception("Unknown response type");
        }

        return connector.waitForSeal(txID);
    }

    public static byte[] loadScript(String name) {
        InputStream resource = ExamplesUtils.class.getClassLoader().getResourceAsStream(name);
        try (resource) {
            if (resource == null) {
                throw new FileNotFoundException("Script file " + name + " not found");
            }
            return resource.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load script " + name, e);
        }
    }

    public static String loadScriptContent(String path) {
        return new String(loadScript(path), StandardCharsets.UTF_8);
    }

    public static byte[] toUnsignedByteArray(byte[] byteArray) {
        byte[] result = new byte[byteArray.length];
        for (int i = 0; i < byteArray.length; i++) {
            result[i] = (byte) (byteArray[i] & 0xFF);
        }
        return result;
    }

    public static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
