transaction(publicKey: String) {
	prepare(signer: AuthAccount) {
		let account = AuthAccount(payer: signer)
        account.addPublicKey(publicKey.decodeHex())
	}
}
