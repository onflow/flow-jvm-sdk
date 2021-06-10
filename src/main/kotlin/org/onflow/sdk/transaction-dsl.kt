package org.onflow.sdk

import org.onflow.sdk.cadence.Field
import org.onflow.sdk.cadence.JsonCadenceBuilder
import java.util.concurrent.TimeoutException

@Throws(TimeoutException::class)
fun waitForSeal(api: FlowAccessApi, transactionId: FlowId, pauseMs: Long = 500L, timeoutMs: Long = 10_000L): FlowTransactionResult {
    check(pauseMs < timeoutMs) { "pause must be less than timeout" }
    val start = System.currentTimeMillis()
    var ret: FlowTransactionResult
    while (true) {
        ret = checkNotNull(api.getTransactionResultById(transactionId)) { "Transaction with that id not found" }
        if (ret.status == FlowTransactionStatus.SEALED) {
            return ret
        }
        Thread.sleep(pauseMs)
        if (System.currentTimeMillis() - start > timeoutMs) {
            throw TimeoutException("Timeout waiting for seal")
        }
    }
}

fun flowTransaction(block: TransactionBuilder.() -> Unit): FlowTransaction {
    val builder = TransactionBuilder()
    block(builder)
    return builder.build()
}

fun FlowAccessApi.simpleFlowTransaction(address: FlowAddress, signer: Signer, gasLimit: Long = 100, keyIndex: Int = 0, block: TransactionBuilder.() -> Unit): FlowTransactionStub {
    val api = this
    val referenceBlockId = api.getLatestBlockHeader().id
    val payerAccount = checkNotNull(api.getAccountAtLatestBlock(address)) { "Account not found for address: ${address.base16Value}" }

    val builder = TransactionBuilder()
    builder.referenceBlockId = referenceBlockId
    builder.gasLimit = gasLimit
    builder.proposalKey = FlowTransactionProposalKey(
        address = payerAccount.address,
        keyIndex = payerAccount.keys[keyIndex].id,
        sequenceNumber = payerAccount.keys[keyIndex].sequenceNumber.toLong()
    )
    builder.payerAddress = payerAccount.address
    builder.authorizer(payerAccount.address)
    builder.envelopSignature(
        PendingSignature(
            address = payerAccount.address,
            keyIndex = keyIndex,
            signer = signer
        )
    )

    block(builder)

    return FlowTransactionStub(api, builder)
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
            api.sendTransaction(transaction!!)
        } catch (t: Throwable) {
            throw FlowException("Error while executing transaction", t)
        }
        return this
    }

    fun sendAndWaitForSeal(pauseMs: Long = 500L, timeoutMs: Long = 10_000L): FlowTransactionResult {
        return send().waitForSeal(pauseMs, timeoutMs)
    }

    fun getResult(): FlowTransactionResult {
        checkSent()
        return checkNotNull(api.getTransactionResultById(transactionId!!)) { "Transaction wasn't found" }
    }

    fun waitForSeal(pauseMs: Long = 500L, timeoutMs: Long = 10_000L): FlowTransactionResult {
        checkSent()
        return waitForSeal(api, transactionId!!, pauseMs, timeoutMs)
    }
}

class TransactionBuilder {
    var addressRegistry: AddressRegistry = Flow.DEFAULT_ADDRESS_REGISTRY
    private var _chainId: FlowChainId = Flow.DEFAULT_CHAIN_ID
    private var _script: FlowScript? = null
    private var _arguments: MutableList<FlowArgument> = mutableListOf()
    private var _referenceBlockId: FlowId? = null
    private var _gasLimit: Long? = null
    private var _proposalKey: FlowTransactionProposalKey? = null
    private var _payerAddress: FlowAddress? = null
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
    fun script(code: String, chain: FlowChainId = _chainId) = script(FlowScript(addressRegistry.processScript(code, chain)))
    fun script(code: ByteArray, chain: FlowChainId = _chainId) = script(String(code), chain)
    fun script(chain: FlowChainId = _chainId, code: () -> String) = this.script(code(), chain)

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

    var gasLimit: Long
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

    fun proposalKey(proposalKey: FlowTransactionProposalKeyBuilder.() -> Unit) {
        val builder = FlowTransactionProposalKeyBuilder()
        proposalKey(builder)
        this.proposalKey = builder.build()
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

    var payloadSignatures: List<PendingSignature>
        get() { return _payloadSignatures }
        set(value) {
            _payloadSignatures.clear()
            _payloadSignatures.addAll(value)
        }

    fun payloadSignatures(payloadSignatures: MutableList<PendingSignature>) {
        this.payloadSignatures = payloadSignatures
    }
    fun payloadSignatures(payloadSignatures: FlowTransactionSignatureCollectionBuilder.() -> Unit) {
        val builder = FlowTransactionSignatureCollectionBuilder()
        payloadSignatures(builder)
        this.payloadSignatures = builder.build()
    }
    fun payloadSignature(payloadSignature: PendingSignature) = this._payloadSignatures.add(payloadSignature)
    fun payloadSignature(payloadSignature: () -> PendingSignature) = payloadSignature(payloadSignature())
    fun payloadSignature(address: FlowAddress, keyIndex: Int, signature: FlowSignature) {
        payloadSignature(
            PendingSignature(
                address = address,
                keyIndex = keyIndex,
                signature = signature
            )
        )
    }
    fun payloadSignature(address: FlowAddress, keyIndex: Int, signer: Signer) {
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
    fun envelopeSignatures(envelopeSignatures: FlowTransactionSignatureCollectionBuilder.() -> Unit) {
        val builder = FlowTransactionSignatureCollectionBuilder()
        envelopeSignatures(builder)
        this.envelopeSignatures = builder.build()
    }
    fun envelopSignature(envelopSignature: PendingSignature) = this._envelopeSignatures.add(envelopSignature)
    fun envelopSignature(envelopSignature: () -> PendingSignature) = envelopSignature(envelopSignature())
    fun envelopSignature(signature: FlowTransactionSignature) {
        envelopSignature(PendingSignature(prepared = signature))
    }
    fun envelopSignature(address: FlowAddress, keyIndex: Int, signature: FlowSignature) {
        envelopSignature(
            PendingSignature(
                address = address,
                keyIndex = keyIndex,
                signature = signature
            )
        )
    }
    fun envelopSignature(address: FlowAddress, keyIndex: Int, signer: Signer) {
        envelopSignature(
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
            gasLimit = checkNotNull(_gasLimit) { "gasLimit of FlowTransaction is required" },
            proposalKey = checkNotNull(_proposalKey) { "proposalKey of FlowTransaction is required" },
            payerAddress = checkNotNull(_payerAddress) { "payerAddress of FlowTransaction is required" },
            authorizers = _authorizers
        )

        for (pending in payloadSignatures) {
            tx = pending.applyAsPayloadSignature(tx)
        }

        for (pending in envelopeSignatures) {
            tx = pending.applyAsEnvelopeSignature(tx)
        }

        return tx
    }
}

class PendingSignature(
    val prepared: FlowTransactionSignature? = null,
    val address: FlowAddress? = null,
    val keyIndex: Int? = null,
    val signer: Signer? = null,
    val signature: FlowSignature? = null,
) {

    fun applyAsPayloadSignature(tx: FlowTransaction): FlowTransaction {
        return when {
            prepared != null -> {
                tx.copy(
                    payloadSignatures = tx.payloadSignatures + prepared
                ).updateSignerIndices()
            }
            signature != null -> {
                tx.addEnvelopeSignature(
                    address = checkNotNull(address) { "address of FlowTransactionSignature required" },
                    keyIndex = checkNotNull(keyIndex) { "keyIndex of FlowTransactionSignature required" },
                    signature = signature
                )
            }
            signer != null -> {
                tx.addEnvelopeSignature(
                    address = checkNotNull(address) { "address of FlowTransactionSignature required" },
                    keyIndex = checkNotNull(keyIndex) { "keyIndex of FlowTransactionSignature required" },
                    signer = signer,
                    domainTag = DomainTag.TRANSACTION_DOMAIN_TAG
                )
            }
            else -> throw IllegalStateException("One of prepared, signature, or signer must be specified for a payload signature")
        }
    }

    fun applyAsEnvelopeSignature(tx: FlowTransaction): FlowTransaction {
        return when {
            prepared != null -> {
                tx.copy(
                    envelopeSignatures = tx.envelopeSignatures + prepared
                ).updateSignerIndices()
            }
            signature != null -> {
                tx.addEnvelopeSignature(
                    address = checkNotNull(address) { "address of FlowTransactionSignature required" },
                    keyIndex = checkNotNull(keyIndex) { "keyIndex of FlowTransactionSignature required" },
                    signature = signature,
                )
            }
            signer != null -> {
                tx.addEnvelopeSignature(
                    address = checkNotNull(address) { "address of FlowTransactionSignature required" },
                    keyIndex = checkNotNull(keyIndex) { "keyIndex of FlowTransactionSignature required" },
                    signer = signer,
                    domainTag = DomainTag.TRANSACTION_DOMAIN_TAG
                )
            }
            else -> throw IllegalStateException("One of prepared, signature, or signer must be specified for an envelope signature")
        }
    }
}

class FlowArgumentsBuilder {
    private var _values: MutableList<FlowArgument> = mutableListOf()
    fun arg(value: FlowArgument) {
        _values.add(value)
    }
    fun arg(arg: JsonCadenceBuilder.() -> Field<*>) = arg(FlowArgument(arg(JsonCadenceBuilder())))
    fun build(): MutableList<FlowArgument> = _values
}

class FlowTransactionSignatureCollectionBuilder {
    private var _values: MutableList<PendingSignature> = mutableListOf()
    fun signature(value: PendingSignature) {
        _values.add(value)
    }
    fun signature(signature: FlowTransactionSignatureBuilder.() -> Unit) {
        val builder = FlowTransactionSignatureBuilder()
        signature(builder)
        signature(builder.build())
    }
    fun build(): MutableList<PendingSignature> = _values
}

class FlowTransactionSignatureBuilder {
    private var _address: FlowAddress? = null
    private var _keyIndex: Int? = null
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

    var keyIndex: Int
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

    fun build(): PendingSignature {
        return when {
            _signature != null -> {
                PendingSignature(
                    prepared = FlowTransactionSignature(
                        address = checkNotNull(_address) { "address of FlowTransactionSignature required" },
                        signerIndex = -1,
                        keyIndex = checkNotNull(_keyIndex) { "keyIndex of FlowTransactionSignature required" },
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
}

class FlowAddressCollectionBuilder {
    private var _values: MutableList<FlowAddress> = mutableListOf()
    fun address(value: FlowAddress) {
        _values.add(value)
    }
    fun address(payerAddress: String) = address(FlowAddress(payerAddress))
    fun address(payerAddress: ByteArray) = address(FlowAddress.of(payerAddress))
    fun address(payerAddress: () -> FlowAddress) = this.address(payerAddress())
    fun build(): List<FlowAddress> = _values
}

class FlowTransactionProposalKeyBuilder {
    private var _address: FlowAddress? = null
    private var _keyIndex: Int? = null
    private var _sequenceNumber: Long? = null

    var address: FlowAddress
        get() { return _address!! }
        set(value) { _address = value }

    fun address(address: FlowAddress) {
        this.address = address
    }
    fun address(address: String) = address(FlowAddress(address))
    fun address(address: ByteArray) = address(FlowAddress.of(address))
    fun address(address: () -> FlowAddress) = this.address(address())

    var keyIndex: Int
        get() { return _keyIndex!! }
        set(value) { _keyIndex = value }

    fun keyIndex(keyIndex: Number) {
        this.keyIndex = keyIndex.toInt()
    }
    fun keyIndex(keyIndex: () -> Int) = this.keyIndex(keyIndex())

    var sequenceNumber: Long
        get() { return _sequenceNumber!! }
        set(value) { _sequenceNumber = value }

    fun sequenceNumber(sequenceNumber: Number) {
        this.sequenceNumber = sequenceNumber.toLong()
    }
    fun sequenceNumber(sequenceNumber: () -> Int) = this.sequenceNumber(sequenceNumber())

    fun build(): FlowTransactionProposalKey = FlowTransactionProposalKey(
        address = checkNotNull(_address) { "address of FlowTransactionProposalKey required" },
        keyIndex = checkNotNull(_keyIndex) { "keyIndex of FlowTransactionProposalKey required" },
        sequenceNumber = checkNotNull(_sequenceNumber) { "sequenceNumber of FlowTransactionProposalKey required" }
    )
}
