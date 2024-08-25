package org.onflow.examples.kotlin.sendTransaction

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.onflow.flow.common.test.*
import org.onflow.flow.sdk.*

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
internal class SendTransactionExampleTest {
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
        val txResult = transactionConnector.sendSimpleTransaction(serviceAccount.flowAddress)

        Assertions.assertNotNull(txResult)
        Assertions.assertTrue(txResult.status === FlowTransactionStatus.SEALED, "Transaction should be sealed")
    }

    @Test
    fun `Can send a complex transaction with arguments`() {
        val greeting = "Hello Flow!"
        val txResult = transactionConnector.sendComplexTransactionWithArguments(serviceAccount.flowAddress, greeting = greeting)

        Assertions.assertNotNull(txResult)
        Assertions.assertTrue(txResult.status === FlowTransactionStatus.SEALED, "Transaction should be sealed")
    }
}
