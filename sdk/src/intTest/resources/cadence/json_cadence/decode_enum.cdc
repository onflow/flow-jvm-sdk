access(all) enum Color: UInt8 {
   access(all) case red
   access(all) case green
   access(all) case blue
}

access(all) fun main() : Color {
    return Color.red
}