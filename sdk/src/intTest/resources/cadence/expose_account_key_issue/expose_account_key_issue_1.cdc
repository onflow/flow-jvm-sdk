import "FlowToken"
import "FungibleToken"

transaction(startingBalance: UFix64, publicKey: String, signatureAlgorithm: UInt8, hashAlgorithm: UInt8) {
    prepare(signer: auth(BorrowValue) &Account) {

        let newAccount = Account(payer: signer)

        newAccount.keys.add(
            publicKey: PublicKey(
                publicKey: publicKey.decodeHex(),
                signatureAlgorithm: SignatureAlgorithm(rawValue: signatureAlgorithm)!
            ),
            hashAlgorithm: HashAlgorithm(rawValue: hashAlgorithm)!,
            weight: UFix64(1000)
        )

        let provider = signer.capabilities.borrow<auth(FungibleToken.Withdraw) &FlowToken.Vault>(from: /storage/flowTokenVault)!

        let newVault = newAccount.capabilities.borrow<&{FungibleToken.Receiver}>(/public/flowTokenReceiver)!

        let tokensWithdrawn <- provider.withdraw(amount: startingBalance)
        newVault.deposit(from: <- tokensWithdrawn)
    }
}