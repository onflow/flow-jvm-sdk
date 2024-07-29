package org.onflow.examples.kotlin

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.onflow.flow.common.test.*
import org.onflow.flow.sdk.FlowAccessApi
import org.onflow.flow.sdk.FlowAddress
import org.onflow.flow.sdk.crypto.Crypto
import org.onflow.flow.sdk.test.*
import java.math.BigDecimal

@FlowEmulatorTest
internal class AccessAPIConnectorTest {
    private var userPrivateKeyHex: String = ""
    private var userPublicKeyHex: String = ""

    @FlowServiceAccountCredentials
    lateinit var serviceAccount: TestAccount

    @FlowTestClient
    lateinit var accessAPI: FlowAccessApi

    @FlowTestAccount
    lateinit var recipientAccount: TestAccount

    @BeforeEach
    fun setupUser() {
        val keyPair = Crypto.generateKeyPair()
        userPrivateKeyHex = keyPair.private.hex
        userPublicKeyHex = keyPair.public.hex
    }

    @Test
    fun `Can create an account`() {
        val accessAPIConnector = AccessAPIConnector(serviceAccount.privateKey, accessAPI)
        val account = accessAPIConnector.createAccount(serviceAccount.flowAddress, userPublicKeyHex)
        Assertions.assertNotNull(account)
    }

    @Test
    fun `Can transfer tokens`() {
        val accessAPIConnector = AccessAPIConnector(serviceAccount.privateKey, accessAPI)

        val recipient: FlowAddress = recipientAccount.flowAddress

        val amount = BigDecimal("10.00000001")
        val balance1 = accessAPIConnector.getAccountBalance(recipient)
        accessAPIConnector.transferTokens(serviceAccount.flowAddress, recipient, amount)
        val balance2 = accessAPIConnector.getAccountBalance(recipient)
        Assertions.assertEquals(balance1.add(amount), balance2)
    }

    @Test
    fun `Can get an account balance`() {
        val accessAPIConnector = AccessAPIConnector(serviceAccount.privateKey, accessAPI)
        val balance = accessAPIConnector.getAccountBalance(serviceAccount.flowAddress)
        Assertions.assertNotNull(balance)
    }
}
