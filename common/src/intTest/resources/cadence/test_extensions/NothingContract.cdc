
access(all) contract NothingContract {

    access(all) let name: String
    access(all) let description: String

    init(name: String, description: String) {
        self.name = name
        self.description = description
    }
}
