package com.nftco.flow.sdk

import com.nftco.flow.sdk.cadence.*
import com.nftco.flow.sdk.crypto.Crypto
import com.nftco.flow.sdk.test.FlowEmulatorProjectTest
import com.nftco.flow.sdk.test.FlowTestClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
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

@FlowEmulatorProjectTest(flowJsonLocation = "flow/flow.json")
class ScriptTest {
    @FlowTestClient
    lateinit var accessAPI: FlowAccessApi

    @Test
    fun `Can execute a script`() {
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

        val result = accessAPI.simpleFlowScript {
            script {
                """
                    import Crypto

                    pub fun main(
                      rawPublicKeys: [String],
                      weights: [UFix64],
                      signatures: [String],
                      message: String,
                    ): Bool {
                    
                      var i = 0
                      let keyList = Crypto.KeyList()
                      for rawPublicKey in rawPublicKeys {
                        keyList.add(
                          PublicKey(
                            publicKey: rawPublicKey.decodeHex(),
                            signatureAlgorithm: SignatureAlgorithm.ECDSA_P256
                          ),
                          hashAlgorithm: HashAlgorithm.SHA3_256,
                          weight: weights[i],
                        )
                        i = i + 1
                      }
                      
                      i = 0
                      let signatureSet: [Crypto.KeyListSignature] = []
                      for signature in signatures {
                        signatureSet.append(
                          Crypto.KeyListSignature(
                            keyIndex: i,
                            signature: signature.decodeHex()
                          )
                        )
                        i = i + 1
                      }
                      
                      return keyList.verify(
                        signatureSet: signatureSet,
                        signedData: message.decodeHex(),
                      )
                    }
                """
            }
            arg { publicKeys }
            arg { weights }
            arg { signatures }
            arg { string(message) }
        }

        assertTrue(result.jsonCadence is BooleanField)
        assertTrue((result.jsonCadence as BooleanField).value!!)
    }
}
