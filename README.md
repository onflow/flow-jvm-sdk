# Flow JVM SDK

[![Maven Central](https://img.shields.io/maven-central/v/org.onflow/flow-jvm-sdk)](https://search.maven.org/search?q=g:com.nftco%20AND%20a:flow-jvm-sdk) 
[![Sonatype OSS](https://img.shields.io/nexus/s/org.onflow/flow-jvm-sdk?label=snapshot&server=https%3A%2F%2Fs01.oss.sonatype.org%2F)](https://s01.oss.sonatype.org/content/repositories/snapshots/com/nftco/flow-jvm-sdk/)

The Flow JVM SDK is a library for JVM languages (e.g. Java, Kotlin) that provides
utilities to interact with the Flow blockchain.

At the moment, this SDK includes the following features:
- [x] Communication with the [Flow Access API](https://docs.onflow.org/access-api) over gRPC 
- [x] Transaction preparation and signing
- [x] Cryptographic key generation, parsing, and signing
- [x] Marshalling & unmarshalling of [JSON-Cadence](https://docs.onflow.org/cadence/json-cadence-spec/)
- [x] DSL for creating, signing, and sending transactions and scripts

## Installation

Use the configuration below to add this 
SDK to your project using Maven or Gradle.

### Maven

```xml
<!--
    the following repository is required because the trusted data framework
    is not available on maven central.
 -->
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>

<dependency>
  <groupId>org.onflow</groupId>
  <artifactId>flow-jvm-sdk</artifactId>
  <version>[VERSION HERE]</version>
</dependency>
```

### Gradle

```groovy
repositories {
    ...
    /*
        the following repository is required because the trusted data framework
        is not available on maven central.
    */
    maven { url 'https://jitpack.io' }
}

dependencies {
    api("org.onflow:flow-jvm-sdk:[VERSION HERE]")
}
```

### Gradle (with test extensions)

```groovy
plugins {
    ...
    id("java-test-fixtures")
}

repositories {
    ...
    /*
        the following repository is required because the trusted data framework
        is not available on maven central.
    */
    maven { url 'https://jitpack.io' }
}

dependencies {
    api("org.onflow:flow-jvm-sdk:[VERSION HERE]")
    testFixturesApi(testFixtures("org.onflow:flow-jvm-sdk:[VERSION HERE]"))
}
```

The jitpack.io repository is necessary to access some of the dependencies of this library that are not available on Maven Central.

## SDK Examples

Check out the [example repository](https://github.com/onflow/flow-java-client-example) for an example
of how to use this SDK in a Java or Kotlin application.

## Integrating a Custom Logger with the SDK

The SDK provides a flexible way to integrate your application's existing logging system. This is done through the `SdkConfig` class, which allows you to set a custom logger for the SDK. By default, the SDK uses Logback via SLF4J for its logging configuration. To integrate your custom logger, follow the steps below:

1. **Create or Use an Existing Logger**: Ensure you have an SLF4J `Logger` instance that you want to use for the SDK.

    ```kotlin
    import org.slf4j.Logger
    import org.slf4j.LoggerFactory

    val customLogger: Logger = LoggerFactory.getLogger("MyCustomLogger")
    ```

2. **Set the Custom Logger**: Use the `SdkConfig` class to set the custom logger for the SDK.

    ```kotlin
    import org.onflow.flow.sdk.SdkConfig

    val sdkConfig = SdkConfig()
    sdkConfig.setLogger(customLogger)
    ```

#### Example

Here's a complete example of how to integrate your custom logger with the SDK:

```kotlin
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.onflow.flow.sdk.SdkConfig

fun main() {
    // Create a custom logger
    val customLogger: Logger = LoggerFactory.getLogger("MyCustomLogger")

    // Set the custom logger for the SDK
    val sdkConfig = SdkConfig()
    sdkConfig.setLogger(customLogger)

    // Your SDK-related code here
}
```

## Integration tests

Tests annotated with `FlowEmulatorTest` depend on the [Flow Emulator](https://github.com/onflow/flow-emulator), which is part of the [Flow CLI](https://github.com/onflow/flow-cli) to be installed on your machine.

The`FlowEmulatorTest` extension may be used by consumers of this library as well to streamline unit tests that interact
with the FLOW blockchian. The `FlowEmulatorTest` extension uses the local flow emulator to prepare the test environment
for unit and integration tests. For example:

Setup dependency on the SDK:
```gradle
plugins {
    id("java-test-fixtures")
}

repositories {
    /*
        the following repository is required because the trusted data framework
        is not available on maven central.
    */
    maven { url 'https://jitpack.io' }
}

dependencies {
    api("org.onflow:flow-jvm-sdk:[VERSION HERE]")
    
    // this allows for using the test extension
    testFixturesApi(testFixtures("org.onflow:flow-jvm-sdk:[VERSION HERE]"))
}
```

Write your blockchain tests:
```kotlin
@FlowEmulatorTest
class TransactionTest {

    @FlowTestClient
    lateinit var accessAPI: FlowAccessApi

    @FlowServiceAccountCredentials
    lateinit var serviceAccount: TestAccount

    @Test
    fun `Test something on the emnulator`() {
        val result = accessAPI.simpleFlowTransaction(
            serviceAccount.flowAddress,
            serviceAccount.signer
        ) {
            script {
                """
                    transaction(publicKey: String) {
                        prepare(signer: AuthAccount) {
                            let account = AuthAccount(payer: signer)
                            account.addPublicKey(publicKey.decodeHex())
                        }
                    }
                """
            }
            arguments {
                arg { string(newAccountPublicKey.encoded.bytesToHex()) }
            }
        }.sendAndWaitForSeal()
            .throwOnError()
        assertThat(result.status).isEqualTo(FlowTransactionStatus.SEALED)
    }
    
}
```

There are two ways to test using the emulator:

- `@FlowEmulatorProjectTest` - this uses a `flow.json` file that has your configuration in it
- `@FlowEmulatorTest` - this creates a fresh and temporary flow configuration for each test

Also, the following annotations are available in tests as helpers:

- `@FlowTestClient` - used to inject a `FlowAccessApi` or `AsyncFlowAccessApi` into your tests
- `@FlowServiceAccountCredentials` - used to inject a `TestAccount` instance into your tests that contain
  the flow service account credentials
- `@FlowTestAccount` - used to automatically create an account in the emulator and inject a `TestAccount` instance
  containing the new account's credentials.

See [ProjectTestExtensionsTest](src/test/kotlin/com/nftco/flow/sdk/ProjectTestExtensionsTest.kt) and
[TestExtensionsTest](src/test/kotlin/com/nftco/flow/sdk/TestExtensionsTest.kt) for examples.

## Contribute to this SDK

This project is in the very early phase; all contributions are welcomed.

Read the [contributing guide](https://github.com/the-nft-company/flow-jvm-sdk/blob/main/CONTRIBUTING.md) to get started.

## Dependencies

This SDK requires Java Developer Kit (JDK) 8 or newer.

## Credit

The Flow JVM SDK maintainers have included
* [The NFT Company](https://nftco.com)
   * [@briandilley](https://github.com/briandilley)  
   * [@jereanon](https://github.com/jereanon) 
* [Purple Dash](https://purpledash.dev)
   * [@lealobanov](https://github.com/lealobanov)
 
     
