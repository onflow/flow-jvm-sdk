access(all) let hello = "Hello"
access(all) let helloRef: &String = &hello as &String

access(all) fun main(): &String {
    return helloRef
}