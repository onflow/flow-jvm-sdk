transaction(index: Int) {
    prepare(signer: auth(RevokeKey) &Account) {
        signer.keys.revoke(keyIndex: index) ?? panic("Key not found to revoke")
    }
}