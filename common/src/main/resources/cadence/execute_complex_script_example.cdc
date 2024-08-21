access(all) struct User {
    access(all) var balance: UFix64
    access(all) var address: Address
    access(all) var name: String

    init(name: String, address: Address, balance: UFix64) {
        self.name = name
        self.address = address
        self.balance = balance
    }
}

access(all) fun main(name: String): User {
    return User(
        name: name,
        address: 0x1,
        balance: 10.0
    )
}