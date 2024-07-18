pub struct TestClass {
    pub let address: Address
    pub let balance: UFix64
    pub let hashAlgorithm: HashAlgorithm
    pub let isValid: Bool

    init(address: Address, balance: UFix64, hashAlgorithm: HashAlgorithm, isValid: Bool) {
        self.address = address
        self.balance = balance
        self.hashAlgorithm = hashAlgorithm
        self.isValid = isValid
    }
}

pub fun main(address: Address): TestClass {
    return TestClass(
        address: address,
        balance: UFix64(1234),
        hashAlgorithm: HashAlgorithm.SHA3_256,
        isValid: true
    )
}