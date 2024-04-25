package org.onflow.flow.sdk.test

import org.onflow.flow.sdk.*
import org.onflow.flow.sdk.cadence.StringField
import org.onflow.flow.sdk.crypto.Crypto
import org.onflow.flow.sdk.crypto.KeyPair
import org.onflow.flow.sdk.impl.AsyncFlowAccessApiImpl
import org.onflow.flow.sdk.impl.FlowAccessApiImpl
import org.apiguardian.api.API
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.lang.annotation.Inherited
import java.lang.reflect.Field
import java.math.BigDecimal
import java.net.InetAddress
import java.net.ServerSocket
import java.util.concurrent.TimeUnit

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Inherited
@API(status = API.Status.STABLE, since = "5.0")
annotation class FlowTestClient

@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Inherited
@API(status = API.Status.STABLE, since = "5.0")
annotation class FlowEmulatorCommand(
    val value: String = "flow",
    val expectedExitValue: Int = 0,
    val throwOnError: Boolean = true,
    val timeout: Long = 10,
    val unit: TimeUnit = TimeUnit.SECONDS
)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Inherited
@API(status = API.Status.STABLE, since = "5.0")
annotation class FlowServiceAccountCredentials

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Inherited
@API(status = API.Status.STABLE, since = "5.0")
annotation class FlowTestAccount(
    val signAlgo: SignatureAlgorithm = SignatureAlgorithm.ECDSA_SECP256k1,
    val hashAlgo: HashAlgorithm = HashAlgorithm.SHA3_256,
    val publicKey: String = "",
    val privateKey: String = "",
    val balance: Double = 0.01,
    val contracts: Array<FlowTestContractDeployment> = []
)

@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Inherited
@API(status = API.Status.STABLE, since = "5.0")
annotation class FlowTestContractDeployment(
    val name: String,
    val alias: String = "",
    val addToRegistry: Boolean = true,
    val code: String = "",
    val codeClasspathLocation: String = "",
    val codeFileLocation: String = "",
    val gasLimit: Int = 1000,
    val arguments: Array<TestContractArg> = []
)

@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Inherited
@API(status = API.Status.STABLE, since = "5.0")
annotation class TestContractArg(
    val name: String,
    val value: String
)

data class TestContractDeployment(
    val name: String,
    val code: String,
    val args: Map<String, org.onflow.flow.sdk.cadence.Field<*>>
) {
    companion object {
        fun from(name: String, code: () -> InputStream, args: Map<String, org.onflow.flow.sdk.cadence.Field<*>> = mapOf()): TestContractDeployment {
            return TestContractDeployment(
                name = name,
                code = code().use { String(it.readAllBytes()) },
                args = args
            )
        }
    }
}

data class TestAccount(
    val address: String,
    val privateKey: String,
    val publicKey: String,
    val signAlgo: SignatureAlgorithm,
    val hashAlgo: HashAlgorithm,
    val keyIndex: Int,
    val balance: BigDecimal
) {
    val signer: Signer
        get() = Crypto.getSigner(
            privateKey = Crypto.decodePrivateKey(privateKey, signAlgo),
            hashAlgo = hashAlgo
        )

    val flowAddress: FlowAddress get() = FlowAddress(address)

    val isValid: Boolean get() = address.isNotEmpty()
        && privateKey.isNotEmpty()
        && publicKey.isNotEmpty()
        && signAlgo != SignatureAlgorithm.UNKNOWN
        && hashAlgo != HashAlgorithm.UNKNOWN
        && keyIndex >= 0
}

data class Emulator(
    val process: Process,
    val pidFile: File,
    val host: String,
    val port: Int,
    val restPort: Int,
    val adminPort: Int,
    val serviceAccount: TestAccount
)

abstract class AbstractFlowEmulatorExtension : BeforeEachCallback, AfterEachCallback, TestExecutionExceptionHandler {
    private var process: Process? = null
    private var pidFile: File? = null
    private var accessApi: FlowAccessApiImpl? = null
    private var asyncAccessApi: AsyncFlowAccessApiImpl? = null

    protected abstract fun launchEmulator(context: ExtensionContext): Emulator

    private fun <T : Annotation> withAnnotatedTestFields(context: ExtensionContext, clazz: Class<T>, block: (Any, Field, T) -> Unit) {
        val tests = (
            context.testInstances.map { it.allInstances.toSet() }.orElseGet { emptySet() }
                + context.testInstance.map { setOf(it) }.orElseGet { emptySet() }
        )

        tests.map { it to it.javaClass.fields }
            .flatMap { it.second.map { f -> it.first to f } }
            .filter { it.second.isAnnotationPresent(clazz) }
            .map { block(it.first, it.second, it.second.getAnnotation(clazz)) }
    }

    override fun beforeEach(context: ExtensionContext) {
        Flow.configureDefaults(chainId = FlowChainId.EMULATOR)
        Flow.DEFAULT_ADDRESS_REGISTRY.defaultChainId = FlowChainId.EMULATOR

        val emulator = launchEmulator(context)
        this.process = emulator.process
        this.pidFile = emulator.pidFile

        this.accessApi = Flow.newAccessApi(
            host = emulator.host,
            port = emulator.port
        ) as FlowAccessApiImpl

        this.asyncAccessApi = Flow.newAsyncAccessApi(
            host = emulator.host,
            port = emulator.port
        ) as AsyncFlowAccessApiImpl

        withAnnotatedTestFields(context, FlowTestClient::class.java) { instance, field, _ ->
            if (field.type.equals(FlowAccessApi::class.java)) {
                field.isAccessible = true
                field.set(instance, this.accessApi!!)
            } else if (field.type.equals(AsyncFlowAccessApi::class.java)) {
                field.isAccessible = true
                field.set(instance, asyncAccessApi)
            } else {
                throw IllegalArgumentException(
                    "field $field is not of type FlowAccessApi or AsyncFlowAccessAPi"
                )
            }
        }

        withAnnotatedTestFields(context, FlowServiceAccountCredentials::class.java) { instance, field, _ ->
            if (!field.type.equals(TestAccount::class.java)) {
                throw IllegalArgumentException("field $field is not of type TestAccount")
            } else if (!emulator.serviceAccount.isValid) {
                throw IllegalArgumentException(
                    "FLOW Service account configuration is not valid"
                )
            }
            field.isAccessible = true
            field.set(instance, emulator.serviceAccount)
        }

        withAnnotatedTestFields(context, FlowTestAccount::class.java) { instance, field, annotation ->
            if (!field.type.equals(TestAccount::class.java)) {
                throw IllegalArgumentException("field $field is not of type TestAccount")
            } else if (!emulator.serviceAccount.isValid) {
                throw IllegalArgumentException(
                    "FLOW Service account configuration is not valid, cannot create a FlowTestAccount"
                )
            }

            val keyPair = if (annotation.privateKey.isEmpty() && annotation.publicKey.isEmpty()) {
                Crypto.generateKeyPair(annotation.signAlgo)
            } else {
                KeyPair(
                    private = Crypto.decodePrivateKey(annotation.privateKey, annotation.signAlgo),
                    public = Crypto.decodePublicKey(annotation.publicKey, annotation.signAlgo)
                )
            }

            val address = FlowTestUtil.createAccount(
                api = this.accessApi!!,
                serviceAccount = emulator.serviceAccount,
                publicKey = keyPair.public.hex,
                signAlgo = annotation.signAlgo,
                hashAlgo = annotation.hashAlgo,
                balance = BigDecimal(annotation.balance)
            )

            val testAccount = TestAccount(
                address = address.formatted,
                privateKey = keyPair.private.hex,
                publicKey = keyPair.public.hex,
                signAlgo = annotation.signAlgo,
                hashAlgo = annotation.hashAlgo,
                keyIndex = 0,
                balance = BigDecimal(annotation.balance)
            )

            field.isAccessible = true
            field.set(instance, testAccount)

            // deploy contracts
            for (deployable in annotation.contracts) {
                FlowTestUtil.deployContracts(
                    api = this.accessApi!!,
                    account = testAccount,
                    gasLimit = deployable.gasLimit,
                    TestContractDeployment.from(
                        name = deployable.name,
                        code = {
                            if (deployable.codeFileLocation.isNotEmpty()) {
                                File(deployable.codeFileLocation).inputStream()
                            } else if (deployable.codeClasspathLocation.isNotEmpty()) {
                                instance.javaClass.getResourceAsStream(deployable.codeClasspathLocation)
                            } else {
                                ByteArrayInputStream(deployable.code.toByteArray())
                            }
                        },
                        args = deployable.arguments.associate { it.name to StringField(it.value) }
                    )
                ).sendAndWaitForSeal()
                    .throwOnError()

                if (deployable.addToRegistry) {
                    val alias = deployable.alias.ifEmpty { "0x${deployable.name.uppercase()}" }
                    Flow.DEFAULT_ADDRESS_REGISTRY.register(alias, testAccount.flowAddress, FlowChainId.EMULATOR)
                }
            }
        }

        Runtime.getRuntime().addShutdownHook(
            Thread(this::shutdownEmulator)
        )
    }

    override fun afterEach(context: ExtensionContext) = shutdownEmulator()

    override fun handleTestExecutionException(context: ExtensionContext, throwable: Throwable) {
        try {
            shutdownEmulator()
        } finally {
            throw throwable
        }
    }

    private fun shutdownEmulator() {
        val api = this.accessApi
        if (api != null) {
            api.close()
            this.accessApi = null
        }
        val asyncApi = asyncAccessApi
        if (asyncApi != null) {
            asyncApi.close()
            this.asyncAccessApi = null
        }
        val proc = process
        if (proc != null) {
            proc.destroy()
            var count = 0
            while (!proc.waitFor(1, TimeUnit.SECONDS)) {
                proc.destroyForcibly()
                count++
                if (count >= 60) {
                    throw IllegalStateException("Unable to terminate flow emulator process")
                }
            }
        }
        if (pidFile != null) {
            pidFile?.delete()
            pidFile = null
        }
        process = null
    }

    protected fun findFreePort(host: String): Int {
        return ServerSocket(0, 50, InetAddress.getByName(host)).use { it.localPort }
    }
}
