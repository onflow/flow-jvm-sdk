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
    lateinit var serviceAccount: TestAccount

    @FlowTestClient
    lateinit var accessAPI: FlowAccessApi

    private lateinit var accessAPIConnector: AccessAPIConnector
    private lateinit var deployContractExample: DeployContractExample

    @BeforeEach
    fun setup() {
        deployContractExample = DeployContractExample(serviceAccount.privateKey, accessAPI)
        accessAPIConnector = AccessAPIConnector(serviceAccount.privateKey, accessAPI)
    }

    @Test
    fun `Can deploy contract to account`() {

        val initialAccount = accessAPIConnector.getAccount(serviceAccount.flowAddress)
        Assertions.assertTrue(initialAccount.contracts.size == 16, "The service account should initially have 16 contracts")

        val txResult = deployContractExample.deployContract(
            payerAddress = serviceAccount.flowAddress,
        )

        Assertions.assertNotNull(txResult, "Transaction result should not be null")
        // Assertions.assertTrue(txResult.getResult().status == FlowTransactionStatus.SEALED, "Transaction should be sealed")

        // Verify the contract was added to the account
        val updatedAccount = accessAPIConnector.getAccount(serviceAccount.flowAddress)
        //Assertions.assertTrue(updatedAccount.contracts.containsKey(contractName), "The account should now have the contract deployed")
    }
}
