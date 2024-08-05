access(all) struct StorageInfo {
    access(all) let capacity: UInt64
    access(all) let used: UInt64
    access(all) let available: UInt64
    access(all) let foo: Foo

    init(capacity: UInt64, used: UInt64, available: UInt64, foo: Foo) {
        self.capacity = capacity
        self.used = used
        self.available = available
        self.foo = foo
    }
}

access(all) struct Foo {
    access(all) let bar: Int

    init(bar: Int) {
        self.bar = bar
    }
}

access(all) fun main(addr: Address): {String: [StorageInfo]} {
    let acct = getAccount(addr)

    let foo = Foo(bar: 1)
    return {"test": [StorageInfo(capacity: acct.storage.capacity,
                      used: acct.storage.used,
                      available: acct.storage.capacity - acct.storage.used,
                      foo: foo)]}
}