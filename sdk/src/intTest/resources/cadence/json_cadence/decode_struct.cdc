pub struct StorageInfo {
    pub let capacity: Int
    pub let used: Int
    pub let available: Int

    init(capacity: Int, used: Int, available: Int) {
        self.capacity = capacity
        self.used = used
        self.available = available
    }
}

pub fun main(addr: Address): [StorageInfo] {
    let acct = getAccount(addr)
    return [StorageInfo(capacity: 1, used: 2, available: 3)]
}