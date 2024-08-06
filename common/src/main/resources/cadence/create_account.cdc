transaction(publicKey: String) {
 	prepare(signer: auth(BorrowValue) &Account) {
 		let newAccount = Account(payer: signer)
         newAccount.keys.add(
             publicKey: PublicKey(
                 publicKey: publicKey.decodeHex(),
                 signatureAlgorithm: SignatureAlgorithm.ECDSA_P256
             ),
             hashAlgorithm: HashAlgorithm.SHA3_256,
             weight: UFix64(1000)
         )
 	}
}