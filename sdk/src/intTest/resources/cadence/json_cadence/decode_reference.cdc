pub let hello = "Hello"
pub let helloRef: &String = &hello as &String

pub fun main(): &String {
    return helloRef
}