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

## Code examples

Check out the [kotlin-example](../kotlin-example) and [java-example](../java-example) modules in this repository for code examples
of how to use this SDK in your Kotlin or Java application.

## Integration tests

Tests annotated with `FlowEmulatorTest` depend on the [Flow Emulator](https://github.com/onflow/flow-emulator), which requires that the [Flow CLI](https://github.com/onflow/flow-cli) be installed on your machine.

This repository is configured to run with Cadence 1.0. Follow [these steps](https://cadence-lang.org/docs/cadence-migration-guide#install-cadence-10-cli) to install the Cadence 1.0 CLI.

The`FlowEmulatorTest` extension may be used by consumers of this library as well to streamline unit tests that interact
with the FLOW blockchain. The `FlowEmulatorTest` extension uses the local flow emulator to prepare the test environment
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

- `@FlowEmulatorProjectTest` - this uses a `flow.json` file that has your desired configuration in it. We include a functional `flow.json` in this repo (currently used to return tests in GH Actions CICD) which should be sufficient for most use cases.

Also, the following annotations are available in tests as helpers:

- `@FlowTestClient` - used to inject a `FlowAccessApi` or `AsyncFlowAccessApi` instance into your tests
- `@FlowServiceAccountCredentials` - used to inject a `TestAccount` instance into your tests that contain
  the flow service account credentials
- `@FlowTestAccount` - used to automatically create an account in the emulator and inject a `TestAccount` instance
  containing the new account's credentials.

See [ProjectTestExtensionsTest](/../common/src/intTest/kotlin/org/onflow/flow/common/extensions/ProjectTestExtensionsTest.kt) for example usage of the annotation.