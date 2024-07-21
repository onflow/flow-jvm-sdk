pub struct StorageInfo {
    pub let capacity: UInt64
    pub let used: UInt64
    pub let available: UInt64
    pub let foo: Foo

    init(capacity: UInt64, used: UInt64, available: UInt64, foo: Foo) {
        self.capacity = capacity
        self.used = used
        self.available = available
        self.foo = foo
    }
}

pub struct Foo {
    pub let bar: Int

    init(bar: Int) {
        self.bar = bar
    }
}

pub fun main(addr: Address): {String: [StorageInfo]} {
    let acct = getAccount(addr)

    let foo = Foo(bar: 1)
    return {"test": [StorageInfo(capacity: acct.storageCapacity,
                      used: acct.storageUsed,
                      available: acct.storageCapacity - acct.storageUsed,
                      foo: foo)]}
}