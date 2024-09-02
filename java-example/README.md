# JVM SDK - Java Examples

This package contains runnable code examples that use the [Flow JVM SDK](https://github.com/onflow/flow-jvm-sdk) to interact with the Flow blockchain in Java. The examples use the [Flow Emulator](https://github.com/onflow/flow/blob/master/docs/content/emulator/index.md) to simulate a live network connection.

## Running the emulator with the Flow CLI

The emulator is bundled with the [Flow CLI](https://docs.onflow.org/flow-cli), a command-line interface for working with Flow.

### Installation

This repository is configured to run with Cadence 1.0. Follow [these steps](https://cadence-lang.org/docs/cadence-migration-guide#install-cadence-10-cli) to install the Cadence 1.0 CLI, currently in pre-release.

## Running the examples

Each code example has a corresponding test file located in `/java-example/src/test`. Each test file provides a series of runnable functions which boot up the emulator and invoke the corresponding code example. We recommend using IntelliJ IDEA (see the free Community Edition [here](https://www.jetbrains.com/idea/download/)) to interact with and run the tests. However, you can also trigger an individual test run with `./gradlew :java-example:test --tests "com.example.MyTestClass.myTestMethod"`.

## Examples summary

Below is a list of all Java code examples currently supported in this repo:

#### Get Blocks

[Get blocks by ID, height, or latest sealed.](src/main/java/org/onflow/examples/java/getBlock/GetBlockAccessAPIConnector.java)

- Get the latest sealed block
- Get block by ID
- Get block by height

### Get Accounts

[Get accounts by address.](src/main/java/org/onflow/examples/java/getAccount/GetAccountAccessAPIConnector.java)

- Get account balance
- Get account from the latest block
- Get account from block by height

#### Get Events

[Get events emitted by transactions.](src/main/java/org/onflow/examples/java/getEvent/GetEventAccessAPIConnector.java)

- Get events for height range
- Get events for block IDs
- Get events directly from transaction result

#### Get Collection

[Get collections by ID.](src/main/java/org/onflow/examples/java/getCollection/GetCollectionAccessAPIConnector.java)

#### Get Network Parameters

[Get the current network parameters.](src/main/java/org/onflow/examples/java/getNetworkParams/GetNetworkParametersAccessAPIConnector.java)

#### Get Transactions

[Get transactions.](src/main/java/org/onflow/examples/java/getTransaction/GetTransactionAccessAPIConnector.java)

- Get transaction
- Get transaction result

#### Sending Transactions

[Sending transactions.](src/main/java/org/onflow/examples/java/sendTransaction/SendTransactionExample.java)

- Send transaction
- Send transaction with arguments

#### Execute Script

[Execute a Cadence script.](src/main/java/org/onflow/examples/java/executeScript/ExecuteScriptAccessAPIConnector.java)

- Execute a simple Cadence script
- Execute a more complex Cadence script with arguments

#### Create Account

[Create a new account on Flow.](src/main/java/org/onflow/examples/java/createAccount/CreateAccountExample.java)

#### Add Account Key

[Add a key to an existing account.](src/main/java/org/onflow/examples/java/addKey/AddAccountKeyExample.java)

#### Deploy Contract

[Deploy a Cadence smart contract.](src/main/java/org/onflow/examples/java/deployContract/DeployContractExample.java)

#### Transaction Signing

[Common paradigms for signing transactions.](src/main/java/org/onflow/examples/java/signTransaction/SignTransactionExample.java)

- Single party, single signature
- Single party, multiple signatures
- Multiple parties
- Multiple parties, 2 authorizers
- Multiple parties, multiple signatures