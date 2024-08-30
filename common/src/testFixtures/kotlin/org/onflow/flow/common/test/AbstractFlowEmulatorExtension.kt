package org.onflow.flow.common.test

import io.grpc.ManagedChannel
import org.onflow.flow.sdk.*
import org.onflow.flow.sdk.cadence.StringField
import org.onflow.flow.sdk.crypto.Crypto
import org.onflow.flow.sdk.crypto.KeyPair
import org.onflow.flow.sdk.impl.AsyncFlowAccessApiImpl
import org.onflow.flow.sdk.impl.FlowAccessApiImpl
import org.apiguardian.api.API
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler
import org.onflow.flow.sdk.crypto.PrivateKey
import org.onflow.flow.sdk.crypto.PublicKey
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
    val value: String = "flow-c1",
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
    val balance: Double = 1000.01,
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
        fun from(name: String, code: () -> InputStream, args: Map<String, org.onflow.flow.sdk.cadence.Field<*>> = mapOf()): TestContractDeployment = TestContractDeployment(
            name = name,
            code = code().use { String(it.readAllBytes()) },
            args = args
        )
    }
}

data class TestAccount(
    val address: String,
    val privateKey: PrivateKey,
    val publicKey: PublicKey,
    val hashAlgo: HashAlgorithm,
    val keyIndex: Int,
    val balance: BigDecimal
) {
    val signer: Signer
        get() = Crypto.getSigner(
            privateKey = privateKey,
            hashAlgo = hashAlgo
        )

    val flowAddress: FlowAddress get() = FlowAddress(address)

    val isValid: Boolean get() = address.isNotEmpty()
        &&
        privateKey.hex.isNotEmpty()
        &&
        publicKey.hex.isNotEmpty()
        &&
        privateKey.algo != SignatureAlgorithm.UNKNOWN
        &&
        hashAlgo != HashAlgorithm.UNKNOWN
        &&
        keyIndex >= 0
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

abstract class AbstractFlowEmulatorExtension :
    BeforeEachCallback,
    TestExecutionExceptionHandler {
    private var process: Process? = null
    private var pidFile: File? = null
    private var accessApi: FlowAccessApiImpl? = null
    private var asyncAccessApi: AsyncFlowAccessApiImpl? = null
    private val channels = mutableListOf<ManagedChannel>()

    protected abstract fun launchEmulator(context: ExtensionContext): Emulator

    private fun getManagedChannel(api: Any): ManagedChannel? = try {
        val field: Field = api.javaClass.getDeclaredField("api")
        field.isAccessible = true
        val stub = field.get(api) as io.grpc.stub.AbstractStub<*>
        stub.channel as? ManagedChannel
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    private fun createAccessApi(host: String, port: Int): FlowAccessApiImpl {
        val api = Flow.newAccessApi(host, port) as FlowAccessApiImpl
        getManagedChannel(api)?.let { channels.add(it) }
        return api
    }

    private fun createAsyncAccessApi(host: String, port: Int): AsyncFlowAccessApiImpl {
        val api = Flow.newAsyncAccessApi(host, port) as AsyncFlowAccessApiImpl
        getManagedChannel(api)?.let { channels.add(it) }
        return api
    }

    private fun <T : Annotation> withAnnotatedTestFields(context: ExtensionContext, clazz: Class<T>, block: (Any, Field, T) -> Unit) {
        val tests = (
            context.testInstances.map { it.allInstances.toSet() }.orElseGet { emptySet() }
                + context.testInstance.map { setOf(it) }.orElseGet { emptySet() }
        )

        tests
            .map { it to it.javaClass.declaredFields }
            .flatMap { it.second.map { f -> it.first to f } }
            .filter { it.second.isAnnotationPresent(clazz) }
            .forEach { block(it.first, it.second, it.second.getAnnotation(clazz)) }
    }

    override fun beforeEach(context: ExtensionContext) {
        Flow.configureDefaults(chainId = FlowChainId.EMULATOR)
        Flow.DEFAULT_ADDRESS_REGISTRY.defaultChainId = FlowChainId.EMULATOR

        val emulator = launchEmulator(context)
        this.process = emulator.process
        this.pidFile = emulator.pidFile

        // Adding delay to ensure emulator has started
        Thread.sleep(5000) // Wait for 5 seconds

        this.accessApi = createAccessApi(emulator.host, emulator.port)
        this.asyncAccessApi = createAsyncAccessApi(emulator.host, emulator.port)

        withAnnotatedTestFields(context, FlowTestClient::class.java) { instance, field, _ ->
            field.isAccessible = true
            when (field.type) {
                FlowAccessApi::class.java -> field.set(instance, this.accessApi!!)
                AsyncFlowAccessApi::class.java -> field.set(instance, asyncAccessApi)
                else -> throw IllegalArgumentException("field $field is not of type FlowAccessApi or AsyncFlowAccessApi")
            }
        }

        withAnnotatedTestFields(context, FlowServiceAccountCredentials::class.java) { instance, field, _ ->
            field.isAccessible = true
            if (field.type != TestAccount::class.java) {
                throw IllegalArgumentException("field $field is not of type TestAccount")
            } else if (!emulator.serviceAccount.isValid) {
                throw IllegalArgumentException("FLOW Service account configuration is not valid")
            }
            field.set(instance, emulator.serviceAccount)
        }

        withAnnotatedTestFields(context, FlowTestAccount::class.java) { instance, field, annotation ->
            field.isAccessible = true
            if (field.type != TestAccount::class.java) {
                throw IllegalArgumentException("field $field is not of type TestAccount")
            } else if (!emulator.serviceAccount.isValid) {
                throw IllegalArgumentException("FLOW Service account configuration is not valid, cannot create a FlowTestAccount")
            }

            val keyPair = if (annotation.privateKey.isEmpty() && annotation.publicKey.isEmpty()) {
                Crypto.generateKeyPair(annotation.signAlgo)
            } else {
                KeyPair(
                    private = Crypto.decodePrivateKey(annotation.privateKey, annotation.signAlgo),
                    public = Crypto.decodePublicKey(annotation.publicKey, annotation.signAlgo)
                )
            }

            val createAccountResult = FlowTestUtil.createAccount(
                api = this.accessApi!!,
                serviceAccount = emulator.serviceAccount,
                publicKey = keyPair.public.hex,
                signAlgo = annotation.signAlgo,
                hashAlgo = annotation.hashAlgo,
                balance = BigDecimal(annotation.balance)
            )

            val address = when (createAccountResult) {
                is FlowAccessApi.AccessApiCallResponse.Success -> createAccountResult.data
                is FlowAccessApi.AccessApiCallResponse.Error -> throw IllegalStateException("Failed to create account: ${createAccountResult.message}", createAccountResult.throwable)
            }

            val testAccount = TestAccount(
                address = address.formatted,
                privateKey = keyPair.private,
                publicKey = keyPair.public,
                hashAlgo = annotation.hashAlgo,
                keyIndex = 0,
                balance = BigDecimal(annotation.balance)
            )

            field.set(instance, testAccount)

            // deploy contracts
            for (deployable in annotation.contracts) {
                val deployResult = FlowTestUtil
                    .deployContracts(
                        api = this.accessApi!!,
                        account = testAccount,
                        gasLimit = deployable.gasLimit,
                        TestContractDeployment.from(
                            name = deployable.name,
                            code = {
                                when {
                                    deployable.codeFileLocation.isNotEmpty() -> File(deployable.codeFileLocation).inputStream()
                                    deployable.codeClasspathLocation.isNotEmpty() -> instance.javaClass.getResourceAsStream(deployable.codeClasspathLocation)!!
                                    else -> ByteArrayInputStream(deployable.code.toByteArray())
                                }
                            },
                            args = deployable.arguments.associate { it.name to StringField(it.value) }
                        )
                    ).sendAndWaitForSeal()

                when (deployResult) {
                    is FlowAccessApi.AccessApiCallResponse.Success -> {
                        // Contract deployment successful
                    }
                    is FlowAccessApi.AccessApiCallResponse.Error -> {
                        throw IllegalStateException("Failed to deploy contract: ${deployResult.message}", deployResult.throwable)
                    }
                }

                if (deployable.addToRegistry) {
                    val alias = deployable.alias.ifEmpty { "0x${deployable.name.uppercase()}" }
                    Flow.DEFAULT_ADDRESS_REGISTRY.register(alias, testAccount.flowAddress, FlowChainId.EMULATOR)
                }
            }
        }

        Runtime.getRuntime().addShutdownHook(Thread(this::shutdownEmulator))
    }

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

        channels.forEach { it.shutdown().awaitTermination(5, TimeUnit.SECONDS) }
    }

    protected fun findFreePort(host: String): Int = ServerSocket(0, 50, InetAddress.getByName(host)).use { it.localPort }
}
