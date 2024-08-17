package org.onflow.examples.kotlin.getTransaction

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.onflow.examples.kotlin.AccessAPIConnector
import org.onflow.flow.common.test.FlowEmulatorProjectTest
import org.onflow.flow.common.test.FlowServiceAccountCredentials
import org.onflow.flow.common.test.FlowTestClient
import org.onflow.flow.common.test.TestAccount
import org.onflow.flow.sdk.*
import org.onflow.flow.sdk.crypto.Crypto

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
internal class GetTransactionAccessAPIConnectorTest {
    @FlowServiceAccountCredentials
    lateinit var serviceAccount: TestAccount

    @FlowTestClient
    lateinit var accessAPI: FlowAccessApi

    private lateinit var connector: GetTransactionAccessAPIConnector
    private lateinit var accessAPIConnector: AccessAPIConnector

    private lateinit var txID: FlowId

    @BeforeEach
    fun setup() {
        accessAPIConnector = AccessAPIConnector(serviceAccount.privateKey, accessAPI)
        connector = GetTransactionAccessAPIConnector(accessAPI)

        // Send a sample transaction to create an account and capture the transaction ID
        val publicKey = Crypto.generateKeyPair(SignatureAlgorithm.ECDSA_P256).public
        txID = accessAPIConnector.sendSampleTransaction(
            serviceAccount.flowAddress,
            publicKey
        )
    }

    @Test
    fun `Can fetch a transaction`() {
        val transaction = connector.getTransaction(txID)

        assertNotNull(transaction, "Transaction should not be null")
        assertEquals(txID, transaction.id, "Transaction ID should match")
    }

    @Test
    fun `Can fetch a transaction result`() {
        val transactionResult = connector.getTransactionResult(txID)

        assertNotNull(transactionResult, "Transaction result should not be null")
        assertTrue(transactionResult.status === FlowTransactionStatus.SEALED, "Transaction should be sealed")
    }
}
