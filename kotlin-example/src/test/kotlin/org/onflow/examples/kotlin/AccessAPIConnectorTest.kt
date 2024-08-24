package org.onflow.examples.kotlin

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.onflow.examples.kotlin.createAccount.CreateAccountExample
import org.onflow.flow.common.test.*
import org.onflow.flow.sdk.FlowAccessApi
import org.onflow.flow.sdk.FlowAddress
import org.onflow.flow.sdk.SignatureAlgorithm
import org.onflow.flow.sdk.bytesToHex
import org.onflow.flow.sdk.crypto.Crypto
import org.onflow.flow.sdk.crypto.PublicKey
import org.onflow.flow.sdk.crypto.PrivateKey
import java.math.BigDecimal

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
internal class AccessAPIConnectorTest {
    // user key pairs using all supported signing algorithms
    private val userKeyPairs = arrayOf(
        Crypto.generateKeyPair(SignatureAlgorithm.ECDSA_P256),
        Crypto.generateKeyPair(SignatureAlgorithm.ECDSA_SECP256k1)
    )

    // user account addresses
    private val userAccountAddress = arrayOf(
        FlowAddress(""),
        FlowAddress("")
    )

    @FlowServiceAccountCredentials
    lateinit var serviceAccount: TestAccount

    @FlowTestClient
    lateinit var accessAPI: FlowAccessApi

    @FlowTestAccount
    lateinit var otherAccount: TestAccount

    @BeforeEach
    fun setupUsers() {
        for ((index, address) in userAccountAddress.withIndex()) {
            if (address == FlowAddress("")) {
                // create test accounts
                userAccountAddress[index] = createUserAccount(userKeyPairs[index].public)
                // make sure test accounts have enough tokens for the tests
                val amount = BigDecimal("100.00000001")
                transferTokens(serviceAccount.flowAddress, serviceAccount.privateKey, userAccountAddress[index], amount)
            }
        }
    }

    // create an account using the service account
    private fun createUserAccount(userPublicKey: PublicKey): FlowAddress {
        val createAccountExample = CreateAccountExample(serviceAccount.privateKey, accessAPI)
        val account = createAccountExample.createAccount(serviceAccount.flowAddress, userPublicKey)
        return account
    }

    // create an account using the service account
    private fun transferTokens(sender: FlowAddress, senderKey: PrivateKey, to: FlowAddress, amount: BigDecimal) {
        val accessAPIConnector = AccessAPIConnector(senderKey, accessAPI)
        accessAPIConnector.transferTokens(sender, to, amount)
    }

    @Test
    fun `Can transfer tokens to other account`() {
        val amount = BigDecimal("10.00000001")
        val recipient = otherAccount.flowAddress
        transferTokens(serviceAccount.flowAddress, serviceAccount.privateKey, recipient, amount)

        val accessAPIConnector = AccessAPIConnector(serviceAccount.privateKey, accessAPI)
        val balance1 = accessAPIConnector.getAccountBalance(recipient)
        accessAPIConnector.transferTokens(serviceAccount.flowAddress, recipient, amount)
        val balance2 = accessAPIConnector.getAccountBalance(recipient)
        Assertions.assertEquals(balance1.add(amount), balance2)
    }

    @Test
    fun `Can transfer tokens from user accounts`() {
        val recipient: FlowAddress = serviceAccount.flowAddress
        val amount = BigDecimal("1.00000001")

        for ((index, sender) in userAccountAddress.withIndex()) {
            val accessAPIConnector = AccessAPIConnector(userKeyPairs[index].private, accessAPI)
            val balance1 = accessAPIConnector.getAccountBalance(recipient)
            transferTokens(sender, userKeyPairs[index].private, recipient, amount)

            val balance2 = accessAPIConnector.getAccountBalance(recipient)
            Assertions.assertEquals(balance1.add(amount), balance2)
        }
    }

    @Test
    fun `Can get an account balance`() {
        val accessAPIConnector = AccessAPIConnector(serviceAccount.privateKey, accessAPI)
        val balance = accessAPIConnector.getAccountBalance(serviceAccount.flowAddress)
        Assertions.assertNotNull(balance)
    }
}
