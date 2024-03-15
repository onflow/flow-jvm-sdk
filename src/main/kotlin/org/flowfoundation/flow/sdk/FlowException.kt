package org.flowfoundation.flow.sdk

class FlowException : RuntimeException {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)

    private val executionErrorCode: Int? get() = message?.let { parseErrorCode(it) }

    val executionError: FlowError? get() = executionErrorCode?.let { FlowError.forErrorCode(it) }
}
