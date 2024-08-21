package org.onflow.examples.kotlin.executeScript

import com.google.protobuf.ByteString
import org.onflow.flow.sdk.*
import java.math.BigDecimal

internal class ExecuteScriptAccessAPIConnector(
    private val accessAPI: FlowAccessApi
) {
    fun executeSimpleScript(): FlowScriptResponse {
        val script = """
            pub fun main(a: Int): Int {
                return a + 10
            }
        """.trimIndent()

        val args = listOf(ByteString.copyFromUtf8("5"))
        val response = accessAPI.executeScriptAtLatestBlock(
            FlowScript(script.toByteArray()),
            args
        )

        return when (response) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
        }
    }

    fun executeComplexScript(): User {
        val script = """
            pub struct User {
                pub var balance: UFix64
                pub var address: Address
                pub var name: String

                init(name: String, address: Address, balance: UFix64) {
                    self.name = name
                    self.address = address
                    self.balance = balance
                }
            }

            pub fun main(name: String): User {
                return User(
                    name: name,
                    address: 0x1,
                    balance: 10.0
                )
            }
        """.trimIndent()

        val args = listOf(ByteString.copyFromUtf8("my_name"))
        val response = accessAPI.executeScriptAtLatestBlock(
            FlowScript(script.toByteArray()),
            args
        )

        val value = when (response) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
        }

        val struct = value.jsonCadence.decode<User>()
        return struct
    }

    data class User(
        val balance: BigDecimal,
        val address: FlowAddress,
        val name: String
    )
}

