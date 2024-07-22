transaction(publicKey: String) {
    prepare(signer: &Account) {
        signer.addPublicKey(publicKey.decodeHex())
    }
}