# JVM SDK - Kotlin Examples

This package contains runnable code examples that use the [Flow JVM SDK](https://github.com/onflow/flow-jvm-sdk) to interact with the Flow blockchain in Kotlin. The examples use the [Flow Emulator](https://developers.flow.com/tools/emulator) to simulate a live network connection.

## Table of Contents
- [Running the emulator with the Flow CLI](#running-the-emulator-with-the-flow-cli)
    - [Installation](#installation)
- [Running the examples](#running-the-examples)
- [Examples summary](#examples-summary)
    - [Get blocks](#get-blocks)
    - [Get accounts](#get-accounts)
    - [Get events](#get-events)
    - [Get collection](#get-collection)
    - [Get execution data](#get-execution-data)
    - [Get network parameters](#get-network-parameters)
    - [Get transactions](#get-transactions)
    - [Sending transactions](#sending-transactions)
    - [Execute script](#execute-script)
    - [Create account](#create-account)
    - [Add account key](#add-account-key)
    - [Deploy contract](#deploy-contract)
    - [Transaction signing](#transaction-signing)
    - [Verifying signatures](#verifying-signatures)
    - [Streaming events and execution data](#streaming-events-and-execution-data)
  
## Running the emulator with the Flow CLI

The emulator is bundled with the [Flow CLI](https://docs.onflow.org/flow-cli), a command-line interface for working with Flow. 

### Installation

This repository is configured to run with Cadence 1.0. Follow [these steps](https://cadence-lang.org/docs/cadence-migration-guide#install-cadence-10-cli) to install the Cadence 1.0 CLI.

## Running the examples

Each code example has a corresponding test file located in `/kotlin-example/src/test`. Each test file provides a series of runnable functions which boot up the emulator and invoke the corresponding code example. We recommend using IntelliJ IDEA (see the free Community Edition [here](https://www.jetbrains.com/idea/download/)) to interact with and run the tests. However, you can also trigger an individual test run with `./gradlew :kotlin-example:test --tests "com.example.MyTestClass.myTestMethod"`. 

## Examples summary

Below is a list of all Kotlin code examples currently supported in this repo:

#### Get Blocks

[Get blocks by ID, height, or latest sealed.](src/main/kotlin/org/onflow/examples/kotlin/getBlock/GetBlockAccessAPIConnector.kt)

- Get the latest sealed block
- Get block by ID
- Get block by height

#### Get Accounts

[Get accounts by address.](src/main/kotlin/org/onflow/examples/kotlin/getAccount/GetAccountAccessAPIConnector.kt)

- Get account balance
- Get account from the latest block
- Get account from block by height
- Get account key at latest block
- Get account keys at latest block
- Get account key at block height
- Get account keys at block height

#### Get Events

[Get events emitted by transactions.](src/main/kotlin/org/onflow/examples/kotlin/getEvent/GetEventAccessAPIConnector.kt)

- Get events for height range
- Get events for block IDs
- Get events directly from transaction result

#### Get Collection

[Get collections by ID.](src/main/kotlin/org/onflow/examples/kotlin/getCollection/GetCollectionAccessAPIConnector.kt)

- Get collection by id
- Get full collection by id (returns all transactions in collection response)

#### Get Execution Data

[Get execution data by block ID.](src/main/kotlin/org/onflow/examples/kotlin/getExecutionData/GetExecutionDataAccessAPIConnector.kt)

#### Get Network Parameters

[Get the current network parameters.](src/main/kotlin/org/onflow/examples/kotlin/getNetworkParams/GetNetworkParametersAccessAPIConnector.kt)

#### Get Transactions

[Get transactions.](src/main/kotlin/org/onflow/examples/kotlin/getTransaction/GetTransactionAccessAPIConnector.kt)

- Get transaction 
- Get system transaction
- Get transaction result
- Get system transaction result
- Get transaction result by index

#### Sending Transactions

[Sending transactions.](src/main/kotlin/org/onflow/examples/kotlin/sendTransaction/SendTransactionExample.kt)

- Send transaction
- Send transaction with arguments

#### Execute Script

[Execute a Cadence script.](src/main/kotlin/org/onflow/examples/kotlin/executeScript/ExecuteScriptAccessAPIConnector.kt)

- Execute a simple Cadence script
- Execute a more complex Cadence script with arguments

#### Create Account

[Create a new account on Flow.](src/main/kotlin/org/onflow/examples/kotlin/createAccount/CreateAccountExample.kt)

#### Add Account Key

[Add a key to an existing account.](src/main/kotlin/org/onflow/examples/kotlin/addKey/AddAccountKeyExample.kt)

#### Deploy Contract

[Deploy a Cadence smart contract.](src/main/kotlin/org/onflow/examples/kotlin/deployContract/DeployContractExample.kt)

#### Transaction Signing

[Common paradigms for signing transactions.](src/main/kotlin/org/onflow/examples/kotlin/signTransaction/SignTransactionExample.kt)

- Single party, single signature
- Single party, multiple signatures
- Multiple parties
- Multiple parties, 2 authorizers
- Multiple parties, multiple signatures

#### Verifying Signatures

[Common paradigms for signing messages.](src/main/kotlin/org/onflow/examples/kotlin/verifySignature)

- Signing an arbitrary message.
- Signing an arbitrary user message and verifying it using the public keys on an account, respecting the weights of each key.
- Signing an arbitrary user message and verifying it using the public keys on an account. Return success if any public key on the account can sign the message.

#### Streaming Events and Execution Data

[Utilizing the Access API subscription endpoints to stream event and execution data.](src/main/kotlin/org/onflow/examples/kotlin/streaming)

- Streaming events.
- Streaming events and reconnecting in the event of a failure.
- Streaming execution data.