package org.onflow.flow.common.test

import org.onflow.flow.sdk.HashAlgorithm
import org.onflow.flow.sdk.SignatureAlgorithm
import org.onflow.flow.sdk.crypto.Crypto
import org.apiguardian.api.API
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import java.lang.annotation.Inherited
import java.math.BigDecimal

/**
 * Annotates a test that doesn't reply on a flow.json project configuration.
 */
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION
)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Inherited
@ExtendWith(FlowEmulatorTestExtension::class)
@API(status = API.Status.STABLE, since = "5.0")
annotation class FlowEmulatorTest(
    val executable: String = "flow-c1",
    val host: String = "localhost",
    val postStartCommands: Array<FlowEmulatorCommand> = [],
    val pidFilename: String = "flow-emulator.pid",
    val signAlgo: SignatureAlgorithm = SignatureAlgorithm.ECDSA_P256,
    val hashAlgo: HashAlgorithm = HashAlgorithm.SHA3_256
)

class FlowEmulatorTestExtension : AbstractFlowEmulatorExtension() {
    override fun launchEmulator(context: ExtensionContext): Emulator {
        if (!context.requiredTestClass.isAnnotationPresent(FlowEmulatorTest::class.java)) {
            throw IllegalStateException("FlowEmulatorTestFlowEmulatorTest annotation not found")
        }

        val config = context.requiredTestClass.getAnnotation(FlowEmulatorTest::class.java)
        val port = findFreePort(config.host)
        val restPort = findFreePort(config.host)
        val adminPort = findFreePort(config.host)
        val serviceKeyPair = Crypto.generateKeyPair(config.signAlgo)
        val serviceAccount = TestAccount(
            address = "0xf8d6e0586b0a20c7", // TODO: is this a safe assumption?
            privateKey = serviceKeyPair.private.hex,
            publicKey = serviceKeyPair.public.hex,
            signAlgo = config.signAlgo,
            hashAlgo = config.hashAlgo,
            keyIndex = 0,
            balance = BigDecimal(-1)
        )

        val args = """
            --verbose --grpc-debug 
            --service-priv-key=${serviceKeyPair.private.hex.replace(Regex("^(00)+"), "")}
            --service-sig-algo=${config.signAlgo.name.uppercase()}
            --service-hash-algo=${config.hashAlgo.name.uppercase()}
        """.trimIndent()
            .replace("\n", " ")

        val ret = FlowTestUtil.runFlow(
            executable = config.executable,
            arguments = args,
            host = config.host,
            port = port,
            restPort = restPort,
            adminPort = adminPort,
            postStartCommands = config.postStartCommands,
            flowJsonLocation = null,
            pidFilename = config.pidFilename
        )

        return Emulator(
            process = ret.first,
            pidFile = ret.second,
            host = config.host,
            port = port,
            restPort = restPort,
            adminPort = adminPort,
            serviceAccount = serviceAccount
        )
    }
}
