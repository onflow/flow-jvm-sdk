transaction(publicKey: String) {
    prepare(signer: &Account) {
        signer.keys.add(
            publicKey: publicKey.decodeHex()
        )
    }
}