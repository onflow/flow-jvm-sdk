package org.onflow.examples.kotlin

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.onflow.flow.sdk.FlowAddress
import org.onflow.flow.sdk.crypto.Crypto
import java.math.BigDecimal

val serviceAccountAddress: FlowAddress = FlowAddress("f8d6e0586b0a20c7")
val testRecipientAddress: FlowAddress = FlowAddress("01cf0e2f2f715450")
const val servicePrivateKeyHex = "a2f983853e61b3e27d94b7bf3d7094dd756aead2a813dd5cf738e1da56fa9c17"

internal class AccessAPIConnectorTest {
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
        val accessAPIConnector = AccessAPIConnector("localhost", 3569, servicePrivateKeyHex)

        val account = accessAPIConnector.createAccount(serviceAccountAddress, userPublicKeyHex)
        Assertions.assertNotNull(account)
    }

    @Test
    fun `Can transfer tokens`() {
        val accessAPIConnector = AccessAPIConnector("localhost", 3569, servicePrivateKeyHex)

        // service account address
        val recipient: FlowAddress = testRecipientAddress

        // FLOW amounts always have 8 decimal places
        val amount = BigDecimal("10.00000001")
        val balance1 = accessAPIConnector.getAccountBalance(recipient)
        accessAPIConnector.transferTokens(serviceAccountAddress, recipient, amount)
        val balance2 = accessAPIConnector.getAccountBalance(recipient)
        Assertions.assertEquals(balance1.add(amount), balance2)
    }

    @Test
    fun `Can get an account balance`() {
        val accessAPIConnector = AccessAPIConnector("localhost", 3569, servicePrivateKeyHex)
        val balance = accessAPIConnector.getAccountBalance(serviceAccountAddress)
        Assertions.assertNotNull(balance)
    }
}
