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
}
