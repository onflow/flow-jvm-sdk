transaction(bytes: [UInt8]) {
    prepare(signer: &Account) {
        log(bytes)
    }
}