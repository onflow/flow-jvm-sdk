access(all) resource SomeResource {
    access(all) var value: Int

    init(value: Int) {
        self.value = value
    }
}

access(all) fun main(): @SomeResource {
    let newResource <- create SomeResource(value: 20)
    return <-newResource
}