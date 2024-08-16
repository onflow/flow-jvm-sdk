# Breaking Changes Documentation

This document summarizes the breaking changes introduced in various releases of the repository. Each section corresponds to a specific release version, allowing users to review changes over time.

## v2.0.0

### Overview

This release introduces significant changes that may affect your project. Please review the breaking changes below before upgrading to the latest version of the SDK.

### Breaking Changes

#### 1. Introduction of the AccessApiCallResponse class

- Introduced a Success/Error wrapper for the responses from the `AccessApiImpl` and `AsyncAccessApiImpl` classes
- A detailed error message indicating a reason for the failed Access API call is now included in `Error.message`
- Previously a `null` return was used to indicate a failed call or lack of response; this has now been replaced with `AccessApiCallResponse.Error` in all instances

```shell
sealed class AccessApiCallResponse<out T> {
        data class Success<out T>(val data: T) : AccessApiCallResponse<T>()
        data class Error(val message: String, val throwable: Throwable? = null) : AccessApiCallResponse<Nothing>()
    }
```

#### 2. Refactoring of the ECDSA signing implementation in `Crypto.kt` and `models.kt`

- The `Signer` class no longer implements `Hasher` as `Signer` is not being used to hash any data
- Similarly, `SignerImpl` no longer accepts a Hasher input. The `HashAlgorithm` input is sufficient to derive the hasher implementation.
- `PrivateKey` serialization is now padded to the order size. Deserializing a private key string also requires the input to be padded.
- Fields for the `PrivateKey` and `PublicKey` classes have been updated. `PrivateKey`: `algo` (type `SignatureAlgorithm`) and `publicKey` (type `PublicKey`) have been added, `ecCoupleComponentSize` has been removed. `PublicKey`: `algo` (type `SignatureAlgorithm`) has been added. We anticipate that the `KeyPair` class will be deprecated in upcoming versions as its functionality is now obsolete; it has been left in this release of the SDK to minimize breaking change impact for now.

#### 3. Deprecation of 384-bit signing

- Support for SHA2_384 and SHA3_384 has now been deprecated in the ECDSA signing implementation.

#### 4. Refactoring of the hashing implementation in `Crypto.kt` and `models.kt`

- The `id` field has been removed from the `HashAlgorithm` class; it is now automatically inferred based on the algorithm.

#### 5. Cadence 1.0 support as part of Crescendo migration

- This repository has been upgraded to use Cadence 1.0 and run tests using the latest release of the Flow emulator and Flow CLI. Please ensure you have the Cadence 1.0 CLI installed on your machine (available on your system with the `flow-c1` command) before running tests and examples locally.
___

*Please ensure your projects are compatible with these changes before upgrading to the latest version.*
