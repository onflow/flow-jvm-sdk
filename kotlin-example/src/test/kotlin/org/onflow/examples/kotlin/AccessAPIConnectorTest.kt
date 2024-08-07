package org.onflow.examples.kotlin

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.onflow.flow.common.test.*
import org.onflow.flow.sdk.FlowAccessApi
import org.onflow.flow.sdk.FlowAddress
import org.onflow.flow.sdk.bytesToHex
import org.onflow.flow.sdk.crypto.Crypto
import java.math.BigDecimal

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
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
        println("userPublicKeyHex: $userPublicKeyHex")
        // use the service account as a payer to create a new user account that has `userPublicKeyHex`
        val account = accessAPIConnector.createAccount(serviceAccount.flowAddress, userPublicKeyHex)
        Assertions.assertNotNull(account)

        println("Service account address: "  + serviceAccount.flowAddress)
        println("Created account address: $account")

        // get the newly created user account
        val key = accessAPIConnector.getAccountKey(account, 0)
        val keyHex = key.publicKey.bytes.bytesToHex()
        // the key is not the expected one
        // it's the service account key that seems to be wrongly put in the nw account
        Assertions.assertEquals(keyHex, userPublicKeyHex)
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
