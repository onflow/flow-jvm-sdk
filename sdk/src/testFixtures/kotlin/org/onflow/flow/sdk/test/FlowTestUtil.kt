package org.onflow.flow.sdk.test

import org.onflow.flow.sdk.*
import org.onflow.flow.sdk.cadence.AddressField
import org.onflow.flow.sdk.cadence.EventField
import org.onflow.flow.sdk.impl.FlowAccessApiImpl
import java.io.File
import java.io.IOException
import java.math.BigDecimal
import java.nio.charset.StandardCharsets
import kotlin.io.path.createTempDirectory

object FlowTestUtil {
    private fun loadScript(name: String): ByteArray {
        val resource = javaClass.classLoader.getResourceAsStream(name)
            ?: throw IllegalArgumentException("Script file $name not found")
        return resource.use { it.readAllBytes() }
    }

    @JvmStatic
    @JvmOverloads
    fun deployContracts(
        api: FlowAccessApi,
        account: TestAccount,
        gasLimit: Int = 1000,
        vararg contracts: TestContractDeployment
    ): FlowTransactionStub {
        val contractList = contracts.toList()
        val contractArgs = contractList
            .mapIndexed { i, c ->
                c.args.entries.sortedBy { it.key }
                    .joinToString(separator = "") { ", contract${i}_${it.key}: ${it.value.type}" }
            }
            .joinToString(separator = "")
        val contractAddArgs = contractList
            .mapIndexed { i, c ->
                c.args.entries.sortedBy { it.key }
                    .joinToString(separator = "") { ", ${it.key}: contract${i}_${it.key}" }
            }
            .toList()
        val contractAdds = List(contractList.size) { i ->
            """
                    signer.contracts.add(
                        name: names[$i], code: codes[$i].utf8${contractAddArgs[i]}
                    )
                """
        }.joinToString(separator = "")

        return api.simpleFlowTransaction(
            address = account.flowAddress,
            signer = account.signer,
            keyIndex = account.keyIndex
        ) {
            script {
                """
                    transaction(names: [String], codes: [String]$contractArgs) {
                        prepare(signer: AuthAccount) {
                            $contractAdds
                        }
                    }
                """
            }
            gasLimit(gasLimit)
            arguments {
                arg { array(contractList) { string(it.name) } }
                arg { array(contractList) { string(addressRegistry.processScript(it.code)) } }
                contractList
                    .map { c -> c.args.entries.sortedBy { it.key } }
                    .flatten()
                    .map { it.value }
                    .forEach { arg { it } }
            }
        }
    }

    @JvmStatic
    @JvmOverloads
    fun createAccount(
        api: FlowAccessApi,
        serviceAccount: TestAccount,
        publicKey: String,
        signAlgo: SignatureAlgorithm,
        hashAlgo: HashAlgorithm,
        balance: BigDecimal = BigDecimal(0.01)
    ): FlowAccessApi.AccessApiCallResponse<FlowAddress> {
        val loadedScript = String(loadScript("cadence/test_utils_create_account.cdc"), StandardCharsets.UTF_8)
        val transactionResult = api.simpleFlowTransaction(
            address = serviceAccount.flowAddress,
            signer = serviceAccount.signer,
            keyIndex = serviceAccount.keyIndex
        ) {
            script {
                loadedScript
            }
            gasLimit(1000)
            arguments {
                arg { ufix64(balance) }
                arg { string(publicKey) }
                arg { uint8(signAlgo.index) }
                arg { uint8(hashAlgo.index) }
                arg { serviceAccount.signer }
            }
        }.sendAndWaitForSeal()

        return when (transactionResult) {
            is FlowAccessApi.AccessApiCallResponse.Success -> {
                val result = transactionResult.data
                val address = result.events
                    .find { it.type == "flow.AccountCreated" }
                    ?.payload
                    ?.let { (it.jsonCadence as EventField).value }
                    ?.getRequiredField<AddressField>("address")
                    ?.value
                    ?: return FlowAccessApi.AccessApiCallResponse.Error("Couldn't find AccountCreated event with address for account that was created")

                FlowAccessApi.AccessApiCallResponse.Success(FlowAddress(address))
            }
            is FlowAccessApi.AccessApiCallResponse.Error -> FlowAccessApi.AccessApiCallResponse.Error("Failed to create account: ${transactionResult.message}", transactionResult.throwable)
        }
    }

    @JvmStatic
    @JvmOverloads
    fun runFlow(
        executable: String = "flow-c1",
        arguments: String? = null,
        host: String = "localhost",
        port: Int = 3570,
        restPort: Int = 8889,
        adminPort: Int = 8081,
        flowJsonLocation: String? = null,
        postStartCommands: Array<FlowEmulatorCommand> = emptyArray(),
        classLoader: ClassLoader = AbstractFlowEmulatorExtension::class.java.classLoader,
        pidFilename: String = "flow-emulator.pid"
    ): Pair<Process, File> {
        var flowJson: String?

        val pidFile = File(System.getProperty("java.io.tmpdir"), pidFilename)
        if (pidFile.exists()) {
            // TODO: maybe a better way of doing this?
            // we only have to do this because sometimes the process
            // is left alive and there doesn't seem to be a way to
            // stop it remotely by connecting to it and issuing a
            // shutdown command or similar. The only other thing I
            // can think of is to start the emulator on a random port
            // and inject it into the test, but then we may leave
            // a bunch of rogue emulators running on the client machine.
            // The only time a rogue emulator is left running is when
            // the JVM is forcibly killed before the shutdownEmulator
            // method is called on the FlowEmulatorExtension, this seems to
            // only happen when debugging unit tests in the IDE.
            try {
                val pid = String(pidFile.readBytes()).toLong()
                ProcessHandle.of(pid).ifPresent { it.destroyForcibly() }
            } catch (e: Throwable) {
                println("Error forcibly killing a zombie emulator process, tests may fail")
            }
        }
        pidFile.delete()

        // is it a file?
        flowJson = flowJsonLocation?.let(::File)
            ?.takeIf { it.exists() }
            ?.takeIf { it.isFile }
            ?.absolutePath

        // is it in the classpath?
        if (flowJson == null) {
            flowJson = flowJsonLocation?.let(classLoader::getResource)
                ?.openStream()
                ?.use { input ->
                    val tmp = File.createTempFile("flow", ".json")
                    tmp.deleteOnExit()
                    tmp.outputStream().use { output -> input.copyTo(output) }
                    tmp
                }
                ?.absolutePath
        }

        // is it a directory with a flow.json file
        if (flowJson == null) {
            flowJson = flowJsonLocation?.let(::File)
                ?.takeIf { it.exists() }
                ?.takeIf { it.isDirectory }
                ?.let { File(it, "flow.json") }
                ?.takeIf { it.exists() }
                ?.absolutePath
        }

        var workingDirectory: File? = null

        val configFile = if (flowJson != null) {
            "--config-path $flowJson"
        } else {
            workingDirectory = createTempDirectory("flow-emulator").toFile()
            "--init"
        }

        val cmd = if (File(executable).exists() && File(executable).isFile && File(executable).canExecute()) {
            executable
        } else {
            (
                listOf("${System.getProperty("user.home")}/.local/bin", "/usr/local/bin", "/usr/bin", "/bin")
                    + (System.getenv()["PATH"]?.split(File.pathSeparator) ?: emptyList())
            )
                .map { File(it, "flow") }
                .find { it.exists() }
                ?: throw IOException("flow command not found")
        }

        val emulatorCommand = "$cmd emulator $arguments --port $port --rest-port $restPort --admin-port $adminPort $configFile"

        val start = System.currentTimeMillis()
        var proc = ProcessBuilder()
            .command(emulatorCommand.split(" "))
            .inheritIO()
        if (workingDirectory != null) {
            proc = proc.directory(workingDirectory)
        }
        val ret = proc.start()

        if (ret.isAlive) {
            pidFile.writeBytes(ret.pid().toString().toByteArray())
        }

        val api = Flow.newAccessApi(host = host, port = port) as FlowAccessApiImpl
        while (true) {
            val elapsed = System.currentTimeMillis() - start
            try {
                api.ping()
                break
            } catch (t: Throwable) {
                if (elapsed > 25_000) {
                    throw IllegalStateException("Unable to find flow process that was started after 25 seconds")
                }
            }
            if (!ret.isAlive) {
                throw IllegalStateException("Flow process died after $elapsed milliseconds")
            }
        }
        api.close()

        // run commands
        for (postStartCommand in postStartCommands) {
            val exec = "$cmd ${postStartCommand.value} -n emulator $configFile"
            val process = ProcessBuilder()
                .command(exec.split(" "))
                .inheritIO()
                .start()
            if (!process.waitFor(postStartCommand.timeout, postStartCommand.unit) && postStartCommand.throwOnError) {
                throw IllegalStateException("Waiting for command failed; $exec")
            } else if (process.exitValue() != postStartCommand.expectedExitValue && postStartCommand.throwOnError) {
                throw IllegalStateException("Expected exit value ${postStartCommand.expectedExitValue} but got ${process.exitValue()} for command: $exec")
            }
        }

        // we're g2g
        return ret to pidFile
    }
}
