import "FlowToken"
import "FungibleToken"

transaction(startingBalance: UFix64, publicKey: String, signatureAlgorithm: UInt8, hashAlgorithm: UInt8) {
    prepare(signer: auth(BorrowValue, Storage, Capabilities) &Account) {

        let newAccount = Account(payer: signer)

        let payerVaultRef = signer.capabilities.borrow<&FlowToken.Vault>(/storage/flowTokenVault)!

        let newAccountVaultRef = newAccount.capabilities.borrow<&{FungibleToken.Receiver}>(/public/flowTokenReceiver)!

        let tokensWithdrawn <- payerVaultRef.withdraw(amount: startingBalance)
        newAccountVaultRef.deposit(from: <- tokensWithdrawn)

        newAccount.keys.add(
            publicKey: PublicKey(
                publicKey: publicKey.decodeHex(),
                signatureAlgorithm: SignatureAlgorithm(rawValue: SignatureAlgorithm.ECDSA_P256)!
            ),
            hashAlgorithm: HashAlgorithm(rawValue: HashAlgorithm.SHA3_256)!,
            weight: UFix64(1000)
        )
    }
}