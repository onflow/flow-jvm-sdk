package org.onflow.flow.sdk

val ERROR_CODE_REGEX = Regex(".*\\[Error Code: ([0-9]+)].*", RegexOption.DOT_MATCHES_ALL)

fun parseErrorCode(message: String): Int? = message
    .let { ERROR_CODE_REGEX.matchEntire(it) }
    ?.let { it.groupValues[1] }
    ?.toIntOrNull()

enum class FlowError(val code: Int) {
    FLOW_ERROR_InvalidTxByteSizeError(FlowErrorCodeInvalidTxByteSizeError),
    FLOW_ERROR_InvalidReferenceBlockError(FlowErrorCodeInvalidReferenceBlockError),
    FLOW_ERROR_ExpiredTransactionError(FlowErrorCodeExpiredTransactionError),
    FLOW_ERROR_InvalidScriptError(FlowErrorCodeInvalidScriptError),
    FLOW_ERROR_InvalidGasLimitError(FlowErrorCodeInvalidGasLimitError),
    FLOW_ERROR_InvalidProposalSignatureError(FlowErrorCodeInvalidProposalSignatureError),
    FLOW_ERROR_InvalidProposalSeqNumberError(FlowErrorCodeInvalidProposalSeqNumberError),
    FLOW_ERROR_InvalidPayloadSignatureError(FlowErrorCodeInvalidPayloadSignatureError),
    FLOW_ERROR_InvalidEnvelopeSignatureError(FlowErrorCodeInvalidEnvelopeSignatureError),

    FLOW_ERROR_FVMInternalError(FlowErrorCodeFVMInternalError),
    FLOW_ERROR_ValueError(FlowErrorCodeValueError),
    FLOW_ERROR_InvalidArgumentError(FlowErrorCodeInvalidArgumentError),
    FLOW_ERROR_InvalidAddressError(FlowErrorCodeInvalidAddressError),
    FLOW_ERROR_InvalidLocationError(FlowErrorCodeInvalidLocationError),
    FLOW_ERROR_AccountAuthorizationError(FlowErrorCodeAccountAuthorizationError),
    FLOW_ERROR_OperationAuthorizationError(FlowErrorCodeOperationAuthorizationError),
    FLOW_ERROR_OperationNotSupportedError(FlowErrorCodeOperationNotSupportedError),

    FLOW_ERROR_CadenceRunTimeError(FlowErrorCodeCadenceRunTimeError),
    FLOW_ERROR_EncodingUnsupportedValue(FlowErrorCodeEncodingUnsupportedValue),
    FLOW_ERROR_StorageCapacityExceeded(FlowErrorCodeStorageCapacityExceeded),
    FLOW_ERROR_GasLimitExceededError(FlowErrorCodeGasLimitExceededError),
    FLOW_ERROR_EventLimitExceededError(FlowErrorCodeEventLimitExceededError),
    FLOW_ERROR_LedgerIntractionLimitExceededError(FlowErrorCodeLedgerIntractionLimitExceededError),
    FLOW_ERROR_StateKeySizeLimitError(FlowErrorCodeStateKeySizeLimitError),
    FLOW_ERROR_StateValueSizeLimitError(FlowErrorCodeStateValueSizeLimitError),
    FLOW_ERROR_TransactionFeeDeductionFailedError(FlowErrorCodeTransactionFeeDeductionFailedError),

    FLOW_ERROR_AccountNotFoundError(FlowErrorCodeAccountNotFoundError),
    FLOW_ERROR_AccountPublicKeyNotFoundError(FlowErrorCodeAccountPublicKeyNotFoundError),
    FLOW_ERROR_AccountAlreadyExistsError(FlowErrorCodeAccountAlreadyExistsError),
    FLOW_ERROR_FrozenAccountError(FlowErrorCodeFrozenAccountError),

    FLOW_ERROR_ContractNotFoundError(FlowErrorCodeContractNotFoundError),
    FLOW_ERROR_ContractNamesNotFoundError(FlowErrorCodeContractNamesNotFoundError)
    ;

    companion object {
        @JvmStatic
        fun forErrorCode(code: Int): FlowError? = entries
            .find { it.code == code }
    }
}

// tx validation errors 1000 - 1049
// ErrCodeTxValidationError         ErrorCode = 1000 - reserved
const val FlowErrorCodeInvalidTxByteSizeError: Int = 1001
const val FlowErrorCodeInvalidReferenceBlockError: Int = 1002
const val FlowErrorCodeExpiredTransactionError: Int = 1003
const val FlowErrorCodeInvalidScriptError: Int = 1004
const val FlowErrorCodeInvalidGasLimitError: Int = 1005
const val FlowErrorCodeInvalidProposalSignatureError: Int = 1006
const val FlowErrorCodeInvalidProposalSeqNumberError: Int = 1007
const val FlowErrorCodeInvalidPayloadSignatureError: Int = 1008
const val FlowErrorCodeInvalidEnvelopeSignatureError: Int = 1009

// base errors 1050 - 1100
const val FlowErrorCodeFVMInternalError: Int = 1050
const val FlowErrorCodeValueError: Int = 1051
const val FlowErrorCodeInvalidArgumentError: Int = 1052
const val FlowErrorCodeInvalidAddressError: Int = 1053
const val FlowErrorCodeInvalidLocationError: Int = 1054
const val FlowErrorCodeAccountAuthorizationError: Int = 1055
const val FlowErrorCodeOperationAuthorizationError: Int = 1056
const val FlowErrorCodeOperationNotSupportedError: Int = 1057

// execution errors 1100 - 1200
// const val FlowErrorCodeExecutionError: Int = 1100 - reserved
const val FlowErrorCodeCadenceRunTimeError: Int = 1101
const val FlowErrorCodeEncodingUnsupportedValue: Int = 1102
const val FlowErrorCodeStorageCapacityExceeded: Int = 1103
const val FlowErrorCodeGasLimitExceededError: Int = 1104
const val FlowErrorCodeEventLimitExceededError: Int = 1105
const val FlowErrorCodeLedgerIntractionLimitExceededError: Int = 1106
const val FlowErrorCodeStateKeySizeLimitError: Int = 1107
const val FlowErrorCodeStateValueSizeLimitError: Int = 1108
const val FlowErrorCodeTransactionFeeDeductionFailedError: Int = 1109

// accounts errors 1200 - 1250
// const val FlowErrorCodeAccountError: Int = 1200 - reserved
const val FlowErrorCodeAccountNotFoundError: Int = 1201
const val FlowErrorCodeAccountPublicKeyNotFoundError: Int = 1202
const val FlowErrorCodeAccountAlreadyExistsError: Int = 1203
const val FlowErrorCodeFrozenAccountError: Int = 1204

// contract errors 1250 - 1300
// const val FlowErrorCodeContractError: Int = 1250 - reserved
const val FlowErrorCodeContractNotFoundError: Int = 1251
const val FlowErrorCodeContractNamesNotFoundError: Int = 1252
