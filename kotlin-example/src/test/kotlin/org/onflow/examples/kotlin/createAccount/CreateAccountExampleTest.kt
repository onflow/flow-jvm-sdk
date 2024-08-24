package org.onflow.examples.kotlin.createAccount

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.onflow.examples.kotlin.AccessAPIConnector
import org.onflow.flow.common.test.*
import org.onflow.flow.sdk.FlowAccessApi
import org.onflow.flow.sdk.FlowAddress
import org.onflow.flow.sdk.SignatureAlgorithm
import org.onflow.flow.sdk.bytesToHex
import org.onflow.flow.sdk.crypto.Crypto
import org.onflow.flow.sdk.crypto.PublicKey

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
internal class CreateAccountExampleTest {
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

    // create an account using the service account
    private fun createUserAccount(userPublicKey: PublicKey): FlowAddress {
        val createAccountExample = CreateAccountExample(serviceAccount.privateKey, accessAPI)
        val account = createAccountExample.createAccount(serviceAccount.flowAddress, userPublicKey)
        return account
    }

    @Test
    fun `Can create an account`() {
        for ((index, address) in userAccountAddress.withIndex()) {
            if (address == FlowAddress("")) {
                // create test accounts
                userAccountAddress[index] = createUserAccount(userKeyPairs[index].public)
            }
        }

        for ((index, address) in userAccountAddress.withIndex()) {
            Assertions.assertNotNull(address)
            // check account key is the expected one
            val accessAPIConnector = AccessAPIConnector(serviceAccount.privateKey, accessAPI)
            val newAccountKey = accessAPIConnector.getAccountKey(address, 0)
            Assertions.assertEquals(userKeyPairs[index].public.hex, newAccountKey.publicKey.bytes.bytesToHex())
        }
    }
    @Test
    fun `Can get an account balance`() {
        val accessAPIConnector = AccessAPIConnector(serviceAccount.privateKey, accessAPI)
        val balance = accessAPIConnector.getAccountBalance(serviceAccount.flowAddress)
        Assertions.assertNotNull(balance)
    }
}
