pub resource SomeResource {
    pub var value: Int

    init(value: Int) {
        self.value = value
    }
}

pub fun main(): @SomeResource {
    let newResource <- create SomeResource(value: 20)
    return <-newResource
}