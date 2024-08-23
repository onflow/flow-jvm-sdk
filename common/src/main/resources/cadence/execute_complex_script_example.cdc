access(all) struct StorageInfo {
    access(all) let capacity: Int
    access(all) let used: Int
    access(all) let available: Int

    init(capacity: Int, used: Int, available: Int) {
        self.capacity = capacity
        self.used = used
        self.available = available
    }
}

access(all) fun main(addr: Address): [StorageInfo] {
    let acct = getAccount(addr)
    return [StorageInfo(capacity: 1, used: 2, available: 3)]
}