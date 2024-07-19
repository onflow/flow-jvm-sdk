transaction(bytes: [UInt8]) {
    prepare(signer: AuthAccount) {
        log(bytes)
    }
}