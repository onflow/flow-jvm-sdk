package org.onflow.flow.sdk

import org.onflow.flow.sdk.crypto.Crypto
import org.junit.jupiter.api.Assertions.*
import org.onflow.flow.common.test.FlowEmulatorProjectTest
import org.onflow.flow.common.test.FlowTestClient
import org.onflow.flow.common.test.FlowServiceAccountCredentials
import org.onflow.flow.common.test.TestAccount
import org.onflow.flow.common.test.FlowTestUtil
import org.onflow.flow.sdk.IntegrationTestUtils.getAccount
import org.onflow.flow.sdk.IntegrationTestUtils.getAccountAddressFromResult
import org.onflow.flow.sdk.IntegrationTestUtils.handleResult
import java.math.BigDecimal
import java.nio.charset.StandardCharsets

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
class ExposeAccountKeyIssueTest {
    @FlowTestClient
    lateinit var flow: FlowAccessApi

    @FlowServiceAccountCredentials
    lateinit var serviceAccount: TestAccount

    // Constants
    private val startingBalance = BigDecimal.ONE
    private val signatureAlgorithm1 = SignatureAlgorithm.ECDSA_P256
    private val hashAlgorithm1 = HashAlgorithm.SHA3_256

    // Ignoring for now
    // @Test
    fun `Expose issue with account keys api`() {
        val addressRegistry = AddressRegistry()
        addressRegistry.registerDefaults()
        addressRegistry.defaultChainId = FlowChainId.EMULATOR

        Flow.configureDefaults(
            chainId = FlowChainId.EMULATOR,
            addressRegistry = addressRegistry
        )

        // Create the account
        val pair1 = Crypto.generateKeyPair(signatureAlgorithm1)
        val signer1 = Crypto.getSigner(pair1.private, hashAlgorithm1)

        val loadedScript1 = String(FlowTestUtil.loadScript("cadence/expose_account_key_issue/expose_account_key_issue_1.cdc"), StandardCharsets.UTF_8)
        val createAccountResult = flow.simpleFlowTransaction(
            serviceAccount.flowAddress,
            serviceAccount.signer
        ) {
            script {
                loadedScript1
            }
            arguments {
                arg { ufix64(startingBalance) }
                arg { string(pair1.public.hex) }
                arg { uint8(signatureAlgorithm1.index) }
                arg { uint8(hashAlgorithm1.index) }
            }
        }.sendAndWaitForSeal()

        val createAccountResultData = handleResult(createAccountResult, "Failed to create account")
        val newAccountAddress = getAccountAddressFromResult(createAccountResultData)

        val account = getAccount(flow, newAccountAddress)

        assertEquals(1, account.keys.size)
        assertEquals(pair1.public.hex, account.keys[0].publicKey.base16Value)
        assertFalse(account.keys[0].revoked)

        // Add second pair
        val signatureAlgorithm2 = SignatureAlgorithm.ECDSA_P256
        val hashAlgorithm2 = HashAlgorithm.SHA3_256
        val pair2 = Crypto.generateKeyPair(signatureAlgorithm2)
        val signer2 = Crypto.getSigner(pair2.private, hashAlgorithm2)

        val loadedScript2 = String(FlowTestUtil.loadScript("cadence/expose_account_key_issue/expose_account_key_issue_2.cdc"), StandardCharsets.UTF_8)
        val addKeyResult = flow.simpleFlowTransaction(newAccountAddress, signer1) {
            script {
                loadedScript2
            }
            arguments {
                arg { string(pair2.public.hex) }
                arg { uint8(signatureAlgorithm2.index) }
                arg { uint8(hashAlgorithm2.index) }
                arg { ufix64(1000) }
            }
        }.sendAndWaitForSeal()

        handleResult(addKeyResult, "Failed to add key")

        val updatedAccount = getAccount(flow, newAccountAddress)

        assertEquals(2, updatedAccount.keys.size)
        assertEquals(pair1.public.hex, updatedAccount.keys[0].publicKey.base16Value)
        assertEquals(pair2.public.hex, updatedAccount.keys[1].publicKey.base16Value)
        assertFalse(updatedAccount.keys[0].revoked)
        assertFalse(updatedAccount.keys[1].revoked)

        val loadedScript3 = String(FlowTestUtil.loadScript("cadence/expose_account_key_issue/expose_account_key_issue_3.cdc"), StandardCharsets.UTF_8)
        // Remove the second key
        val removeKeyResult = flow.simpleFlowTransaction(newAccountAddress, signer1) {
            script {
                loadedScript3
            }
            arguments {
                arg { int(1) }
            }
        }.sendAndWaitForSeal()

        handleResult(removeKeyResult, "Failed to remove key")

        val finalAccount = getAccount(flow, newAccountAddress)

        assertEquals(2, finalAccount.keys.size)
        assertEquals(pair1.public.hex, finalAccount.keys[0].publicKey.base16Value)
        assertEquals(pair2.public.hex, finalAccount.keys[1].publicKey.base16Value)
        assertFalse(finalAccount.keys[0].revoked)
        assertTrue(finalAccount.keys[1].revoked)
    }
}
