package org.onflow.examples.java;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ExamplesUtils {

    public static byte[] loadScript(String name) {
        InputStream resource = ExamplesUtils.class.getClassLoader().getResourceAsStream(name);
        if (resource == null) {
            throw new IllegalArgumentException("Script file " + name + " not found");
        }
        try {
            return resource.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load script " + name, e);
        } finally {
            try {
                resource.close();
            } catch (IOException e) {
                // Log or handle closing exception if needed
            }
        }
    }

    public static String loadScriptContent(String path) {
        return new String(loadScript(path), StandardCharsets.UTF_8);
    }
}

