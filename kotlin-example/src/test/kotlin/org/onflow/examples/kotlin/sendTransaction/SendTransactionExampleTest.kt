package org.onflow.examples.kotlin.sendTransaction

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.onflow.examples.kotlin.createAccount.CreateAccountExampleTest
import org.onflow.flow.common.test.*
import org.onflow.flow.sdk.*
import org.onflow.flow.sdk.crypto.Crypto

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
internal class SendTransactionExampleTest {
    // user key pairs using supported signing algorithms
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

    private lateinit var transactionConnector: SendTransactionExample

    @BeforeEach
    fun setup() {
        transactionConnector = SendTransactionExample(serviceAccount.privateKey, accessAPI)
    }

    @Test
    fun `Can send a simple transaction`() {
        for ((index, address) in userAccountAddress.withIndex()) {
            if (address == FlowAddress("")) {
                // create test account
                userAccountAddress[index] = CreateAccountExampleTest().createUserAccount(userKeyPairs[index].public)
            }
        }

        val txResult = transactionConnector.sendSimpleTransaction(userAccountAddress[0])

        Assertions.assertNotNull(txResult)
        Assertions.assertTrue(txResult.status === FlowTransactionStatus.SEALED, "Transaction should be sealed")
    }

    @Test
    fun `Can send a complex transaction with arguments`() {
        for ((index, address) in userAccountAddress.withIndex()) {
            if (address == FlowAddress("")) {
                // create test account
                userAccountAddress[index] = CreateAccountExampleTest().createUserAccount(userKeyPairs[index].public)
            }
        }

        val greeting = "Hello Flow!"
        val txResult = transactionConnector.sendComplexTransactionWithArguments(userAccountAddress[0], greeting = greeting)

        Assertions.assertNotNull(txResult)
        Assertions.assertTrue(txResult.status === FlowTransactionStatus.SEALED, "Transaction should be sealed")
    }
}
