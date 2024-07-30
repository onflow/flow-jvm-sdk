transaction(publicKey: String) {
    prepare(signer: auth(AddKey) &Account) {
        signer.keys.add(
            publicKey: publicKey.decodeHex()
        )
    }
}