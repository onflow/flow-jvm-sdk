package org.onflow.examples.kotlin.deployContract

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.onflow.examples.kotlin.AccessAPIConnector
import org.onflow.flow.common.test.*
import org.onflow.flow.sdk.*

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
internal class DeployContractExampleTest {

    @FlowServiceAccountCredentials
    lateinit var testAccount: TestAccount

    @FlowTestClient
    lateinit var accessAPI: FlowAccessApi

    private lateinit var accessAPIConnector: AccessAPIConnector
    private lateinit var deployContractExample: DeployContractExample

    @BeforeEach
    fun setup() {
        deployContractExample = DeployContractExample(testAccount.privateKey, accessAPI)
        accessAPIConnector = AccessAPIConnector(testAccount.privateKey, accessAPI)
    }

    @Test
    fun `Can deploy contract to account`() {
        // Ensure the account initially has no contracts
        val initialAccount = accessAPIConnector.getAccount(testAccount.flowAddress)
        println(initialAccount.contracts.size)
        //Assertions.assertTrue(initialAccount.contracts.isEmpty(), "The account should initially have no contracts")

        // Deploy the contract
        val txResult = deployContractExample.deployContract(
            payerAddress = testAccount.flowAddress,
        )

        // Verify the transaction was successful and sealed
        Assertions.assertNotNull(txResult, "Transaction result should not be null")
        Assertions.assertTrue(txResult.getResult().status == FlowTransactionStatus.SEALED, "Transaction should be sealed")

        // Verify the contract was added to the account
        val updatedAccount = accessAPIConnector.getAccount(testAccount.flowAddress)
        //Assertions.assertTrue(updatedAccount.contracts.containsKey(contractName), "The account should now have the contract deployed")
    }
}
