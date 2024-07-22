transaction(publicKey: String) {
	prepare(signer: &Account) {
		let newAccount = Account(payer: signer)
        newAccount.addPublicKey(publicKey.decodeHex())
	}
}
