package org.onflow.examples.java;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ExamplesUtils {

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

    public static String loadScriptContent(String path) throws FileNotFoundException {
        return new String(loadScript(path), StandardCharsets.UTF_8);
    }
}
