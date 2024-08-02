package org.onflow.flow.sdk.cadence

import kotlinx.serialization.Serializable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.onflow.flow.common.test.*
import org.onflow.flow.sdk.*
import org.onflow.flow.sdk.IntegrationTestUtils.createAndSubmitAccountCreationTransaction
import java.nio.charset.StandardCharsets

@FlowEmulatorTest
class JsonCadenceTest {
    @FlowTestClient
    lateinit var flow: FlowAccessApi

    @FlowServiceAccountCredentials
    lateinit var serviceAccount: TestAccount

    @Serializable
    data class StorageInfo(
        val capacity: Int,
        val used: Int,
        val available: Int
    )

    @Serializable
    data class StorageInfoComplex(
        val capacity: ULong,
        val used: ULong,
        val available: ULong,
        val foo: Foo
    )

    @Serializable
    data class Foo(
        val bar: Int,
    )

    @Serializable
    data class SomeResource(
        val uuid: Long,
        val value: Int
    )

    @Serializable
    data class SomeEnum(
        val rawValue: Int
    )

    private fun loadScriptContent(path: String): String {
        return String(FlowTestUtil.loadScript(path), StandardCharsets.UTF_8)
    }

    private fun executeScript(scriptPath: String): FlowAccessApi.AccessApiCallResponse<FlowScriptResponse> {
        val loadedScript = loadScriptContent(scriptPath)
        return flow.simpleFlowScript {
            script { loadedScript }
        }
    }

    @Test
    fun `Can parse JSON Cadence from transaction`() {
        val txResult = createAndSubmitAccountCreationTransaction(
            flow,
            serviceAccount,
            "cadence/transaction_creation/transaction_creation_simple_transaction.cdc"
        )
        println(txResult)
        assertThat(txResult).isNotNull
        assertThat(txResult.status).isEqualTo(FlowTransactionStatus.SEALED)

        val events = txResult.events.map { it.payload.jsonCadence }
        assertThat(events).hasSize(7)
    }

    @Test
    fun decodeOptional() {
        val data = when (val result = executeScript("cadence/json_cadence/decode_optional.cdc")) {
            is FlowAccessApi.AccessApiCallResponse.Success -> result.data.jsonCadence.decode<Boolean?>()
            is FlowAccessApi.AccessApiCallResponse.Error -> throw IllegalStateException("Failed to execute script: ${result.message}", result.throwable)
        }

        assertThat(data).isEqualTo(null)
    }

    @Test
    fun decodeOptional2() {
        val data = when (val result = executeScript("cadence/json_cadence/decode_optional_2.cdc")) {
            is FlowAccessApi.AccessApiCallResponse.Success -> result.data.jsonCadence.decode<Boolean?>()
            is FlowAccessApi.AccessApiCallResponse.Error -> throw IllegalStateException("Failed to execute script: ${result.message}", result.throwable)
        }

        assertThat(data).isEqualTo(true)
    }

    @Test
    fun decodeBoolean() {
        val data = when (val result = executeScript("cadence/json_cadence/decode_boolean.cdc")) {
            is FlowAccessApi.AccessApiCallResponse.Success -> result.data.jsonCadence.decode<Boolean?>()
            is FlowAccessApi.AccessApiCallResponse.Error -> throw IllegalStateException("Failed to execute script: ${result.message}", result.throwable)
        }

        assertThat(data).isEqualTo(true)
    }

    @Test
    fun decodeArray() {
        val data = when (val result = executeScript("cadence/json_cadence/decode_array.cdc")) {
            is FlowAccessApi.AccessApiCallResponse.Success -> result.data.jsonCadence.decode<List<ULong>>()
            is FlowAccessApi.AccessApiCallResponse.Error -> throw IllegalStateException("Failed to execute script: ${result.message}", result.throwable)
        }

        assertThat(data.first()).isEqualTo(1UL)
        assertThat(data).hasSize(4)
    }

    @Test
    fun decodeUFix64() {
        val data = when (val result = executeScript("cadence/json_cadence/decode_ufix64.cdc")) {
            is FlowAccessApi.AccessApiCallResponse.Success -> result.data.jsonCadence.decode<Double>()
            is FlowAccessApi.AccessApiCallResponse.Error -> throw IllegalStateException("Failed to execute script: ${result.message}", result.throwable)
        }

        assertThat(data).isEqualTo(0.789111)
    }

    @Test
    fun decodeStruct() {
        val loadedScript = loadScriptContent("cadence/json_cadence/decode_struct.cdc")
        val result = flow.simpleFlowScript {
            script { loadedScript }
            arg { address("0x84221fe0294044d7") }
        }

        val data = when (result) {
            is FlowAccessApi.AccessApiCallResponse.Success -> result.data.jsonCadence.decode<List<StorageInfo>>().first()
            is FlowAccessApi.AccessApiCallResponse.Error -> throw IllegalStateException("Failed to execute script: ${result.message}", result.throwable)
        }

        assertThat(data.capacity).isEqualTo(1)
        assertThat(data.used).isEqualTo(2)
        assertThat(data.available).isEqualTo(3)
    }

    @Test
    fun decodeComplexDict() {
        val loadedScript = loadScriptContent("cadence/json_cadence/decode_complex_dict.cdc")
        val result = flow.simpleFlowScript {
            script { loadedScript }
            arg { address(serviceAccount.address) }
        }

        val data = when (result) {
            is FlowAccessApi.AccessApiCallResponse.Success -> result.data.jsonCadence.decode<Map<String, List<StorageInfoComplex>>>()
            is FlowAccessApi.AccessApiCallResponse.Error -> throw IllegalStateException("Failed to execute script: ${result.message}", result.throwable)
        }

        assertThat(data["test"]!!.first().foo.bar).isEqualTo(1)
    }

    @Test
    fun decodeResource() {
        val decodedResource = when (val result = executeScript("cadence/json_cadence/decode_resource.cdc")) {
            is FlowAccessApi.AccessApiCallResponse.Success -> result.data.jsonCadence.decode<SomeResource>()
            is FlowAccessApi.AccessApiCallResponse.Error -> throw IllegalStateException("Failed to execute script: ${result.message}", result.throwable)
        }

        assertThat(decodedResource).isNotNull()
        assertThat(decodedResource.value).isEqualTo(20)
    }

    @Test
    fun decodeEnum() {
        val decodedEnum = when (val result = executeScript("cadence/json_cadence/decode_enum.cdc")) {
            is FlowAccessApi.AccessApiCallResponse.Success -> result.data.jsonCadence.decode<SomeEnum>()
            is FlowAccessApi.AccessApiCallResponse.Error -> throw IllegalStateException("Failed to execute script: ${result.message}", result.throwable)
        }

        assertThat(decodedEnum).isNotNull()
        assertThat(decodedEnum.rawValue).isEqualTo(0)
    }

    @Test
    fun decodeReference() {
        val decodedReference = when (val result = executeScript("cadence/json_cadence/decode_reference.cdc")) {
            is FlowAccessApi.AccessApiCallResponse.Success -> result.data.jsonCadence.decodeToAny()
            is FlowAccessApi.AccessApiCallResponse.Error -> throw IllegalStateException("Failed to execute script: ${result.message}", result.throwable)
        }

        assertThat(decodedReference).isNotNull()
        assertThat(decodedReference).isEqualTo("Hello")
    }
}
