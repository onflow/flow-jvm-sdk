package org.onflow.examples.java;

import org.jetbrains.annotations.NotNull;
import org.onflow.flow.sdk.FlowAccessApi;
import org.onflow.flow.sdk.FlowId;
import org.onflow.flow.sdk.FlowTransaction;
import org.onflow.flow.sdk.FlowTransactionResult;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

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
}
