package org.onflow.flow.sdk

import org.onflow.flow.sdk.cadence.*
import org.onflow.flow.sdk.crypto.Crypto
import org.onflow.flow.common.test.FlowEmulatorProjectTest
import org.onflow.flow.common.test.FlowTestClient
import org.onflow.flow.common.test.FlowTestUtil
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.nio.charset.StandardCharsets

@JsonCadenceConversion(TestClassConverterJson::class)
open class TestClass(
    val address: FlowAddress,
    val balance: BigDecimal,
    val hashAlgorithm: HashAlgorithm,
    val isValid: Boolean
)

class TestClassConverterJson : JsonCadenceConverter<TestClass> {
    override fun unmarshall(value: Field<*>, namespace: CadenceNamespace): TestClass = unmarshall(value) {
        TestClass(
            address = FlowAddress(address("address")),
            balance = bigDecimal("balance"),
            hashAlgorithm = enum("hashAlgorithm"),
            isValid = boolean("isValid")
        )
    }

    override fun marshall(value: TestClass, namespace: CadenceNamespace): Field<*> {
        return marshall {
            struct {
                compositeOfPairs(namespace.withNamespace("TestClass")) {
                    listOf(
                        "address" to address(value.address.base16Value),
                        "balance" to ufix64(value.balance),
                        "hashAlgorithm" to enum(value.hashAlgorithm),
                        "isValid" to boolean(value.isValid)
                    )
                }
            }
        }
    }
}

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
class ScriptTest {
    @FlowTestClient
    lateinit var accessAPI: FlowAccessApi

    @Test
    fun `Can execute a script`() {
        val loadedScript = String(FlowTestUtil.loadScript("cadence/hello_world.cdc"), StandardCharsets.UTF_8)
        val result = accessAPI.simpleFlowScript {
            script {
                loadedScript
            }
        }

        when (result) {
            is FlowAccessApi.AccessApiCallResponse.Success -> {
                val jsonCadence = result.data.jsonCadence
                if (jsonCadence is StringField) {
                    assertEquals("Hello World", jsonCadence.value)
                } else {
                    throw IllegalStateException("Expected StringField but got ${jsonCadence::class.simpleName}")
                }
            }
            is FlowAccessApi.AccessApiCallResponse.Error -> throw IllegalStateException("Failed to execute script: ${result.message}", result.throwable)
        }
    }

    @Test
    fun `Can input and export arguments`() {
        val address = "e467b9dd11fa00df"
        val loadedScript = String(FlowTestUtil.loadScript("cadence/import_export_arguments.cdc"), StandardCharsets.UTF_8)

        val result = accessAPI.simpleFlowScript {
            script {
                loadedScript
            }
            arg { address(address) }
        }

        when (result) {
            is FlowAccessApi.AccessApiCallResponse.Success -> {
                assertTrue(result.data.jsonCadence is StructField)
                val struct = Flow.unmarshall(TestClass::class, result.data.jsonCadence)
                assertEquals(address, struct.address.base16Value)
                assertEquals(BigDecimal("1234"), struct.balance.stripTrailingZeros())
                assertEquals(HashAlgorithm.SHA3_256, struct.hashAlgorithm)
                assertTrue(struct.isValid)
            }
            is FlowAccessApi.AccessApiCallResponse.Error -> throw IllegalStateException("Failed to execute script: ${result.message}", result.throwable)
        }
    }

    @Test
    fun `Test domain tags`() {
        val pairA = Crypto.generateKeyPair(SignatureAlgorithm.ECDSA_P256)
        val signerA = Crypto.getSigner(pairA.private, HashAlgorithm.SHA3_256)

        val pairB = Crypto.generateKeyPair(SignatureAlgorithm.ECDSA_P256)
        val signerB = Crypto.getSigner(pairB.private, HashAlgorithm.SHA3_256)

        val message = "666f6f"

        val signatureA = signerA.signAsUser(message.hexToBytes())
        val signatureB = signerB.signAsUser(message.hexToBytes())

        val publicKeys = marshall {
            array {
                listOf(
                    string(pairA.public.hex),
                    string(pairB.public.hex)
                )
            }
        }

        val weights = marshall {
            array {
                listOf(
                    ufix64("100.00"),
                    ufix64("0.5")
                )
            }
        }

        val signatures = marshall {
            array {
                listOf(
                    string(signatureA.bytesToHex()),
                    string(signatureB.bytesToHex())
                )
            }
        }
        val loadedScript = String(FlowTestUtil.loadScript("cadence/domain_tags.cdc"), StandardCharsets.UTF_8)

        val result = accessAPI.simpleFlowScript {
            script {
                loadedScript
            }
            arg { publicKeys }
            arg { weights }
            arg { signatures }
            arg { string(message) }
        }

        when (result) {
            is FlowAccessApi.AccessApiCallResponse.Success -> {
                assertTrue(result.data.jsonCadence is BooleanField)
                assertTrue((result.data.jsonCadence as BooleanField).value!!)
            }
            is FlowAccessApi.AccessApiCallResponse.Error -> throw IllegalStateException("Failed to execute script: ${result.message}", result.throwable)
        }
    }
}
