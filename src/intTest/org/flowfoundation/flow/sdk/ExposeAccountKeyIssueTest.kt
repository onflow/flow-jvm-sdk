package org.flowfoundation.flow.sdk

import org.flowfoundation.flow.sdk.cadence.AddressField
import org.flowfoundation.flow.sdk.crypto.Crypto
import org.flowfoundation.flow.sdk.test.FlowEmulatorTest
import org.flowfoundation.flow.sdk.test.FlowServiceAccountCredentials
import org.flowfoundation.flow.sdk.test.FlowTestClient
import org.flowfoundation.flow.sdk.test.TestAccount
import org.junit.jupiter.api.Assertions.*
import java.math.BigDecimal

@FlowEmulatorTest
class ExposeAccountKeyIssueTest {
    @FlowTestClient
    lateinit var flow: org.flowfoundation.flow.sdk.FlowAccessApi

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
            .throwOnError()

        val newAccountAddress = createAccountResult.getEventsOfType("flow.AccountCreated", expectedCount = 1)
            .first()
            .get<AddressField>("address")
            ?.value
            ?.let { FlowAddress(it) }
            ?: throw IllegalStateException("AccountCreated event not found")

        var account = requireNotNull(flow.getAccountAtLatestBlock(newAccountAddress))
        assertEquals(1, account.keys.size)
        assertEquals(pair1.public.hex, account.keys[0].publicKey.base16Value)
        assertFalse(account.keys[0].revoked)

        // add second pair

        val signatureAlgorithm2 = SignatureAlgorithm.ECDSA_P256
        val hashAlgorithm2 = HashAlgorithm.SHA3_256
        val pair2 = Crypto.generateKeyPair(signatureAlgorithm1)
        val signer2 = Crypto.getSigner(pair1.private, hashAlgorithm1)

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
            .throwOnError()

        account = requireNotNull(flow.getAccountAtLatestBlock(newAccountAddress))
        assertEquals(2, account.keys.size)
        assertEquals(pair1.public.hex, account.keys[0].publicKey.base16Value)
        assertEquals(pair2.public.hex, account.keys[1].publicKey.base16Value)
        assertFalse(account.keys[0].revoked)
        assertFalse(account.keys[1].revoked)

        // remove the second key

        val removeResult = flow.simpleFlowTransaction(newAccountAddress, signer1) {
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
            .throwOnError()

        account = requireNotNull(flow.getAccountAtLatestBlock(newAccountAddress))
        assertEquals(2, account.keys.size)
        assertEquals(pair1.public.hex, account.keys[0].publicKey.base16Value)
        assertEquals(pair2.public.hex, account.keys[1].publicKey.base16Value)
        assertFalse(account.keys[0].revoked)
        assertTrue(account.keys[1].revoked)
    }
}
