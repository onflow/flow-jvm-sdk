# Error Codes

This file documents the list of error code returned from failing transactions and scripts in the SDK. Each error code has an accompanying error message that provides more clarification as to the nature of the error thrown. 

The associated code file for these error codes can be found [here](sdk/src/main/kotlin/org/onflow/flow/sdk/errors.kt).

## Table of Contents
- [Transaction Validation Errors](#transaction-validation-errors-1000---1049)
- [Base Errors](#base-errors-1050---1099)
- [Execution Errors](#execution-errors-1100---1199)
- [Accounts Errors](#accounts-errors-1200---1249)
- [Contracts Errors](#contract-errors-1250---1299)


## Transaction Validation Errors (1000 - 1049)

### [DEPRECATED] 1000: TxValidationError

### [DEPRECATED] 1001: InvalidTxByteSizeError

### [DEPRECATED] 1002: InvalidReferenceBlockError

### [DEPRECATED] 1003: ExpiredTransactionError

### [DEPRECATED] 1004: InvalidScriptError

### [DEPRECATED] 1005: InvalidGasLimitError

### 1006: InvalidProposalSignatureError
```bash
[Error Code: 1006] invalid proposal signature: public key 0 on account xxx does not have a valid signature: signature is not valid
```
### 1007: InvalidProposalSeqNumberError
```bash
[Error Code: 1007] invalid proposal key: public key 0 on account xxx has sequence number xxx, but given xxx
```
### 1008: InvalidPayloadSignatureError
```bash
[Error Code: 1008] invalid payload signature: public key 0 on account xxx does not have a valid signature: signature is not valid
```
### 1009: InvalidEnvelopeSignatureError
```bash
[Error Code: 1009] invalid envelope key: public key 1 on account xxx does not have a valid signature: signature is not valid
```

## Base Errors (1050 - 1099)

### [DEPRECATED] 1050: FVMInternalError

### 1051: ValueError
```bash
[Error Code: 1051] invalid value (xxx): invalid encoded public key value: rlp: expected input list for flow.runtimeAccountPublicKeyWrapper...
```
### 1052: InvalidArgumentError
```bash
[Error Code: 1052] transaction arguments are invalid: (argument is not json decodable: failed to decode value: runtime error: slice bounds out of range [:2] with length 0)
```
### 1053: InvalidAddressError
### 1054: InvalidLocationError
```bash
[Error Code: 1054] location (../contracts/FungibleToken.cdc) is not a valid location: expecting an AddressLocation, but other location types are passed ../contracts/FungibleToken.cdc
```
### 1055: AccountAuthorizationError
```bash
[Error Code: 1055] authorization failed for account e85d442d61a611d8: payer account does not have sufficient signatures (1 < 1000)
```
### 1056: OperationAuthorizationError
```bash
[Error Code: 1056] (RemoveContract) is not authorized: removing contracts requires authorization from specific accounts goroutine 5688834491 [running]:
```
### 1057: OperationNotSupportedError

### 1058: BlockHeightOutOfRangeError

## Execution Errors (1100 - 1199)

### [DEPRECATED] 1100: CodeExecutionError

### 1101: CadenceRunTimeError
```bash
[Error Code: 1101] cadence runtime error Execution failed: error: pre-condition failed: Amount withdrawn must be less than or equal than the balance of the Vault
```

### [DEPRECATED] 1102: EncodingUnsupportedValue

### 1103: StorageCapacityExceeded
```bash
[Error Code: 1103] The account with address (xxx) uses 96559611 bytes of storage which is over its capacity (96554500 bytes). Capacity can be increased by adding FLOW tokens to the account.
```

### [DEPRECATED] 1104: GasLimitExceededError

### 1105: EventLimitExceededError
```bash
[Error Code: 1105] total event byte size (256200) exceeds limit (256000)
```

### 1106: LedgerIntractionLimitExceededError
```bash
[Error Code: 1106] max interaction with storage has exceeded the limit (used: 20276498 bytes, limit 20000000 bytes)
```

### 1107: StateKeySizeLimitError

### 1108: StateValueSizeLimitError

### 1109: TransactionFeeDeductionFailedError

```bash
[Error Code: 1109] failed to deduct 0 transaction fees from 14af75b8c487333c: Execution failed: f919ee77447b7497.FlowFees:97:24

```

### 1110: ComputationLimitExceededError

```bash
[Error Code: 1110] computation exceeds limit (100)
```

### 1111: MemoryLimitExceededError

### 1112: CouldNotDecodeExecutionParameterFromState

### 1113: ScriptExecutionTimedOutError

### 1114: ScriptExecutionCancelledError

### 1115: EventEncodingError

### 1116: InvalidInternalStateAccessError

### 1118: InsufficientPayerBalance

```bash
[Error Code: 1118] payer ... has insufficient balance to attempt transaction execution (required balance: 0.00100000)
```

## Accounts Errors (1200 - 1249)

### 1201: AccountNotFoundError
```bash
[Error Code: 1201] account not found for address xxx
```

### 1202: AccountPublicKeyNotFoundError
```bash
[Error Code: 1202] account public key not found for address xxx and key index 3
```

### 1203: AccountAlreadyExistsError

### [DEPRECATED] 1204: FrozenAccountError

### [DEPRECATED] 1205: AccountStorageNotInitializedError

### 1206: AccountPublicKeyLimitError

## Contract Errors (1250 - 1299)

### [DEPRECATED] 1250: ContractError

### 1251: ContractNotFoundError

### [DEPRECATED] 1252: ContractNamesNotFoundError