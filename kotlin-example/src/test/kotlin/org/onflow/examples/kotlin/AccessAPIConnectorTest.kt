package org.onflow.examples.kotlin

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.onflow.flow.common.test.*
import org.onflow.flow.sdk.FlowAccessApi
import org.onflow.flow.sdk.FlowAddress
import org.onflow.flow.sdk.SignatureAlgorithm
import org.onflow.flow.sdk.crypto.Crypto
import org.onflow.flow.sdk.crypto.PublicKey
import java.math.BigDecimal

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
internal class AccessAPIConnectorTest {
    // user key pairs using all supported signing algorithms
    private val userKeyPairs = arrayOf(
        // Crypto.generateKeyPair(SignatureAlgorithm.ECDSA_P256),
        Crypto.generateKeyPair(SignatureAlgorithm.ECDSA_SECP256k1)
    )

    // user account addresses
    private val userAccountAddress = arrayOf(
        FlowAddress(""),
        // FlowAddress("")
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
                userAccountAddress[index] = createUserAccount(userKeyPairs[index].public)
            }
        }
    }

    // create an account using the service account
    private fun createUserAccount(userPublicKey: PublicKey): FlowAddress {
        val accessAPIConnector = AccessAPIConnector(serviceAccount.privateKey, accessAPI)
        val account = accessAPIConnector.createAccount(serviceAccount.flowAddress, userPublicKey)
        return account
    }

    @Test
    fun `Can create an account`() {
        // accounts are created in `setupUser`
        for (address in userAccountAddress) {
            Assertions.assertNotNull(address)
        }
    }

    @Test
    fun `Can transfer tokens to other account`() {
        val accessAPIConnector = AccessAPIConnector(serviceAccount.privateKey, accessAPI)

        val recipient: FlowAddress = otherAccount.flowAddress

        val amount = BigDecimal("10.00000001")
        val balance1 = accessAPIConnector.getAccountBalance(recipient)
        accessAPIConnector.transferTokens(serviceAccount.flowAddress, recipient, amount)
        val balance2 = accessAPIConnector.getAccountBalance(recipient)
        Assertions.assertEquals(balance1.add(amount), balance2)
    }

    @Test
    fun `Can transfer tokens from user accounts`() {
        val recipient: FlowAddress = serviceAccount.flowAddress

        for ((index, sender) in userAccountAddress.withIndex()) {
            val accessAPIConnector = AccessAPIConnector(userKeyPairs[index].private, accessAPI)

            val amount = BigDecimal("1.00000001")
            val balance1 = accessAPIConnector.getAccountBalance(recipient)
            accessAPIConnector.transferTokens(sender, recipient, amount)
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
