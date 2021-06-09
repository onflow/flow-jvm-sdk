package org.onflow.sdk

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.onflow.sdk.cadence.AddressField
import org.onflow.sdk.cadence.BooleanField
import org.onflow.sdk.cadence.CadenceNamespace
import org.onflow.sdk.cadence.Field
import org.onflow.sdk.cadence.JsonCadenceConversion
import org.onflow.sdk.cadence.JsonCadenceConverter
import org.onflow.sdk.cadence.StringField
import org.onflow.sdk.cadence.StructField
import org.onflow.sdk.cadence.UFix64NumberField
import org.onflow.sdk.cadence.marshall
import org.onflow.sdk.cadence.unmarshall
import org.onflow.sdk.crypto.Crypto
import org.onflow.sdk.test.FlowEmulatorTest
import java.math.BigDecimal

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
                        "signatureAlgorithm" to enum(value.hashAlgorithm),
                        "isValid" to boolean(value.isValid)
                    )
                }
            }
        }
    }
}

@FlowEmulatorTest
class ScriptTest {

    @Test
    fun `Can execute a script`() {
        val accessAPI = Flow.newAccessApi("localhost", 3570)

        val result = accessAPI.simpleFlowScript {
            script {
                """
                    pub fun main(): String {
                        return "Hello World"
                    }
                """
            }
        }

        assertTrue(result.jsonCadence is StringField)
        assertEquals("Hello World", result.jsonCadence.value)
    }

    @Test
    fun `Can input and export arguments`() {
        val accessAPI = Flow.newAccessApi("localhost", 3570)
        val address = "e467b9dd11fa00df"

        val result = accessAPI.simpleFlowScript {
            script {
                """
                    pub struct TestClass {
                        pub let address: Address
                        pub let balance: UFix64
                        pub let hashAlgorithm: HashAlgorithm
                        pub let isValid: Bool
                        
                        init(address: Address, balance: UFix64, hashAlgorithm: HashAlgorithm, isValid: Bool) {
                            self.address = address
                            self.balance = balance
                            self.hashAlgorithm = hashAlgorithm
                            self.isValid = isValid
                        }
                    }
                    
                    pub fun main(address: Address): TestClass {
                        return TestClass(
                            address: address,
                            balance: UFix64(1234),
                            hashAlgorithm: HashAlgorithm.SHA3_256,
                            isValid: true
                        )
                    }
                """
            }
            arg { address(address) }
        }

        assertTrue(result.jsonCadence is StructField)
        val struct = Flow.unmarshall(TestClass::class, result.jsonCadence)
        assertEquals(address, struct.address.base16Value)
        assertEquals(BigDecimal("1234"), struct.balance.stripTrailingZeros())
        assertEquals(HashAlgorithm.SHA3_256, struct.hashAlgorithm)
        assertTrue(struct.isValid)
    }

    @Test
    fun `Test domain tags`() {
        val accessAPI = Flow.newAccessApi("localhost", 3570)

        val pairA = Crypto.generateKeyPair()
        val signerA = Crypto.getSigner(pairA.private, HashAlgorithm.SHA3_256)

        val pairB = Crypto.generateKeyPair()
        val signerB = Crypto.getSigner(pairB.private, HashAlgorithm.SHA3_256)

        val toAddress = AddressField("e7d6e0582b0a21c3")
        val fromAddress = AddressField("e536e1583b0a22d4")
        val amount = UFix64NumberField("100.00")

        val message = Flow.encodeJsonCadence(toAddress) + Flow.encodeJsonCadence(fromAddress) + Flow.encodeJsonCadence(amount)

        val signatureA = signerA.signWithDomain(message, DomainTag.USER_DOMAIN_TAG)
        val signatureB = signerB.signWithDomain(message, DomainTag.USER_DOMAIN_TAG)

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

        val result = accessAPI.simpleFlowScript {
            script {
                """
                    import Crypto
                    pub fun main(
                      rawPublicKeys: [String],
                      weights: [UFix64],
                      signatures: [String],
                      toAddress: Address,
                      fromAddress: Address,
                      amount: UFix64,
                    ): Bool {
                      let keyList = Crypto.KeyList()
                      var i = 0
                      for rawPublicKey in rawPublicKeys {
                        keyList.add(
                          Crypto.PublicKey(
                            publicKey: rawPublicKey.decodeHex(),
                            signatureAlgorithm: Crypto.ECDSA_P256
                          ),
                          hashAlgorithm: Crypto.SHA3_256,
                          weight: weights[i],
                        )
                        i = i + 1
                      }
                      let signatureSet: [Crypto.KeyListSignature] = []
                      var j = 0
                      for signature in signatures {
                        signatureSet.append(
                          Crypto.KeyListSignature(
                            keyIndex: j,
                            signature: signature.decodeHex()
                          )
                        )
                        j = j + 1
                      }
                      let message = toAddress.toBytes()
                        .concat(fromAddress.toBytes())
                        .concat(amount.toBigEndianBytes())
                      return keyList.isValid(
                        signatureSet: signatureSet,
                        signedData: message,
                      )
                    }
                """
            }
            arg { publicKeys }
            arg { weights }
            arg { signatures }
            arg { toAddress }
            arg { fromAddress }
            arg { amount }
        }

        assertTrue(result.jsonCadence is BooleanField)
        assertTrue((result.jsonCadence as BooleanField).value!!)
    }
}
