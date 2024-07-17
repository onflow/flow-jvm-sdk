# SDK Setup

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

## Example usage

Check out the [kotlin-example](../kotlin-example) and [java-example](../java-example) modules in this repository for examples
of how to use this SDK in your Kotlin or Java application.

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

See [ProjectTestExtensionsTest](/src/intTest/org/onflow/flow/sdk/extensions/ProjectTestExtensionsTest.kt) and
[TestExtensionsTest](/src/intTest/org/onflow/flow/sdk/extensions/TestExtensionsTest.kt) for examples.