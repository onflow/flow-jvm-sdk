package org.onflow.examples.kotlin.signTransaction

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.onflow.flow.common.test.*
import org.onflow.flow.sdk.*

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
internal class SignTransactionExampleTest {
    @FlowServiceAccountCredentials
    lateinit var serviceAccount: TestAccount

    @FlowTestAccount
    lateinit var testAccount: TestAccount

    @FlowTestAccount
    lateinit var testAccount2: TestAccount

    @FlowTestClient
    lateinit var accessAPI: FlowAccessApi

    private lateinit var transactionConnector: SignTransactionExample

    @BeforeEach
    fun setup() {
        transactionConnector = SignTransactionExample(serviceAccount.privateKey, accessAPI)
    }

    @Test
    fun `Can sign single party single sig transaction`() {
        val txResult = transactionConnector.singlePartySingleSignature(serviceAccount.flowAddress)

        Assertions.assertNotNull(txResult)
        Assertions.assertTrue(txResult.status === FlowTransactionStatus.SEALED, "Transaction should be sealed")
    }

    @Test
    fun `Can sign single party multi-sig transaction`() {
        transactionConnector = SignTransactionExample(testAccount.privateKey, accessAPI)
        val txResult = transactionConnector.singlePartyMultiSignature(testAccount.flowAddress)

        Assertions.assertNotNull(txResult)
        Assertions.assertTrue(txResult.status === FlowTransactionStatus.SEALED, "Transaction should be sealed")
    }

    @Test
    fun `Can sign multi-party single sig transaction`() {
        val txResult = transactionConnector.multiPartySingleSignature(
            account1PrivateKey = serviceAccount.privateKey,
            account2PrivateKey = testAccount.privateKey,
            payerAddress = testAccount.flowAddress,
            authorizerAddress = serviceAccount.flowAddress
        )

        Assertions.assertNotNull(txResult)
        Assertions.assertTrue(txResult.status === FlowTransactionStatus.SEALED, "Transaction should be sealed")
    }

    @Test
    fun `Can sign multi-party multi-sig transaction`() {
        val txResult = transactionConnector.multiPartyMultiSignature(
            account1PrivateKeys = listOf(testAccount2.privateKey, testAccount2.privateKey),
            account2PrivateKeys = listOf(testAccount.privateKey, testAccount.privateKey),
            payerAddress = testAccount.flowAddress,
            authorizerAddress = testAccount2.flowAddress
        )

        Assertions.assertNotNull(txResult)
        Assertions.assertTrue(txResult.status === FlowTransactionStatus.SEALED, "Transaction should be sealed")
    }

    @Test
    fun `Can sign multi-party 2 authorizers transaction`() {
        val txResult = transactionConnector.multiParty2Authorizers(
            account1PrivateKey = serviceAccount.privateKey,
            account2PrivateKey = testAccount.privateKey,
            payerAddress = testAccount.flowAddress,
            authorizer1Address = serviceAccount.flowAddress,
            authorizer2Address = testAccount.flowAddress
        )

        Assertions.assertNotNull(txResult)
        Assertions.assertTrue(txResult.status === FlowTransactionStatus.SEALED, "Transaction should be sealed")
    }
}
