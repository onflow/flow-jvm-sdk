transaction(index: Int) {
    prepare(signer: &Account) {
        signer.keys.revoke(keyIndex: index) ?? panic("Key not found to revoke")
    }
}