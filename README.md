# Flow JVM SDK

[![Maven Central](https://img.shields.io/maven-central/v/org.onflow/flow-jvm-sdk)](https://search.maven.org/search?q=g:org.onflow%20AND%20a:flow-jvm-sdk)

> :warning: This is an alpha release not yet intended for production use. Functionality may change, please use at your own risk.

The Flow JVM SDK is a library for JVM languages (e.g. Java, Kotlin) that provides
utilities to interact with the Flow blockchain.

At the moment, this SDK includes the following features:
- [x] Communication with the [Flow Access API](https://docs.onflow.org/access-api) over gRPC 
- [x] Transaction preparation and signing
- [x] Cryptographic key generation, parsing, and signing
- [x] Events parsing 

## Installation

To add this SDK to your project using Maven, use the following:

Maven:
```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>

<dependency>
  <groupId>org.onflow</groupId>
  <artifactId>flow-jvm-sdk</artifactId>
  <version>0.2.2</version>
</dependency>
```

Gradle:
```groovy
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}

dependencies {
  api("org.onflow:flow-jvm-sdk:0.2.2")
}
```

The jitpack.io repository is necesssary to access some of the dependencies of this library that are not available on maven central.

## Example Usage

Check out the [example repository](https://github.com/onflow/flow-java-client-example) for an example
of how to use this SDK in a Java application.

### Flow Integration Tests
Tests annotated with `FlowEmulatorTest` depend on the [Flow Emulator](https://github.com/onflow/flow-emulator), which is part of the [Flow CLI](https://github.com/onflow/flow-cli) to be installed on your machine.

## Contribution

Project is in the very early phase, all contributions are welcomed.

Read the [contributing guide](https://github.com/onflow/flow-jvm-sdk/blob/main/CONTRIBUTING.md) to get started.

## Dependencies

Java Developer Kit (JDK) 8 or newer.
