package org.onflow.flow.common.test

import org.onflow.flow.sdk.HashAlgorithm
import org.onflow.flow.sdk.SignatureAlgorithm
import org.apiguardian.api.API
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.onflow.flow.sdk.crypto.Crypto
import java.lang.annotation.Inherited
import java.math.BigDecimal

/**
 * Annotates a test that uses a flow.json project configuration
 */
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION
)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Inherited
@ExtendWith(FlowEmulatorProjectTestExtension::class)
@API(status = API.Status.STABLE, since = "5.0")
annotation class FlowEmulatorProjectTest(
    val executable: String = "flow-c1",
    val arguments: String = "--log debug --verbose",
    val host: String = "localhost",
    val port: Int = -1,
    val restPort: Int = -1,
    val adminPort: Int = -1,
    val postStartCommands: Array<FlowEmulatorCommand> = [],
    /**
     * Location of flow.json, can also be in the classpath or
     * a directory containing flow.json.
     */
    val flowJsonLocation: String = "flow.json",
    val pidFilename: String = "flow-emulator-project.pid",
    val serviceAccountAddress: String = "",
    val serviceAccountPublicKey: String = "",
    val serviceAccountPrivateKey: String = "",
    val serviceAccountSignAlgo: SignatureAlgorithm = SignatureAlgorithm.UNKNOWN,
    val serviceAccountHashAlgo: HashAlgorithm = HashAlgorithm.UNKNOWN,
    val serviceAccountKeyIndex: Int = -1
)

class FlowEmulatorProjectTestExtension : AbstractFlowEmulatorExtension() {
    override fun launchEmulator(context: ExtensionContext): Emulator {
        if (!context.requiredTestClass.isAnnotationPresent(FlowEmulatorProjectTest::class.java)) {
            throw IllegalStateException("FlowEmulatorProjectTest annotation not found")
        }
        val config = context.requiredTestClass.getAnnotation(FlowEmulatorProjectTest::class.java)
        val port = config.port.takeUnless { it < 0 } ?: findFreePort("localhost")
        val restPort = config.restPort.takeUnless { it < 0 } ?: findFreePort("localhost")
        val adminPort = config.adminPort.takeUnless { it < 0 } ?: findFreePort("localhost")

        val serviceAccount: TestAccount
        val args: String

        if (config.serviceAccountAddress.isNotEmpty() &&
            config.serviceAccountPublicKey.isNotEmpty() &&
            config.serviceAccountPrivateKey.isNotEmpty()
        ) {
            serviceAccount = TestAccount(
                address = config.serviceAccountAddress,
                privateKey = config.serviceAccountPrivateKey,
                publicKey = config.serviceAccountPublicKey,
                signAlgo = config.serviceAccountSignAlgo,
                hashAlgo = config.serviceAccountHashAlgo,
                keyIndex = config.serviceAccountKeyIndex,
                balance = BigDecimal(-1)
            )
            args = config.arguments
        } else {
            val serviceKeyPair = Crypto.generateKeyPair(SignatureAlgorithm.ECDSA_P256)
            serviceAccount = TestAccount(
                address = "0xf8d6e0586b0a20c7",
                privateKey = serviceKeyPair.private.hex,
                publicKey = serviceKeyPair.public.hex,
                signAlgo = SignatureAlgorithm.ECDSA_P256,
                hashAlgo = HashAlgorithm.SHA3_256,
                keyIndex = 0,
                balance = BigDecimal(-1)
            )

            args = """
                --verbose --grpc-debug 
                --service-priv-key=${serviceKeyPair.private.hex.replace(Regex("^(00)+"), "")}
                --service-sig-algo=${SignatureAlgorithm.ECDSA_P256.name.uppercase()}
                --service-hash-algo=${HashAlgorithm.SHA3_256.name.uppercase()}
            """.trimIndent().replace("\n", " ")
        }

        val ret = FlowTestUtil.runFlow(
            executable = config.executable,
            arguments = args.trim().takeIf { it.isNotEmpty() },
            host = config.host,
            port = port,
            restPort = restPort,
            adminPort = adminPort,
            postStartCommands = config.postStartCommands,
            flowJsonLocation = config.flowJsonLocation.trim().takeIf { it.isNotEmpty() },
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
