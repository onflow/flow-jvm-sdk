package org.onflow.flow.sdk.extensions

import org.onflow.flow.sdk.AsyncFlowAccessApi
import org.onflow.flow.sdk.FlowAccessApi
import org.onflow.flow.sdk.SignatureAlgorithm
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.onflow.flow.common.test.*

@FlowEmulatorTest
class TestExtensionsTest {
    @FlowTestClient
    lateinit var accessAPI: FlowAccessApi

    @FlowTestClient
    lateinit var asyncAccessApi: AsyncFlowAccessApi

    @FlowServiceAccountCredentials
    lateinit var serviceAccount: TestAccount

    @FlowTestAccount(
        contracts = [
            FlowTestContractDeployment(
                name = "NonFungibleToken",
                codeFileLocation = "./flow/NonFungibleToken.cdc"
            )
        ]
    )
    lateinit var account0: TestAccount

    @FlowTestAccount(
        signAlgo = SignatureAlgorithm.ECDSA_P256,
        balance = 69.0,
        contracts = [
            FlowTestContractDeployment(
                name = "NothingContract",
                codeClasspathLocation = "/cadence/test_extensions/NothingContract.cdc",
                arguments = [
                    TestContractArg("name", "The Name"),
                    TestContractArg("description", "The Description"),
                ]
            )
        ]
    )
    lateinit var account1: TestAccount

    @FlowTestAccount(
        signAlgo = SignatureAlgorithm.ECDSA_SECP256k1,
        balance = 420.0,
        contracts = [
            FlowTestContractDeployment(
                name = "EmptyContract",
                codeClasspathLocation = "/cadence/test_extensions/EmptyContract.cdc",
            )
        ]
    )
    lateinit var account2: TestAccount

    @FlowTestAccount(
        contracts = [
            FlowTestContractDeployment(
                name = "NonFungibleToken",
                codeFileLocation = "./flow/NonFungibleToken.cdc"
            ),
            FlowTestContractDeployment(
                name = "NothingContract",
                codeClasspathLocation = "/cadence/test_extensions/NothingContract.cdc",
                arguments = [
                    TestContractArg("name", "The Name"),
                    TestContractArg("description", "The Description"),
                ]
            ),
            FlowTestContractDeployment(
                name = "EmptyContract",
                codeClasspathLocation = "/cadence/test_extensions/EmptyContract.cdc",
            )
        ]
    )
    lateinit var account3: TestAccount

    @FlowTestAccount
    lateinit var account4: TestAccount

    @FlowTestAccount(
        signAlgo = SignatureAlgorithm.ECDSA_SECP256k1,
        balance = 420.0,
        contracts = [
            FlowTestContractDeployment(
                name = "ContractInterface",
                codeClasspathLocation = "/cadence/test_extensions/ContractInterface.cdc",
            ),
            FlowTestContractDeployment(
                name = "ContractSuccessor",
                codeClasspathLocation = "/cadence/test_extensions/ContractSuccessor.cdc",
            ),
        ]
    )
    lateinit var account5: TestAccount

    @Test
    fun `Test extensions work`() {
        accessAPI.ping()
        asyncAccessApi.ping().get()
        assertTrue(serviceAccount.isValid)
        assertTrue(account0.isValid)
        assertTrue(account1.isValid)
        assertTrue(account2.isValid)
        assertTrue(account3.isValid)
        assertTrue(account4.isValid)
        assertTrue(account5.isValid)

        val addresses = setOf(
            account0.flowAddress,
            account1.flowAddress,
            account2.flowAddress,
            account3.flowAddress,
            account4.flowAddress,
            account5.flowAddress,
        )
        assertEquals(6, addresses.size)
    }
}
