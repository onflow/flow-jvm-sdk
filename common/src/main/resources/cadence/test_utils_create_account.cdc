import FlowToken from 0x0ae53cb6e3f42a79
import FungibleToken from 0xee82856bf20e2aa6

transaction(startingBalance: UFix64, publicKey: String, signatureAlgorithm: UInt8, hashAlgorithm: UInt8) {
    prepare(signer: auth(BorrowValue) &Account) {

        let newAccount = Account(payer: signer)

        let payerVaultRef = signer.storage.borrow<auth(FungibleToken.Withdraw) &FlowToken.Vault>(from: /storage/flowTokenVault)!

        let newAccountVaultRef = newAccount.capabilities.borrow<&{FungibleToken.Receiver}>(/public/flowTokenReceiver)!

        let tokensWithdrawn <- payerVaultRef.withdraw(amount: startingBalance)
        newAccountVaultRef.deposit(from: <- tokensWithdrawn)

        newAccount.keys.add(
            publicKey: PublicKey(
                publicKey: publicKey.decodeHex(),
                signatureAlgorithm: SignatureAlgorithm(rawValue: signatureAlgorithm)!
            ),
            hashAlgorithm: HashAlgorithm(rawValue: hashAlgorithm)!,
            weight: UFix64(1000)
        )
    }
}