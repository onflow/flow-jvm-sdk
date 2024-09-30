package org.onflow.examples.kotlin.addKey

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.onflow.examples.kotlin.AccessAPIConnector
import org.onflow.flow.common.test.*
import org.onflow.flow.sdk.*
import org.onflow.flow.sdk.crypto.Crypto

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

        // Generate a new key for the test
        val newKeyPair = Crypto.generateKeyPair(SignatureAlgorithm.ECDSA_P256)
        val newPublicKey = FlowPublicKey(newKeyPair.public.hex)

        val signAlgo = SignatureAlgorithm.ECDSA_P256
        val hashAlgo = HashAlgorithm.SHA3_256

        val txResult = connector.addKeyToAccount(
            payerAddress = testAccount.flowAddress,
            newPublicKey = newPublicKey,
            signAlgo = signAlgo,
            hashAlgo = hashAlgo
        )

        Assertions.assertNotNull(txResult, "Transaction result should not be null")
        Assertions.assertTrue(txResult.status == FlowTransactionStatus.SEALED, "Transaction should be sealed")

        val updatedAccount = accessAPIConnector.getAccount(testAccount.flowAddress)
        Assertions.assertEquals(3, updatedAccount.keys.size, "The account should now have 3 keys")

        // Check that the added key is the one generated
        val addedKey = updatedAccount.keys[2]
        Assertions.assertEquals(newPublicKey.base16Value, addedKey.publicKey.base16Value, "The added key should match the expected public key")
    }
}
