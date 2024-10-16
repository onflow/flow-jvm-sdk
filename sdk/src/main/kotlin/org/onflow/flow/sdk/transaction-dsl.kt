package org.onflow.flow.sdk

import org.onflow.flow.sdk.cadence.Field
import org.onflow.flow.sdk.cadence.JsonCadenceBuilder
import java.util.concurrent.TimeoutException
import java.util.logging.Logger

@Throws(TimeoutException::class)
fun waitForSeal(api: FlowAccessApi, transactionId: FlowId, pauseMs: Number = 500L, timeoutMs: Number = 10_000L): FlowAccessApi.AccessApiCallResponse<FlowTransactionResult> {
    check(pauseMs.toLong() < timeoutMs.toLong()) { "pause must be less than timeout" }
    val start = System.currentTimeMillis()
    while (true) {
        val ret = when (val result = api.getTransactionResultById(transactionId)) {
            is FlowAccessApi.AccessApiCallResponse.Success -> result.data
            is FlowAccessApi.AccessApiCallResponse.Error -> return FlowAccessApi.AccessApiCallResponse.Error("Failed to retrieve transaction result: ${result.message}", result.throwable)
        }

        if (ret.status == FlowTransactionStatus.SEALED) {
            return FlowAccessApi.AccessApiCallResponse.Success(ret)
        }
        Thread.sleep(pauseMs.toLong())
        if (System.currentTimeMillis() - start > timeoutMs.toLong()) {
            return FlowAccessApi.AccessApiCallResponse.Error("Timeout waiting for seal", TimeoutException("Timeout waiting for seal"))
        }
    }
}

fun flowTransaction(block: TransactionBuilder.() -> Unit): FlowTransaction {
    val builder = TransactionBuilder()
    block(builder)
    return builder.build()
}

fun FlowAccessApi.flowTransaction(referenceBlockId: FlowId? = null, block: TransactionBuilder.() -> Unit): FlowTransactionStub {
    val actualReferenceBlockId = referenceBlockId ?: run {
        when (val result = this.getLatestBlockHeader()) {
            is FlowAccessApi.AccessApiCallResponse.Success -> result.data.id
            is FlowAccessApi.AccessApiCallResponse.Error -> throw IllegalStateException("Failed to retrieve latest block header: ${result.message}", result.throwable)
        }
    }

    val builder = TransactionBuilder(this)
    builder.referenceBlockId = actualReferenceBlockId
    block(builder)
    return FlowTransactionStub(this, builder)
}

fun FlowAccessApi.simpleFlowTransaction(address: FlowAddress, signer: Signer, gasLimit: Number = 100, keyIndex: Number = 0, block: TransactionBuilder.() -> Unit): FlowTransactionStub =
    this.flowTransaction {
        gasLimit(gasLimit)
        proposeAndPay(address, keyIndex, signer)
        block(this)
    }

class FlowTransactionStub(
    private val api: FlowAccessApi,
    private val builder: TransactionBuilder
) {
    var transaction: FlowTransaction? = null
        private set

    var transactionId: FlowId? = null
        private set

    fun checkNotBuilt() = check(transaction == null) { "Transaction already built" }
    fun checkBuilt() = check(transaction != null) { "Transaction not built" }
    fun checkNotSent() = check(transactionId == null) { "Transaction already sent" }
    fun checkSent() = check(transactionId != null) { "Transaction not sent" }

    fun build(): FlowTransactionStub {
        checkNotBuilt()
        transaction = builder.build()
        return this
    }

    fun buildIfNecessary(): FlowTransactionStub {
        if (transaction == null) {
            return build()
        }
        return this
    }

    fun send(): FlowTransactionStub {
        buildIfNecessary()
        checkNotSent()
        transactionId = try {
            when (val result = api.sendTransaction(transaction!!)) {
                is FlowAccessApi.AccessApiCallResponse.Success -> result.data
                is FlowAccessApi.AccessApiCallResponse.Error -> throw FlowException("Error while executing transaction: ${result.message}", result.throwable!!)
            }
        } catch (t: Throwable) {
            throw FlowException("Error while executing transaction", t)
        }
        return this
    }

    fun sendAndWaitForSeal(pauseMs: Number = 500L, timeoutMs: Number = 10_000L): FlowAccessApi.AccessApiCallResponse<FlowTransactionResult> {
        val sendResult = try {
            send()
            FlowAccessApi.AccessApiCallResponse.Success(transactionId!!)
        } catch (e: FlowException) {
            FlowAccessApi.AccessApiCallResponse.Error("Failed to send transaction", e)
        }

        return when (sendResult) {
            is FlowAccessApi.AccessApiCallResponse.Success -> waitForSeal(api, sendResult.data, pauseMs, timeoutMs)
            is FlowAccessApi.AccessApiCallResponse.Error -> sendResult
        }
    }

    fun getResult(): FlowTransactionResult {
        checkSent()
        return when (val result = api.getTransactionResultById(transactionId!!)) {
            is FlowAccessApi.AccessApiCallResponse.Success -> result.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw FlowException("Transaction wasn't found: ${result.message}", result.throwable!!)
        }
    }

    fun waitForSeal(pauseMs: Number = 500L, timeoutMs: Number = 10_000L): FlowTransactionResult {
        checkSent()
        return when (val result = waitForSeal(api, transactionId!!, pauseMs, timeoutMs)) {
            is FlowAccessApi.AccessApiCallResponse.Success -> result.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw IllegalStateException("Failed to wait for seal: ${result.message}", result.throwable)
        }
    }

    fun getResult(
        pauseMs: Number = 500L,
        timeoutMs: Number = 10_000L,
        validStatusCodes: Set<Int> = setOf(0)
    ): Pair<FlowId, FlowTransactionResult> {
        checkSent()
        return transactionId!! to waitForSeal(pauseMs, timeoutMs).throwOnError(validStatusCodes)
    }

    fun sendAndGetResult(
        pauseMs: Number = 500L,
        timeoutMs: Number = 10_000L,
        validStatusCodes: Set<Int> = setOf(0)
    ): Pair<FlowId, FlowTransactionResult> {
        send()
        return getResult(pauseMs, timeoutMs, validStatusCodes)
    }
}

class TransactionBuilder(
    val api: FlowAccessApi? = null
) {
    var addressRegistry: AddressRegistry = Flow.DEFAULT_ADDRESS_REGISTRY
    private var _chainId: FlowChainId = Flow.DEFAULT_CHAIN_ID
    private var _script: FlowScript? = null
    private var _arguments: MutableList<FlowArgument> = mutableListOf()
    private var _referenceBlockId: FlowId? = null
    private var _gasLimit: Number? = null
    private var _proposalKey: FlowTransactionProposalKey? = null
    private var _payerAddress: FlowAddress? = null
    private var _signatures: MutableList<PendingSignature> = mutableListOf()
    private var _authorizers: MutableList<FlowAddress> = mutableListOf()
    private var _payloadSignatures: MutableList<PendingSignature> = mutableListOf()
    private var _envelopeSignatures: MutableList<PendingSignature> = mutableListOf()

    fun addressRegistry(addressRegistry: AddressRegistry) {
        this.addressRegistry = addressRegistry
    }
    fun addressRegistry(block: () -> AddressRegistry) {
        addressRegistry(block())
    }

    var script: FlowScript
        get() { return _script!! }
        set(value) { _script = value }

    fun script(script: FlowScript) {
        this.script = script
    }
    fun script(code: String, chain: FlowChainId = _chainId, addresses: Map<String, FlowAddress> = mapOf()) = script(FlowScript(addressRegistry.processScript(code, chain, addresses)))
    fun script(code: ByteArray, chain: FlowChainId = _chainId, addresses: Map<String, FlowAddress> = mapOf()) = script(String(code), chain, addresses)
    fun script(chain: FlowChainId = _chainId, addresses: Map<String, FlowAddress> = mapOf(), code: () -> String) = this.script(code(), chain, addresses)

    var arguments: MutableList<FlowArgument>
        get() { return _arguments }
        set(value) {
            _arguments.clear()
            _arguments.addAll(value)
        }

    fun arguments(arguments: MutableList<FlowArgument>) {
        this.arguments = arguments
    }
    fun arguments(arguments: FlowArgumentsBuilder.() -> Unit) {
        val builder = FlowArgumentsBuilder()
        arguments(builder)
        this.arguments = builder.build()
    }
    fun argument(argument: FlowArgument) = this._arguments.add(argument)
    fun argument(argument: Field<*>) = this._arguments.add(FlowArgument(argument))
    fun argument(argument: JsonCadenceBuilder.() -> Field<*>) = this.argument(argument(JsonCadenceBuilder()))

    var referenceBlockId: FlowId
        get() { return _referenceBlockId!! }
        set(value) { _referenceBlockId = value }

    fun referenceBlockId(referenceBlockId: FlowId) {
        this.referenceBlockId = referenceBlockId
    }
    fun referenceBlockId(referenceBlockId: String) = referenceBlockId(FlowId(referenceBlockId))
    fun referenceBlockId(referenceBlockId: ByteArray) = referenceBlockId(FlowId.of(referenceBlockId))
    fun referenceBlockId(referenceBlockId: () -> FlowId) = this.referenceBlockId(referenceBlockId())

    var gasLimit: Number
        get() { return _gasLimit!! }
        set(value) { _gasLimit = value }

    fun gasLimit(gasLimit: Number) {
        this.gasLimit = gasLimit.toLong()
    }
    fun gasLimit(gasLimit: () -> Number) = this.gasLimit(gasLimit())

    var chainId: FlowChainId
        get() { return _chainId }
        set(value) { _chainId = value }

    fun chainId(chainId: FlowChainId) {
        this.chainId = chainId
    }
    fun chainId(chainId: () -> FlowChainId) = this.chainId(chainId())

    var proposalKey: FlowTransactionProposalKey
        get() { return _proposalKey!! }
        set(value) { _proposalKey = value }

    fun proposalKey(proposalKey: FlowTransactionProposalKey) {
        this.proposalKey = proposalKey
    }
    fun proposalKey(address: FlowAddress, keyIndex: Number, sequenceNumber: Number) {
        proposalKey(
            FlowTransactionProposalKey(
                address = address,
                keyIndex = keyIndex.toInt(),
                sequenceNumber = sequenceNumber.toLong()
            )
        )
    }
    fun proposalKey(address: FlowAddress, publicKey: String) {
        require(api != null) { "Builder not created with an API instance" }
        val account = when (val result = api.getAccountAtLatestBlock(address)) {
            is FlowAccessApi.AccessApiCallResponse.Success -> result.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw IllegalStateException("Account for address not found: ${result.message}", result.throwable)
        }
        val keyIndex = account.getKeyIndex(publicKey)
        require(keyIndex != -1) { "PublicKey not found for account" }
        proposalKey(
            FlowTransactionProposalKey(
                address = address,
                keyIndex = keyIndex,
                sequenceNumber = account.keys[keyIndex].sequenceNumber.toLong()
            )
        )
    }
    fun proposalKey(address: FlowAddress, keyIndex: Number) {
        require(api != null) { "Builder not created with an API instance" }
        val account = when (val result = api.getAccountAtLatestBlock(address)) {
            is FlowAccessApi.AccessApiCallResponse.Success -> result.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw IllegalStateException("Account for address not found: ${result.message}", result.throwable)
        }
        require(keyIndex.toInt() != -1) { "PublicKey not found for account" }
        require(keyIndex.toInt() < account.keys.size) { "keyIndex out of bounds" }
        proposalKey(
            FlowTransactionProposalKey(
                address = address,
                keyIndex = keyIndex.toInt(),
                sequenceNumber = account.keys[keyIndex.toInt()].sequenceNumber.toLong()
            )
        )
    }
    fun proposalKey(proposalKey: FlowTransactionProposalKeyBuilder.() -> Unit) {
        val builder = FlowTransactionProposalKeyBuilder(this.api)
        proposalKey(builder)
        proposalKey(builder.build())
    }

    fun proposeAndPay(address: FlowAddress, keyIndex: Number, signer: Signer, sequenceNumber: Number) {
        proposalKey(address, keyIndex, sequenceNumber)
        payerAddress(address)
        signature(address, keyIndex, signer)
    }

    fun proposeAndPay(address: FlowAddress, keyIndex: Number, signer: Signer) {
        require(api != null) { "Builder not created with an API instance" }
        val account = when (val result = api.getAccountAtLatestBlock(address)) {
            is FlowAccessApi.AccessApiCallResponse.Success -> result.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw IllegalStateException("Account for address not found: ${result.message}", result.throwable)
        }
        require(keyIndex.toInt() < account.keys.size) { "keyIndex out of bounds" }
        proposeAndPay(address, keyIndex, signer, account.keys[keyIndex.toInt()].sequenceNumber)
    }

    var payerAddress: FlowAddress
        get() { return _payerAddress!! }
        set(value) { _payerAddress = value }
    fun payerAddress(payerAddress: FlowAddress) {
        this.payerAddress = payerAddress
    }
    fun payerAddress(payerAddress: String) = payerAddress(FlowAddress(payerAddress))
    fun payerAddress(payerAddress: ByteArray) = payerAddress(FlowAddress.of(payerAddress))
    fun payerAddress(payerAddress: () -> FlowAddress) = this.payerAddress(payerAddress())

    var authorizers: List<FlowAddress>
        get() { return _authorizers }
        set(value) {
            _authorizers.clear()
            _authorizers.addAll(value)
        }
    fun addAuthorizers(authorizers: MutableList<FlowAddress>) {
        this._authorizers.addAll(authorizers)
    }
    fun authorizers(authorizers: MutableList<FlowAddress>) {
        this.authorizers = authorizers
    }
    fun authorizers(authorizers: FlowAddressCollectionBuilder.() -> Unit) {
        val builder = FlowAddressCollectionBuilder()
        authorizers(builder)
        this.authorizers = builder.build()
    }
    fun authorizer(address: FlowAddress) = this._authorizers.add(address)
    fun authorizer(address: String) = authorizer(FlowAddress(address))
    fun authorizer(address: ByteArray) = authorizer(FlowAddress.of(address))
    fun authorizer(authorizer: () -> FlowAddress) = authorizer(authorizer())

    var signatures: List<PendingSignature>
        get() { return _signatures }
        set(value) {
            _signatures.clear()
            _authorizers.clear()
            value.forEach(this::signature)
        }

    fun addSignatures(signatures: FlowTransactionSignatureCollectionBuilder.() -> Unit) {
        val builder = FlowTransactionSignatureCollectionBuilder()
        signatures(builder)
        builder.build().forEach(this::signature)
    }
    fun signatures(signatures: FlowTransactionSignatureCollectionBuilder.() -> Unit) {
        val builder = FlowTransactionSignatureCollectionBuilder()
        signatures(builder)
        this.signatures = builder.build()
    }
    fun signature(signature: PendingSignature) {
        this._signatures.add(signature)
        if (signature.address != null) {
            this._authorizers.add(signature.address)
        } else if (signature.prepared?.address != null) {
            this._authorizers.add(signature.prepared.address)
        }
    }
    fun signature(signature: () -> PendingSignature) = signature(signature())
    fun signature(address: FlowAddress, keyIndex: Number, signature: FlowSignature) {
        signature(
            PendingSignature(
                address = address,
                keyIndex = keyIndex,
                signature = signature
            )
        )
    }
    fun signature(address: FlowAddress, keyIndex: Number, signer: Signer) {
        signature(
            PendingSignature(
                address = address,
                keyIndex = keyIndex,
                signer = signer
            )
        )
    }

    var payloadSignatures: List<PendingSignature>
        get() { return _payloadSignatures }
        set(value) {
            _payloadSignatures.clear()
            _payloadSignatures.addAll(value)
        }
    fun payloadSignatures(payloadSignatures: MutableList<PendingSignature>) {
        this.payloadSignatures = payloadSignatures
    }
    fun addPayloadSignatures(payloadSignatures: FlowTransactionSignatureCollectionBuilder.() -> Unit) {
        val builder = FlowTransactionSignatureCollectionBuilder()
        payloadSignatures(builder)
        this._payloadSignatures.addAll(builder.build())
    }
    fun payloadSignatures(payloadSignatures: FlowTransactionSignatureCollectionBuilder.() -> Unit) {
        val builder = FlowTransactionSignatureCollectionBuilder()
        payloadSignatures(builder)
        this.payloadSignatures = builder.build()
    }
    fun payloadSignature(payloadSignature: PendingSignature) = this._payloadSignatures.add(payloadSignature)
    fun payloadSignature(payloadSignature: () -> PendingSignature) = payloadSignature(payloadSignature())
    fun payloadSignature(address: FlowAddress, keyIndex: Number, signature: FlowSignature) {
        payloadSignature(
            PendingSignature(
                address = address,
                keyIndex = keyIndex,
                signature = signature
            )
        )
    }
    fun payloadSignature(address: FlowAddress, keyIndex: Number, signer: Signer) {
        payloadSignature(
            PendingSignature(
                address = address,
                keyIndex = keyIndex,
                signer = signer
            )
        )
    }

    var envelopeSignatures: List<PendingSignature>
        get() { return _envelopeSignatures }
        set(value) {
            _envelopeSignatures.clear()
            _envelopeSignatures.addAll(value)
        }
    fun envelopeSignatures(envelopeSignatures: MutableList<PendingSignature>) {
        this.envelopeSignatures = envelopeSignatures
    }
    fun addEnvelopeSignatures(envelopeSignatures: FlowTransactionSignatureCollectionBuilder.() -> Unit) {
        val builder = FlowTransactionSignatureCollectionBuilder()
        envelopeSignatures(builder)
        this._envelopeSignatures.addAll(builder.build())
    }
    fun envelopeSignatures(envelopeSignatures: FlowTransactionSignatureCollectionBuilder.() -> Unit) {
        val builder = FlowTransactionSignatureCollectionBuilder()
        envelopeSignatures(builder)
        this.envelopeSignatures = builder.build()
    }
    fun envelopeSignature(envelopeSignature: PendingSignature) = this._envelopeSignatures.add(envelopeSignature)
    fun envelopeSignature(envelopeSignature: FlowTransactionSignatureBuilder.() -> Unit) {
        val builder = FlowTransactionSignatureBuilder()
        envelopeSignature(builder)
        envelopeSignature(builder.build())
    }
    fun envelopeSignature(signature: FlowTransactionSignature) {
        envelopeSignature(PendingSignature(prepared = signature))
    }
    fun envelopeSignature(address: FlowAddress, keyIndex: Number, signature: FlowSignature) {
        envelopeSignature(
            PendingSignature(
                address = address,
                keyIndex = keyIndex,
                signature = signature
            )
        )
    }
    fun envelopeSignature(address: FlowAddress, keyIndex: Number, signer: Signer) {
        envelopeSignature(
            PendingSignature(
                address = address,
                keyIndex = keyIndex,
                signer = signer
            )
        )
    }

    fun build(): FlowTransaction {
        var tx = FlowTransaction(
            script = checkNotNull(_script) { "script of FlowTransaction is required" },
            arguments = _arguments,
            referenceBlockId = checkNotNull(_referenceBlockId) { "referenceBlockId of FlowTransaction is required" },
            gasLimit = checkNotNull(_gasLimit?.toLong()) { "gasLimit of FlowTransaction is required" },
            proposalKey = checkNotNull(_proposalKey) { "proposalKey of FlowTransaction is required" },
            payerAddress = checkNotNull(_payerAddress) { "payerAddress of FlowTransaction is required" },
            authorizers = _authorizers
        )

        if (signatures.isNotEmpty() && (payloadSignatures.isNotEmpty() || envelopeSignatures.isNotEmpty())) {
            Logger.getLogger(TransactionBuilder::class.qualifiedName).warning(
                "This transaction has both signatures, and payloadSignatures/envelopeSignatures defined. "
                    + "It is not recommended to use these together because it his highly likely that the transaction "
                    + "will be invalid. Instead use either only signatures, or only payloadSignatures/envelopeSignatures "
                    + "when building your transaction"
            )
        }

        if (signatures.isNotEmpty()) {
            for (pending in signatures.filter { it.address != payerAddress }) {
                tx = pending.applyAsPayloadSignature(tx)
            }
            for (pending in signatures.filter { it.address == payerAddress }) {
                tx = pending.applyAsEnvelopeSignature(tx)
            }
        }

        if (payloadSignatures.isNotEmpty() || envelopeSignatures.isNotEmpty()) {
            for (pending in payloadSignatures) {
                tx = pending.applyAsPayloadSignature(tx)
            }
            for (pending in envelopeSignatures) {
                tx = pending.applyAsEnvelopeSignature(tx)
            }
        }
        return tx
    }
}

class PendingSignature(
    val prepared: FlowTransactionSignature? = null,
    val address: FlowAddress? = null,
    val keyIndex: Number? = null,
    val signer: Signer? = null,
    val signature: FlowSignature? = null,
) {
    fun applyAsPayloadSignature(tx: FlowTransaction): FlowTransaction =
        when {
            prepared != null -> {
                tx
                    .copy(
                        payloadSignatures = tx.payloadSignatures + prepared
                    ).updateSignerIndices()
            }
            signature != null -> {
                tx.addPayloadSignature(
                    address = checkNotNull(address) { "address of FlowTransactionSignature required" },
                    keyIndex = checkNotNull(keyIndex?.toInt()) { "keyIndex of FlowTransactionSignature required" },
                    signature = signature
                )
            }
            signer != null -> {
                tx.addPayloadSignature(
                    address = checkNotNull(address) { "address of FlowTransactionSignature required" },
                    keyIndex = checkNotNull(keyIndex?.toInt()) { "keyIndex of FlowTransactionSignature required" },
                    signer = signer
                )
            }
            else -> throw IllegalStateException("One of prepared, signature, or signer must be specified for a payload signature")
        }

    fun applyAsEnvelopeSignature(tx: FlowTransaction): FlowTransaction =
        when {
            prepared != null -> {
                tx
                    .copy(
                        envelopeSignatures = tx.envelopeSignatures + prepared
                    ).updateSignerIndices()
            }
            signature != null -> {
                tx.addEnvelopeSignature(
                    address = checkNotNull(address) { "address of FlowTransactionSignature required" },
                    keyIndex = checkNotNull(keyIndex?.toInt()) { "keyIndex of FlowTransactionSignature required" },
                    signature = signature,
                )
            }
            signer != null -> {
                tx.addEnvelopeSignature(
                    address = checkNotNull(address) { "address of FlowTransactionSignature required" },
                    keyIndex = checkNotNull(keyIndex?.toInt()) { "keyIndex of FlowTransactionSignature required" },
                    signer = signer
                )
            }
            else -> throw IllegalStateException("One of prepared, signature, or signer must be specified for an envelope signature")
        }
}

class FlowArgumentsBuilder {
    private var values: MutableList<FlowArgument> = mutableListOf()
    fun arg(value: FlowArgument) {
        values.add(value)
    }
    fun arg(arg: JsonCadenceBuilder.() -> Field<*>) = arg(FlowArgument(arg(JsonCadenceBuilder())))
    fun build(): MutableList<FlowArgument> = values
}

class FlowTransactionSignatureCollectionBuilder {
    private var values: MutableList<PendingSignature> = mutableListOf()
    fun signature(value: PendingSignature) {
        values.add(value)
    }
    fun signature(signature: FlowTransactionSignatureBuilder.() -> Unit) {
        val builder = FlowTransactionSignatureBuilder()
        signature(builder)
        signature(builder.build())
    }
    fun signature(signature: FlowTransactionSignature) {
        signature(PendingSignature(prepared = signature))
    }
    fun signature(address: FlowAddress, keyIndex: Number, signature: FlowSignature) {
        signature(
            PendingSignature(
                address = address,
                keyIndex = keyIndex,
                signature = signature
            )
        )
    }
    fun signature(address: FlowAddress, keyIndex: Number, signer: Signer) {
        signature(
            PendingSignature(
                address = address,
                keyIndex = keyIndex,
                signer = signer
            )
        )
    }
    fun build(): MutableList<PendingSignature> = values
}

class FlowTransactionSignatureBuilder {
    private var _address: FlowAddress? = null
    private var _keyIndex: Number? = null
    private var _signature: FlowSignature? = null
    private var _signer: Signer? = null

    var address: FlowAddress
        get() { return _address!! }
        set(value) { _address = value }

    fun address(address: FlowAddress) {
        this.address = address
    }

    fun address(address: String) = address(FlowAddress(address))

    fun address(address: ByteArray) = address(FlowAddress.of(address))

    fun address(address: () -> FlowAddress) = this.address(address())

    var keyIndex: Number
        get() { return _keyIndex!! }
        set(value) { _keyIndex = value }

    fun keyIndex(keyIndex: Number) {
        this.keyIndex = keyIndex.toInt()
    }
    fun keyIndex(keyIndex: () -> Int) = this.keyIndex(keyIndex())

    var signature: FlowSignature
        get() { return _signature!! }
        set(value) { _signature = value }

    fun signature(signature: FlowSignature) {
        this.signature = signature
    }

    fun signature(signature: String) = signature(FlowSignature(signature))

    fun signature(signature: ByteArray) = signature(FlowSignature(signature))

    fun signature(signature: () -> FlowSignature) = this.signature(signature())

    var signer: Signer
        get() { return _signer!! }
        set(value) { _signer = value }

    fun signer(signer: Signer) {
        this.signer = signer
    }
    fun signer(signer: () -> Signer) = this.signer(signer())

    fun build(): PendingSignature =
        when {
            _signature != null -> {
                PendingSignature(
                    prepared = FlowTransactionSignature(
                        address = checkNotNull(_address) { "address of FlowTransactionSignature required" },
                        signerIndex = -1,
                        keyIndex = checkNotNull(_keyIndex?.toInt()) { "keyIndex of FlowTransactionSignature required" },
                        signature = checkNotNull(_signature) { "signature of FlowTransactionSignature required" }
                    )
                )
            }
            _signer != null -> {
                PendingSignature(
                    address = checkNotNull(_address) { "address of FlowTransactionSignature required" },
                    keyIndex = checkNotNull(_keyIndex) { "keyIndex of FlowTransactionSignature required" },
                    signer = checkNotNull(_signer) { "signer of FlowTransactionSignature required" }
                )
            }
            else -> throw IllegalArgumentException("one of prepared or signer of FlowTransactionSignature required ")
        }
}

class FlowAddressCollectionBuilder {
    private var values: MutableList<FlowAddress> = mutableListOf()
    fun address(value: FlowAddress) {
        values.add(value)
    }
    fun address(payerAddress: String) = address(FlowAddress(payerAddress))
    fun address(payerAddress: ByteArray) = address(FlowAddress.of(payerAddress))
    fun address(payerAddress: () -> FlowAddress) = this.address(payerAddress())
    fun build(): List<FlowAddress> = values
}

class FlowTransactionProposalKeyBuilder(
    val api: FlowAccessApi? = null
) {
    private var _address: FlowAddress? = null
    private var _keyIndex: Number? = null
    private var _sequenceNumber: Number? = null

    fun usingKeyAtAddress(address: FlowAddress, publicKey: String) {
        require(api != null) { "Builder not created with an API instance" }
        val account = when (val result = api.getAccountAtLatestBlock(address)) {
            is FlowAccessApi.AccessApiCallResponse.Success -> result.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw IllegalStateException("Account for address not found: ${result.message}", result.throwable)
        }
        val keyIndex = account.getKeyIndex(publicKey)
        require(keyIndex != -1) { "PublicKey not found for account" }
        address(address)
        keyIndex(keyIndex)
        sequenceNumber(account.keys[keyIndex].sequenceNumber)
    }

    var address: FlowAddress
        get() { return _address!! }
        set(value) { _address = value }

    fun address(address: FlowAddress) {
        this.address = address
    }
    fun address(address: String) = address(FlowAddress(address))
    fun address(address: ByteArray) = address(FlowAddress.of(address))
    fun address(address: () -> FlowAddress) = this.address(address())

    var keyIndex: Number
        get() { return _keyIndex!! }
        set(value) { _keyIndex = value }

    fun keyIndex(keyIndex: Number) {
        this.keyIndex = keyIndex.toInt()
    }
    fun keyIndex(keyIndex: () -> Int) = this.keyIndex(keyIndex())

    var sequenceNumber: Number
        get() { return _sequenceNumber!! }
        set(value) { _sequenceNumber = value }

    fun sequenceNumber(sequenceNumber: Number) {
        this.sequenceNumber = sequenceNumber.toLong()
    }
    fun sequenceNumber(sequenceNumber: () -> Int) = this.sequenceNumber(sequenceNumber())

    fun build(): FlowTransactionProposalKey = FlowTransactionProposalKey(
        address = checkNotNull(_address) { "address of FlowTransactionProposalKey required" },
        keyIndex = checkNotNull(_keyIndex?.toInt()) { "keyIndex of FlowTransactionProposalKey required" },
        sequenceNumber = checkNotNull(_sequenceNumber?.toLong()) { "sequenceNumber of FlowTransactionProposalKey required" }
    )
}
