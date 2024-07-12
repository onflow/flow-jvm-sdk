package org.onflow.examples.kotlin

import java.math.BigDecimal
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.onflow.flow.sdk.FlowAddress
import org.onflow.flow.sdk.crypto.Crypto

val serviceAccountAddress: FlowAddress = FlowAddress("f8d6e0586b0a20c7")
const val servicePrivateKeyHex = "a2f983853e61b3e27d94b7bf3d7094dd756aead2a813dd5cf738e1da56fa9c17"

internal class AppTest {
    private var userPrivateKeyHex: String = ""
    private var userPublicKeyHex: String = ""

    @BeforeEach
    fun setupUser() {
        val keyPair = Crypto.generateKeyPair()
        userPrivateKeyHex = keyPair.private.hex
        userPublicKeyHex = keyPair.public.hex
    }

    @Test
    fun `Can create an account`() {
        val app = App("localhost", 3569, servicePrivateKeyHex)

        // service account address
        app.createAccount(serviceAccountAddress, userPublicKeyHex)
    }

    @Test
    fun `Can transfer tokens`() {
        val app = App("localhost", 3569, servicePrivateKeyHex)

        // service account address
        val recipient: FlowAddress = app.createAccount(serviceAccountAddress, userPublicKeyHex)

        // FLOW amounts always have 8 decimal places
        val amount = BigDecimal("10.00000001")
        val balance1 = app.getAccountBalance(recipient)
        app.transferTokens(serviceAccountAddress, recipient, amount)
        val balance2 = app.getAccountBalance(recipient)
        Assertions.assertEquals(balance1.add(amount), balance2)
    }

    @Test
    fun `Can get an account balance`() {
        val app = App("localhost", 3569, servicePrivateKeyHex)
        val balance = app.getAccountBalance(serviceAccountAddress)
        println(balance)
    }
}
