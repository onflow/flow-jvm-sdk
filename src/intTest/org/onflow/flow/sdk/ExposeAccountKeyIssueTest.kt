package org.onflow.flow.sdk

import org.onflow.flow.sdk.cadence.AddressField
import org.onflow.flow.sdk.crypto.Crypto
import org.onflow.flow.sdk.test.FlowEmulatorTest
import org.onflow.flow.sdk.test.FlowServiceAccountCredentials
import org.onflow.flow.sdk.test.FlowTestClient
import org.onflow.flow.sdk.test.TestAccount
import org.junit.jupiter.api.Assertions.*
import java.math.BigDecimal

@FlowEmulatorTest
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

        // create the account

        val pair1 = Crypto.generateKeyPair(signatureAlgorithm1)
        val signer1 = Crypto.getSigner(pair1.private, hashAlgorithm1)

        val createAccountResult = flow.simpleFlowTransaction(
            serviceAccount.flowAddress,
            serviceAccount.signer
        ) {
            script {
                """
                import FlowToken from 0xFLOWTOKEN
                import FungibleToken from 0xFUNGIBLETOKEN

                transaction(startingBalance: UFix64, publicKey: String, signatureAlgorithm: UInt8, hashAlgorithm: UInt8) {
                    prepare(signer: AuthAccount) {
                        
                        let newAccount = AuthAccount(payer: signer)

                        newAccount.keys.add(
                            publicKey: PublicKey(
                                publicKey: publicKey.decodeHex(),
                                signatureAlgorithm: SignatureAlgorithm(rawValue: signatureAlgorithm)!
                            ),
                            hashAlgorithm: HashAlgorithm(rawValue: hashAlgorithm)!,
                            weight: UFix64(1000)
                        )

                        let provider = signer.borrow<&FlowToken.Vault>(from: /storage/flowTokenVault)
                            ?? panic("Could not borrow FlowToken.Vault reference")
                        
                        let newVault = newAccount
                            .getCapability(/public/flowTokenReceiver)
                            .borrow<&{FungibleToken.Receiver}>()
                            ?? panic("Could not borrow FungibleToken.Receiver reference")
                            
                        let coin <- provider.withdraw(amount: startingBalance)
                        newVault.deposit(from: <- coin)
                    }
                }
            """
            }
            arguments {
                arg { ufix64(startingBalance) }
                arg { string(pair1.public.hex) }
                arg { uint8(signatureAlgorithm1.index) }
                arg { uint8(hashAlgorithm1.index) }
            }
        }.sendAndWaitForSeal()

        val createAccountResultData = when (createAccountResult) {
            is FlowAccessApi.FlowResult.Success -> createAccountResult.data
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to create account: ${createAccountResult.message}", createAccountResult.throwable)
        }

        val newAccountAddress = createAccountResultData.getEventsOfType("flow.AccountCreated", expectedCount = 1)
            .first()
            .get<AddressField>("address")
            ?.value
            ?.let { FlowAddress(it) }
            ?: throw IllegalStateException("AccountCreated event not found")

        val account = when (val accountResult = flow.getAccountAtLatestBlock(newAccountAddress)) {
            is FlowAccessApi.FlowResult.Success -> accountResult.data
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to get account at latest block: ${accountResult.message}", accountResult.throwable)
        }

        assertEquals(1, account.keys.size)
        assertEquals(pair1.public.hex, account.keys[0].publicKey.base16Value)
        assertFalse(account.keys[0].revoked)

        // add second pair

        val signatureAlgorithm2 = SignatureAlgorithm.ECDSA_P256
        val hashAlgorithm2 = HashAlgorithm.SHA3_256
        val pair2 = Crypto.generateKeyPair(signatureAlgorithm2)
        val signer2 = Crypto.getSigner(pair2.private, hashAlgorithm2)

        val addKeyResult = flow.simpleFlowTransaction(newAccountAddress, signer1) {
            script {
                """
                    transaction(publicKey: String, signatureAlgorithm: UInt8, hashAlgorithm: UInt8, weight: UFix64) {
                        prepare(signer: AuthAccount) {
                            signer.keys.add(
                                publicKey: PublicKey(
                                    publicKey: publicKey.decodeHex(),
                                    signatureAlgorithm: SignatureAlgorithm(rawValue: signatureAlgorithm)!
                                ),
                                hashAlgorithm: HashAlgorithm(rawValue: hashAlgorithm)!,
                                weight: weight
                            )
                        }
                    }
                """
            }
            arguments {
                arg { string(pair2.public.hex) }
                arg { uint8(signatureAlgorithm2.index) }
                arg { uint8(hashAlgorithm2.index) }
                arg { ufix64(1000) }
            }
        }.sendAndWaitForSeal()

        val addKeyEvent = when (addKeyResult) {
            is FlowAccessApi.FlowResult.Success -> addKeyResult.data
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to add key: ${addKeyResult.message}", addKeyResult.throwable)
        }

        val updatedAccount = when (val updatedAccountResult = flow.getAccountAtLatestBlock(newAccountAddress)) {
            is FlowAccessApi.FlowResult.Success -> updatedAccountResult.data
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to get updated account at latest block: ${updatedAccountResult.message}", updatedAccountResult.throwable)
        }

        assertEquals(2, updatedAccount.keys.size)
        assertEquals(pair1.public.hex, updatedAccount.keys[0].publicKey.base16Value)
        assertEquals(pair2.public.hex, updatedAccount.keys[1].publicKey.base16Value)
        assertFalse(updatedAccount.keys[0].revoked)
        assertFalse(updatedAccount.keys[1].revoked)

        // remove the second key

        val removeKeyResult = flow.simpleFlowTransaction(newAccountAddress, signer1) {
            script {
                """
                    transaction(index: Int) {
                        prepare(signer: AuthAccount) {
                            signer.keys.revoke(keyIndex: index) ?? panic("Key not found to revoke")
                        }
                    }
                """
            }
            arguments {
                arg { int(1) }
            }
        }.sendAndWaitForSeal()

        val removeKeyEvent = when (removeKeyResult) {
            is FlowAccessApi.FlowResult.Success -> removeKeyResult.data
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to remove key: ${removeKeyResult.message}", removeKeyResult.throwable)
        }

        val finalAccount = when (val finalAccountResult = flow.getAccountAtLatestBlock(newAccountAddress)) {
            is FlowAccessApi.FlowResult.Success -> finalAccountResult.data
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to get final account at latest block: ${finalAccountResult.message}", finalAccountResult.throwable)
        }

        assertEquals(2, finalAccount.keys.size)
        assertEquals(pair1.public.hex, finalAccount.keys[0].publicKey.base16Value)
        assertEquals(pair2.public.hex, finalAccount.keys[1].publicKey.base16Value)
        assertFalse(finalAccount.keys[0].revoked)
        assertTrue(finalAccount.keys[1].revoked)
    }
}
