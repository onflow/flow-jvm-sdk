package org.onflow.examples.kotlin

import java.io.FileNotFoundException
import java.nio.charset.StandardCharsets

object ExamplesUtils {
    fun loadScript(name: String): ByteArray {
        val resource = javaClass.classLoader.getResourceAsStream(name)
            ?: throw FileNotFoundException("Script file $name not found")
        return resource.use { it.readAllBytes() }
    }

    fun loadScriptContent(path: String): String {
        return String(loadScript(path), StandardCharsets.UTF_8)
    }

    fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }
}
