transaction {
  prepare(acc1: &Account, acc2: &Account) {
  }

  execute {
    log("Transaction executed with two authorizers")
  }
}