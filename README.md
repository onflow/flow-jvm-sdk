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

## Repository modules

This repository is organized as a multi-module project, consisting of the following modules:

### SDK
The core module that includes all the necessary tools and libraries to interact with the Flow blockchain. This module provides the main functionalities such as transaction preparation, signing, and interaction with the Flow Access API. 
It also implements and tests use of the SDK via Java Anotations or Kotlin Extensions which can optionally be used when integrating.

### Java Example
This module contains example implementations demonstrating how to use the Flow JVM SDK in a Java application. It includes sample code for various use cases, making it easier for developers to understand and integrate the SDK into their Java projects.

### Kotlin Example
Similar to the Java Example module, this module provides sample implementations in Kotlin. It showcases how to leverage the SDK's capabilities in a Kotlin environment.

## Contribute to this SDK

We welcome all community contributions and will gladly review improvements and other proposals as PRs.

Read the [contributing guide](https://github.com/onflow/flow-jvm-sdk/blob/main/CONTRIBUTING.md) to get started.

## Dependencies

This SDK requires Java Developer Kit (JDK) 8 or newer.

## Credit

The Flow JVM SDK maintainers have included
* [The NFT Company](https://nftco.com)
   * [@briandilley](https://github.com/briandilley)  
   * [@jereanon](https://github.com/jereanon) 
* [Purple Dash](https://purpledash.dev)
   * [@lealobanov](https://github.com/lealobanov)
 
     
