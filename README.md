# Flow JVM SDK

[![Maven Central](https://img.shields.io/maven-central/v/org.onflow/flow-jvm-sdk)](https://search.maven.org/search?q=g:org.onflow%20AND%20a:flow-jvm-sdk) 
[![Sonatype OSS](https://img.shields.io/nexus/s/org.onflow/flow-jvm-sdk?label=snapshot&server=https%3A%2F%2Fs01.oss.sonatype.org%2F)](https://s01.oss.sonatype.org/content/repositories/snapshots/org/onflow/flow-jvm-sdk/)

The Flow JVM SDK is a library for JVM languages (e.g. Java, Kotlin, Scala, Groovy) that provides utilities to interact with the Flow blockchain.

For a summary of the breaking changes introduced in the latest release, please refer to [BREAKING_CHANGES.md](./BREAKING_CHANGES.md).

At the moment, this SDK includes the following features:
- [x] Communication with the [Flow Access API](https://docs.onflow.org/access-api) over gRPC 
- [x] Transaction preparation and signing
- [x] Cryptographic key generation, parsing, and signing
- [x] Marshalling & unmarshalling of [JSON-Cadence](https://docs.onflow.org/cadence/json-cadence-spec/)
- [x] DSL for creating, signing, and sending transactions and scripts

## Table of Contents
- [Repository structure](#repository-structure)
  - [SDK](#sdk)
  - [Java Examples](#java-examples)
  - [Kotlin Examples](#kotlin-examples)
  - [Common Utils](#common-utils)
- [Contribute to this SDK](#contribute-to-this-sdk)
- [Dependencies](#dependencies)
- [Getting Started](#getting-started)
  - [Installation](#installation)
  - [Generating Keys](#generating-keys)
  - [Supported Curves](#supported-curves)
  - [Accessing the Flow Network](#accessing-the-flow-network)
  - [Creating an Account](#creating-an-account)
  - [Signing Transactions](#signing-transactions)
  - [Sending Transactions](#sending-transactions)
  - [Querying Transaction Results](#querying-transaction-results)
  - [Querying Blocks](#querying-blocks)
  - [Executing a Script](#executing-a-script)
  - [Querying Events](#querying-events)
  - [Querying Accounts](#querying-accounts)
  - [Examples Summary](#examples-summary)
- [Credit](#credit)

## Repository structure

This repository is organized as a multi-module project, consisting of the following modules:

### SDK
The core module that includes all the necessary tools and libraries to interact with the Flow blockchain. This module provides the main functionalities such as transaction preparation, signing, and interaction with the Flow Access API. 
It also implements and tests use of the SDK via Java Annotations or Kotlin Extensions which can optionally be used when integrating.

### Java Examples
This module contains example implementations demonstrating how to use the Flow JVM SDK in a Java application. It includes sample code for various use cases, making it easier for developers to understand and integrate the SDK into their Java projects.

### Kotlin Examples
Similar to the Java Example module, this module provides sample implementations in Kotlin. It showcases how to leverage the SDK's capabilities in a Kotlin environment.

### Common Utils
The common utils module contains resources shared across all 3 above sub-modules, such as Cadence scripts and testing infrastructure.

## Contribute to this SDK

We welcome all community contributions and will gladly review improvements and other proposals as PRs.

Read the [contributing guide](./CONTRIBUTING.md) to get started.

## Dependencies

This SDK requires Java Developer Kit (JDK) 8 or newer.

## Getting Started

### Installation

To add the SDK to your project, check out [this README](/sdk/README.md/#installation) for sample Maven and Gradle setup configurations.

### Generating Keys

Flow uses [ECDSA](https://en.wikipedia.org/wiki/Elliptic_Curve_Digital_Signature_Algorithm)
to control access to user accounts. Each key pair can be used in combination with
the SHA2-256 or SHA3-256 hashing algorithms.

Here's how to generate an ECDSA key pair for the P-256 (secp256r1) curve:

```kotlin
val keyPair = Crypto.generateKeyPair(SignatureAlgorithm.ECDSA_P256)

val privateKey = keyPair.private
val publicKey = keyPair.public
```
One can then retrieve the hexadecimal representation of the key or encode it as bytes:

```kotlin
val privateKeyHex = privateKey.hex
val privateKeyBytes = privateKey.hex.toByteArray()
```

#### Supported Curves

The example above uses an ECDSA key pair on the P-256 (secp256r1) elliptic curve.
Flow also supports the secp256k1 curve used by Bitcoin and Ethereum.

Here's how to generate an ECDSA private key for the secp256k1 curve:

```kotlin
val keyPair = Crypto.generateKeyPair(SignatureAlgorithm.ECDSA_SECP256k1)
```

Here's a full list of the supported signature and hash algorithms on Flow: [Flow Signature & Hash Algorithms](https://cadence-lang.org/docs/language/crypto#hashing).

### Accessing the Flow Network

You can communicate with any Flow Access Node using the Flow JVM SDK. This includes official Access Nodes, nodes you run yourself, and hosted nodes. Flow JVM SDK currently only supports gRPC communication with Access Nodes.

Here's how to create a new gRPC client for any network:

```kotlin
private const val MAINNET_HOSTNAME = "access.mainnet.nodes.onflow.org"
private const val TESTNET_HOSTNAME = "access.devnet.nodes.onflow.org"

fun newAccessApiConnnection(): FlowAccessApi = Flow.newAccessApi(MAINNET_HOSTNAME)

val accessAPIConnection = newAccessApiConnnection()
```

### Creating an Account

Once you have [generated a key pair](#generating-keys), you can create a new account
using its public key. Check out the **Create Account** example for a runnable code snippet in [Java](java-example/src/main/java/org/onflow/examples/java/createAccount/CreateAccountExample.java) or [Kotlin](kotlin-example/src/main/kotlin/org/onflow/examples/kotlin/createAccount/CreateAccountExample.kt).

### Signing Transactions

Transaction signing is accomplished through the `Crypto.Signer` interface. Below is a simple example of how to sign a transaction using a `PrivateKey` generated with `Crypto.generateKeyPair()`.

```kotlin

val latestBlockId
val payerAddress
val payerAccountKey

var tx = FlowTransaction(
  script = FlowScript(ExamplesUtils.loadScript(scriptName)),
  arguments = listOf(),
  referenceBlockId = latestBlockID,
  gasLimit = gasLimit,
  proposalKey = FlowTransactionProposalKey(
    address = payerAddress,
    keyIndex = payerAccountKey.id,
    sequenceNumber = payerAccountKey.sequenceNumber.toLong()
  ),
  payerAddress = payerAddress,
  authorizers = listOf(payerAddress)
)

val signer = Crypto.getSigner(privateKey, payerAccountKey.hashAlgo)
tx = tx.addEnvelopeSignature(payerAddress, payerAccountKey.id, signer)
```

Check out the **Transaction Signing** example for runnable code snippets in [Java](java-example/src/main/java/org/onflow/examples/java/signTransaction/SignTransactionExample.java) or [Kotlin](kotlin-example/src/main/kotlin/org/onflow/examples/kotlin/signTransaction/SignTransactionExample.kt).

The Transaction Signing example introduces multiple transaction signing paradigms, including:
- Single party, single signature
- Single party, multiple signatures
- Multiple parties
- Multiple parties, 2 authorizers
- Multiple parties, multiple signatures

Before trying these examples, we recommend that you read through the [transaction signature documentation](https://developers.flow.com/build/basics/transactions#signing-a-transaction).

### Sending Transactions

You can submit a transaction to the Flow network using the Access API client.

See the **Send Transaction** example for runnable code snippets in [Java](java-example/src/main/java/org/onflow/examples/java/sendTransaction/SendTransactionExample.java) or [Kotlin](kotlin-example/src/main/kotlin/org/onflow/examples/kotlin/sendTransaction/SendTransactionExample.kt); we include both a simple transaction and a more complex transaction with passed arguments.

### Querying Transaction Results

After you have submitted a transaction, you can query its status by ID:

```kotlin
fun getTransactionResult(txID: FlowId): FlowTransactionResult = when (val response = accessAPI.getTransactionResultById(txID)) {
        is FlowAccessApi.AccessApiCallResponse.Success -> {
            if (response.data.errorMessage.isNotEmpty()) {
                throw Exception(response.data.errorMessage)
            }
            response.data
        }
        is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
    }
```

The result includes a `status` field that will be one of the following values:

- `UNKNOWN` - The transaction has not yet been seen by the network.
- `PENDING` - The transaction has not yet been included in a block.
- `FINALIZED` - The transaction has been included in a block.
- `EXECUTED` - The transaction has been executed but the result has not yet been sealed.
- `SEALED` - The transaction has been executed and the result is sealed in a block.

```kotlin
val txResult = getTransactionResult(txID)
if (txResult.status == FlowTransactionStatus.SEALED) {
  println("Transaction Sealed")
}
```

In the event of a failure, the `FlowAccessApi.AccessApiCallResponse` class returns an `Error` object which contains the corresponding error `message` and throwable `Exception`.

See the **Get Transaction** example for runnable code snippets in [Java](java-example/src/main/java/org/onflow/examples/java/getTransaction/GetTransactionAccessAPIConnector.java) or [Kotlin](kotlin-example/src/main/kotlin/org/onflow/examples/kotlin/getTransaction/GetTransactionAccessAPIConnector.kt).

### Querying Blocks

The Access API exposes `getLatestBlock`, `getBlockById`, and `getBlockByHeight` methods for querying blocks on-chain. See the **Get Blocks** example for runnable code snippets in [Java](java-example/src/main/java/org/onflow/examples/java/getBlock/GetBlockAccessAPIConnector.java) or [Kotlin](kotlin-example/src/main/kotlin/org/onflow/examples/kotlin/getBlock/GetBlockAccessAPIConnector.kt).

A block contains the following fields:

`id` - The ID (hash) of the block.
`parentId` - The ID of the previous block in the chain.
`height` - The height of the block in the chain.
`collectionGuarantees` - The list of collections included in the block.

### Executing a Script

You can use the `simpleFlowScript` method to execute a Cadence script against the latest sealed execution state.

This is an example of a valid script:
```cadence
access(all) fun main(a: Int): Int {
    return a + 10
}
```
```kotlin

fun executeSimpleScript(): FlowScriptResponse {
        val loadedScript = ExamplesUtils.loadScriptContent("cadence/execute_simple_script_example.cdc")

        return accessAPI.simpleFlowScript {
            script { loadedScript }
            arg { JsonCadenceBuilder().int(5) }
        }.let { response ->
            when (response) {
                is FlowAccessApi.AccessApiCallResponse.Success -> response.data
                is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
            }
        }
    }
```

See the **Execute Script** example for runnable code snippets in [Java](java-example/src/main/java/org/onflow/examples/java/executeScript/ExecuteScriptAccessAPIConnector.java) or [Kotlin](kotlin-example/src/main/kotlin/org/onflow/examples/kotlin/executeScript/ExecuteScriptAccessAPIConnector.kt).

### Querying Events

The Access API exposes `getEventsForHeightRange`, `getEventsForBlockIds`, and `getTransactionResultById` methods for querying events on-chain. See the **Get Events** example for runnable code snippets in [Java](java-example/src/main/java/org/onflow/examples/java/getEvent/GetEventAccessAPIConnector.java) or [Kotlin](kotlin-example/src/main/kotlin/org/onflow/examples/kotlin/getEvent/GetEventAccessAPIConnector.kt).

### Querying Accounts

The Access API exposes `getAccountAtLatestBlock` and `getAccountByBlockHeight`methods for querying accounts on-chain. See the **Get Accounts** example for runnable code snippets in [Java](java-example/src/main/java/org/onflow/examples/java/getAccount/GetAccountAccessAPIConnector.java) or [Kotlin](kotlin-example/src/main/kotlin/org/onflow/examples/kotlin/getAccount/GetAccountAccessAPIConnector.kt).

A `FlowAccount` contains the following fields:

`address`: `FlowAddress` - The account address.
`balance`: `BigDecimal` - The account balance.
`contracts`: `Map<String, FlowCode>` - The contracts deployed at this account.
`keys`: `List<FlowAccountKey>` - A list of the public keys associated with this account.

### Examples Summary

For a complete list of supported examples use-cases, see the [Examples Summary](kotlin-example/README.md/#examples-summary).

## Credit

The Flow JVM SDK maintainers have included
* [The NFT Company](https://nftco.com)
   * [@briandilley](https://github.com/briandilley)  
   * [@jereanon](https://github.com/jereanon) 
* [Purple Dash](https://purpledash.dev)
   * [@lealobanov](https://github.com/lealobanov)
 
     
