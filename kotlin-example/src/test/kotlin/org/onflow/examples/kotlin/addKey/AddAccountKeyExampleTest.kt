package org.onflow.examples.kotlin.addKey

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.onflow.examples.kotlin.AccessAPIConnector
import org.onflow.flow.common.test.*
import org.onflow.flow.sdk.*

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
internal class AddAccountKeyExampleTest {
    @FlowTestAccount
    lateinit var testAccount: TestAccount

    @FlowTestClient
    lateinit var accessAPI: FlowAccessApi

    private lateinit var accessAPIConnector: AccessAPIConnector
    private lateinit var connector: AddAccountKeyExample

    @BeforeEach
    fun setup() {
        connector = AddAccountKeyExample(testAccount.privateKey, accessAPI)
        accessAPIConnector = AccessAPIConnector(testAccount.privateKey, accessAPI)
    }

    @Test
    fun `Can add key to account`() {
        val initialKeys = accessAPIConnector.getAccount(testAccount.flowAddress)
        Assertions.assertEquals(2, initialKeys.keys.size, "The account should initially have 2 keys")

        val txResult = connector.addKeyToAccount(testAccount.flowAddress)

        Assertions.assertNotNull(txResult, "Transaction result should not be null")
        Assertions.assertTrue(txResult.status == FlowTransactionStatus.SEALED, "Transaction should be sealed")

        val updatedAccount = accessAPIConnector.getAccount(testAccount.flowAddress)
        Assertions.assertEquals(3, updatedAccount.keys.size, "The account should now have 3 keys")
    }
}
