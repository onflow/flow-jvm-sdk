access(all) struct TestClass {
    access(all) let address: Address
    access(all) let balance: UFix64
    access(all) let hashAlgorithm: HashAlgorithm
    access(all) let isValid: Bool

    init(address: Address, balance: UFix64, hashAlgorithm: HashAlgorithm, isValid: Bool) {
        self.address = address
        self.balance = balance
        self.hashAlgorithm = hashAlgorithm
        self.isValid = isValid
    }
}

access(all) fun main(address: Address): TestClass {
    return TestClass(
        address: address,
        balance: UFix64(1234),
        hashAlgorithm: HashAlgorithm.SHA3_256,
        isValid: true
    )
}