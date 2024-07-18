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