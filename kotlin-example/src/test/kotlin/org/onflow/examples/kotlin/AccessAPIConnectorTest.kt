package org.onflow.examples.kotlin

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.onflow.flow.common.test.*
import org.onflow.flow.sdk.FlowAccessApi
import org.onflow.flow.sdk.FlowAddress
import org.onflow.flow.sdk.SignatureAlgorithm
import org.onflow.flow.sdk.crypto.Crypto
import java.math.BigDecimal

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
internal class AccessAPIConnectorTest {
    // TODO: update to use PrivateKey and PublicKey types instead
    // of hex Strings
    private var userPrivateKeyHex: String = ""
    private var userPublicKeyHex: String = ""
    private var userSigningAlgo: SignatureAlgorithm = SignatureAlgorithm.UNKNOWN

    @FlowServiceAccountCredentials
    lateinit var serviceAccount: TestAccount

    @FlowTestClient
    lateinit var accessAPI: FlowAccessApi

    @FlowTestAccount
    lateinit var otherAccount: TestAccount

    @BeforeEach
    fun setupUser() {
        userSigningAlgo = SignatureAlgorithm.ECDSA_SECP256k1
        val keyPair = Crypto.generateKeyPair(userSigningAlgo)
        userPrivateKeyHex = keyPair.private.hex
        userPublicKeyHex = keyPair.public.hex
    }

    @Test
    fun `Can create an account`() {
        val accessAPIConnector = AccessAPIConnector(serviceAccount.privateKey, accessAPI)
        val account = accessAPIConnector.createAccount(serviceAccount.flowAddress, userPublicKeyHex, userSigningAlgo)
        Assertions.assertNotNull(account)
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
    fun `Can transfer tokens from other account`() {
        val accessAPIConnector = AccessAPIConnector(otherAccount.privateKey, accessAPI)
        // make sure this test checks the SECp256k1 case
        Assertions.assertEquals(otherAccount.privateKey.algo, SignatureAlgorithm.ECDSA_SECP256k1)

        val sender: FlowAddress = otherAccount.flowAddress
        val recipient: FlowAddress = serviceAccount.flowAddress

        val amount = BigDecimal("10.00000001")
        val balance1 = accessAPIConnector.getAccountBalance(recipient)
        accessAPIConnector.transferTokens(sender, recipient, amount)
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
