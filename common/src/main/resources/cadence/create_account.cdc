transaction(publicKey: String, signAlgo: UInt8) {
 	prepare(signer: auth(BorrowValue) &Account) {
 		let newAccount = Account(payer: signer)
         newAccount.keys.add(
             publicKey: PublicKey(
                 publicKey: publicKey.decodeHex(),
                 signatureAlgorithm: SignatureAlgorithm(signAlgo)!,
             ),
             hashAlgorithm: HashAlgorithm.SHA3_256,
             weight: UFix64(1000)
         )
 	}
}