package com.nftco.flow.sdk.cadence

import com.nftco.flow.sdk.FlowId
import com.nftco.flow.sdk.IntegrationTestUtils
import com.nftco.flow.sdk.decode
import com.nftco.flow.sdk.simpleFlowScript
import kotlinx.serialization.Serializable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class JsonCadenceTest {
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

    private val flow = IntegrationTestUtils.newMainnetAccessApi()

    @Test
    fun `Can parse new JSON Cadence`() {
        val flow = IntegrationTestUtils.newMainnetAccessApi()
        val tx = flow.getTransactionResultById(FlowId("273f68ffe175a0097db60bc7cf5e92c5a775d189af3f5636f5432c1206be771a"))!!
        val events = tx.events.map { it.payload.jsonCadence }
        assertThat(events).hasSize(7)
    }

    @Test
    fun decodeOptional() {
        val result = flow.simpleFlowScript {
            script {
                """
                pub fun main(): Bool? {
                 return nil
                }
                """.trimIndent()
            }
        }
        val data = result.jsonCadence.decode<Boolean?>()
        assertThat(data).isEqualTo(null)
    }

    @Test
    fun decodeOptional2() {
        val result = flow.simpleFlowScript {
            script {
                """
                pub fun main(): Bool? {
                 return true
                }
                """.trimIndent()
            }
        }
        val data = result.jsonCadence.decode<Boolean?>()
        assertThat(data).isEqualTo(true)
    }

    @Test
    fun decodeBoolean() {
        val result = flow.simpleFlowScript {
            script {
                """
                pub fun main(): Bool {
                 return true
                }
                """.trimIndent()
            }
        }
        val data = result.jsonCadence.decode<Boolean>()
        assertThat(data).isEqualTo(true)
    }

    @Test
    fun decodeArray() {
        val result = flow.simpleFlowScript {
            script {
                """
                pub fun main(): [UInt64] {
                 return [1,3,4,5]
                }
                """.trimIndent()
            }
        }

        val data = result.jsonCadence.decode<List<ULong>>()
        assertThat(data.first()).isEqualTo(1UL)
        assertThat(data).hasSize(4)
    }

    @Test
    fun decodeUFix64() {
        val result = flow.simpleFlowScript {
            script {
                """
                pub fun main(): UFix64 {
                 return 0.789111
                }
                """.trimIndent()
            }
        }

        val data = result.jsonCadence.decode<Double>()
        assertThat(data).isEqualTo(0.789111)
    }

    @Test
    fun decodeStruct() {
        val result = flow.simpleFlowScript {
            script {
                """
                  pub struct StorageInfo {
                      pub let capacity: Int
                      pub let used: Int
                      pub let available: Int

                      init(capacity: Int, used: Int, available: Int) {
                          self.capacity = capacity
                          self.used = used
                          self.available = available
                      }
                  }

                  pub fun main(addr: Address): [StorageInfo] {
                    let acct = getAccount(addr)
                    return [StorageInfo(capacity: 1,
                                       used: 2,
                                       available: 3)]
                  }
                """.trimIndent()
            }

            arg { address("0x84221fe0294044d7") }
        }

        val data = result.jsonCadence.decode<List<StorageInfo>>().first()
        assertThat(data.capacity).isEqualTo(1)
        assertThat(data.used).isEqualTo(2)
        assertThat(data.available).isEqualTo(3)
    }

    @Test
    fun decodeComplexDict() {
        val result = flow.simpleFlowScript {
            script {
                """
                            pub struct StorageInfo {
                                pub let capacity: UInt64
                                pub let used: UInt64
                                pub let available: UInt64
                                pub let foo: Foo

                                init(capacity: UInt64, used: UInt64, available: UInt64, foo: Foo) {
                                    self.capacity = capacity
                                    self.used = used
                                    self.available = available
                                    self.foo = foo
                                }
                            }
                            
                            pub struct Foo {
                                pub let bar: Int

                                    init(bar: Int) {
                                        self.bar = bar
                                    }
                            }

                            pub fun main(addr: Address): {String: [StorageInfo]} {
                              let acct = getAccount(addr)
                              
                               let foo = Foo(bar: 1)
                              return {"test": [StorageInfo(capacity: acct.storageCapacity,
                                                used: acct.storageUsed,
                                                available: acct.storageCapacity - acct.storageUsed,
                                                foo: foo)]}
                            }
                """.trimIndent()
            }

            arg { address("0x84221fe0294044d7") }
        }

        val data = result.decode<Map<String, List<StorageInfoComplex>>>()
        assertThat(data["test"]!!.first().foo.bar).isEqualTo(1)
    }

    @Test
    fun decodeResource() {
        val result = flow.simpleFlowScript {
            script {
                """
                pub resource SomeResource {
                    pub var value: Int
                    
                    init(value: Int) {
                        self.value = value
                    }
                }
                
                pub fun main(): @SomeResource {
                    let newResource <- create SomeResource(value: 20)
                    return <-newResource
                }
                """.trimIndent()
            }
        }

        val decodedResource = result.jsonCadence.decodeToAny()
        assertThat(decodedResource).isNotNull()
    }

    @Test
    fun decodeEnum() {
        val result = flow.simpleFlowScript {
            script {
                """
                pub enum Color: UInt8 {
                   pub case red
                   pub case green
                   pub case blue
                }
    
                pub fun main() : Color {
                    return Color.red
                }
                """.trimIndent()
            }
        }

        val decodedEnum = result.jsonCadence.decodeToAny()
        assertThat(decodedEnum).isNotNull()
    }

    @Test
    fun decodeReference() {
        val result = flow.simpleFlowScript {
            script {
                """
                pub let hello = "Hello"
                pub let helloRef: &String = &hello as &String
                
                pub fun main(): &String {
                    return helloRef
                }
                """.trimIndent()
            }
        }

        val decodedReference = result.jsonCadence.decodeToAny()
        assertThat(decodedReference).isNotNull()
    }
}
