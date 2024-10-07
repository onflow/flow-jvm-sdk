package org.onflow.flow.sdk

import com.google.protobuf.ByteString
import com.google.protobuf.UnsafeByteOperations
import org.onflow.flow.sdk.cadence.EventField
import org.onflow.flow.sdk.cadence.Field
import org.onflow.protobuf.access.Access
import org.onflow.protobuf.entities.*
import org.tdf.rlp.RLP
import org.tdf.rlp.RLPCodec
import java.io.Serializable
import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDateTime

private const val FLOW_ID_SIZE_BYTES = 32
private const val FLOW_ADDRESS_SIZE_BYTES = 8

enum class FlowTransactionStatus(
    val num: Int
) {
    UNKNOWN(0),
    PENDING(1),
    FINALIZED(2),
    EXECUTED(3),
    SEALED(4),
    EXPIRED(5);

    companion object {
        @JvmStatic
        fun of(num: Int): FlowTransactionStatus = entries
            .find { it.num == num }
            ?: throw IllegalArgumentException("Unknown TransactionStatus: $num")
    }
}

enum class FlowChainId(
    val id: String
) {
    UNKNOWN("unknown"),
    MAINNET("flow-mainnet"),
    TESTNET("flow-testnet"),
    CANARYNET("flow-canarynet"),
    EMULATOR("flow-emulator");

    companion object {
        @JvmStatic
        fun of(id: String): FlowChainId = entries
            .find { it.id == id }
            ?: UNKNOWN
    }
}

enum class SignatureAlgorithm(
    val algorithm: String,
    val curve: String,
    val id: String,
    // code is the access API code defined in https://developers.flow.com/build/basics/accounts#signature-and-hash-algorithms
    val code: Int,
    // index is the Cadence index defined in https://cadence-lang.org/docs/language/crypto#signing-algorithms
    val index: Int
) {
    UNKNOWN("unknown", "unknown", "unknown", -1, 0),
    ECDSA_P256("ECDSA", "P-256", "ECDSA_P256", 2, 1),
    ECDSA_SECP256k1("ECDSA", "secp256k1", "ECDSA_secp256k1", 3, 2);

    companion object {
        @JvmStatic
        fun fromCode(code: Int): SignatureAlgorithm = entries
            .find { it.code == code } ?: UNKNOWN

        @JvmStatic
        fun fromCadenceIndex(index: Int): SignatureAlgorithm = entries
            .find { it.index == index } ?: UNKNOWN
    }
}

enum class HashAlgorithm(
    val algorithm: String,
    val outputSize: Int,
    // code is the access API code defined in https://developers.flow.com/build/basics/accounts#signature-and-hash-algorithms
    val code: Int,
    // index is the Cadence index defined in https://cadence-lang.org/docs/language/crypto#hashing
    val index: Int
) {
    UNKNOWN("unknown", -1, -1, 0),
    SHA2_256("SHA-256", 256, 1, 1),
    SHA2_384("SHA-384", 384, -1, 2),
    SHA3_256("SHA3-256", 256, 3, 3),
    SHA3_384("SHA3-384", 384, -1, 4),
    KMAC128("KMAC128", 256, -1, 5),
    KECCAK256("KECCAK256", 256, -1, 6);

    companion object {
        @JvmStatic
        fun fromCode(code: Int): HashAlgorithm = entries.find { it.code == code } ?: UNKNOWN

        @JvmStatic
        fun fromCadenceIndex(index: Int): HashAlgorithm = entries.find { it.index == index } ?: UNKNOWN
    }
}

interface Signer {
    fun sign(bytes: ByteArray): ByteArray

    fun signWithDomain(bytes: ByteArray, domain: ByteArray): ByteArray = sign(domain + bytes)

    fun signAsUser(bytes: ByteArray): ByteArray = signWithDomain(bytes, DomainTag.USER_DOMAIN_TAG)

    fun signAsTransaction(bytes: ByteArray): ByteArray = signWithDomain(bytes, DomainTag.TRANSACTION_DOMAIN_TAG)
}

interface Hasher {
    fun hash(bytes: ByteArray): ByteArray
    fun hashAsHexString(bytes: ByteArray): String = hash(bytes).bytesToHex()
}

data class FlowAccount(
    val address: FlowAddress,
    val balance: BigDecimal,
    @Deprecated(
        message = "use contracts instead",
        replaceWith = ReplaceWith("contracts")
    )
    val code: FlowCode,
    val keys: List<FlowAccountKey>,
    val contracts: Map<String, FlowCode>
) : Serializable {
    companion object {
        @JvmStatic
        fun of(value: AccountOuterClass.Account): FlowAccount = FlowAccount(
            address = FlowAddress.of(value.address.toByteArray()),
            balance = BigDecimal(java.lang.Long.toUnsignedString(value.balance)).movePointLeft(8),
            code = FlowCode(value.code.toByteArray()),
            keys = value.keysList.map { FlowAccountKey.of(it) },
            contracts = value.contractsMap.mapValues { FlowCode(it.value.toByteArray()) }
        )
    }

    @JvmOverloads
    @Suppress("DEPRECATION")
    fun builder(builder: AccountOuterClass.Account.Builder = AccountOuterClass.Account.newBuilder()): AccountOuterClass.Account.Builder = builder
        .setAddress(address.byteStringValue)
        .setBalance(balance.movePointRight(8).toLong())
        .setCode(code.byteStringValue)
        .addAllKeys(keys.map { it.builder().build() })
        .putAllContracts(contracts.mapValues { it.value.byteStringValue })

    /**
     * Returns the index of the public key on the account, or -1 if not found.
     */
    fun getKeyIndex(publicKey: String): Int = this.keys
        .filter { !it.revoked }
        .find {
            it.publicKey.base16Value
                .lowercase()
                .endsWith(publicKey.lowercase())
                ||
                publicKey.lowercase().endsWith(it.publicKey.base16Value.lowercase())
        }?.id
        ?: -1
}

data class FlowAccountKey(
    val id: Int = -1,
    val publicKey: FlowPublicKey,
    val signAlgo: SignatureAlgorithm,
    val hashAlgo: HashAlgorithm,
    val weight: Int,
    val sequenceNumber: Int = -1,
    val revoked: Boolean = false
) : Serializable {
    companion object {
        @JvmStatic
        fun of(value: AccountOuterClass.AccountKey): FlowAccountKey = FlowAccountKey(
            id = value.index,
            publicKey = FlowPublicKey(value.publicKey.toByteArray()),
            signAlgo = SignatureAlgorithm.fromCode(value.signAlgo),
            hashAlgo = HashAlgorithm.fromCode(value.hashAlgo),
            weight = value.weight,
            sequenceNumber = value.sequenceNumber,
            revoked = value.revoked
        )
    }

    @JvmOverloads
    fun builder(builder: AccountOuterClass.AccountKey.Builder = AccountOuterClass.AccountKey.newBuilder()): AccountOuterClass.AccountKey.Builder = builder
        .setIndex(id)
        .setPublicKey(publicKey.byteStringValue)
        .setSignAlgo(signAlgo.code)
        .setHashAlgo(hashAlgo.code)
        .setWeight(weight)
        .setSequenceNumber(sequenceNumber)
        .setRevoked(revoked)

    val encoded: ByteArray get() = RLPCodec.encode(
        arrayOf(
            publicKey.bytes,
            signAlgo.code,
            hashAlgo.code,
            weight
        )
    )
}

data class FlowEventResult(
    val blockId: FlowId,
    val blockHeight: Long,
    val blockTimestamp: LocalDateTime,
    val events: List<FlowEvent>
) : Serializable {
    companion object {
        @JvmStatic
        fun of(value: Access.EventsResponse.Result): FlowEventResult = FlowEventResult(
            blockId = FlowId.of(value.blockId.toByteArray()),
            blockHeight = value.blockHeight,
            blockTimestamp = value.blockTimestamp.asLocalDateTime(),
            events = value.eventsList.map { FlowEvent.of(it) }
        )
    }

    @JvmOverloads
    fun builder(builder: Access.EventsResponse.Result.Builder = Access.EventsResponse.Result.newBuilder()): Access.EventsResponse.Result.Builder = builder
        .setBlockId(blockId.byteStringValue)
        .setBlockHeight(blockHeight)
        .setBlockTimestamp(blockTimestamp.asTimestamp())
        .addAllEvents(events.map { it.builder().build() })
}

// https://github.com/onflow/flow-go-sdk/blob/878e5e586e0f060b88c6036cf4b0f6a7ab66d198/client/client.go#L515
data class FlowEvent(
    val type: String,
    val transactionId: FlowId,
    val transactionIndex: Int,
    val eventIndex: Int,
    val payload: FlowEventPayload
) : Serializable {
    companion object {
        @JvmStatic
        fun of(value: EventOuterClass.Event): FlowEvent = FlowEvent(
            type = value.type,
            transactionId = FlowId.of(value.transactionId.toByteArray()),
            transactionIndex = value.transactionIndex,
            eventIndex = value.eventIndex,
            payload = FlowEventPayload(value.payload.toByteArray())
        )
    }

    val id: String get() = event.id!!
    val event: EventField get() = payload.jsonCadence as EventField

    private fun <T : Field<*>> getField(name: String): T? = event[name]
    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(name: String): T? = getField<Field<*>>(name) as T
    operator fun contains(name: String): Boolean = name in event

    @JvmOverloads
    fun builder(builder: EventOuterClass.Event.Builder = EventOuterClass.Event.newBuilder()): EventOuterClass.Event.Builder = builder
        .setType(type)
        .setTransactionId(transactionId.byteStringValue)
        .setTransactionIndex(transactionIndex)
        .setEventIndex(eventIndex)
        .setPayload(payload.byteStringValue)
}

data class FlowTransactionResult(
    val status: FlowTransactionStatus,
    val statusCode: Int,
    val errorMessage: String,
    val events: List<FlowEvent>,
    val blockId: FlowId,
    val blockHeight: Long,
    val transactionId: FlowId,
    val collectionId: FlowId,
    val computationUsage: Long
) : Serializable {
    companion object {
        @JvmStatic
        fun of(value: Access.TransactionResultResponse): FlowTransactionResult = FlowTransactionResult(
            status = FlowTransactionStatus.of(value.statusValue),
            statusCode = value.statusCode,
            errorMessage = value.errorMessage,
            events = value.eventsList.map { FlowEvent.of(it) },
            blockId = FlowId.of(value.blockId.toByteArray()),
            blockHeight = value.blockHeight,
            transactionId = FlowId.of(value.transactionId.toByteArray()),
            collectionId = FlowId.of(value.collectionId.toByteArray()),
            computationUsage = value.computationUsage
        )
    }

    @JvmOverloads
    fun builder(builder: Access.TransactionResultResponse.Builder = Access.TransactionResultResponse.newBuilder()): Access.TransactionResultResponse.Builder = builder
        .setStatus(TransactionOuterClass.TransactionStatus.valueOf(status.name))
        .setStatusCode(statusCode)
        .setErrorMessage(errorMessage)
        .addAllEvents(events.map { it.builder().build() })

    @JvmOverloads
    fun throwOnError(validStatusCodes: Set<Int> = setOf(0)): FlowTransactionResult {
        if (statusCode !in validStatusCodes) {
            throw FlowException("Transaction failed with code $statusCode:\n$errorMessage")
        }
        return this
    }

    @JvmOverloads
    fun getEventsOfType(type: String, exact: Boolean = false, expectedCount: Int? = null): List<EventField> {
        val ret = this.events
            .filter { if (exact) { it.type == type } else { it.type.endsWith(type) } }
            .map { it.event }
        check(expectedCount == null || ret.size == expectedCount) { "Expected $expectedCount events of type $type but there were ${ret.size}" }
        return ret
    }
}

internal class Payload(
    @RLP(0) val script: ByteArray,
    @RLP(1) val arguments: List<ByteArray>,
    @RLP(2) val referenceBlockId: ByteArray,
    @RLP(3) val gasLimit: Long,
    @RLP(4) val proposalKeyAddress: ByteArray,
    @RLP(5) val proposalKeyIndex: Long,
    @RLP(6) val proposalKeySequenceNumber: Long,
    @RLP(7) val payer: ByteArray,
    @RLP(8) val authorizers: List<ByteArray>
) {
    // no-arg constructor required for decoding
    constructor() : this(byteArrayOf(), listOf(), byteArrayOf(), 0, byteArrayOf(), 0, 0, byteArrayOf(), listOf())
}

internal class PayloadEnvelope(
    @RLP(0) val payload: Payload,
    @RLP(1) val payloadSignatures: List<EnvelopeSignature>
)

internal class PaymentEnvelope(
    @RLP(0) val payloadEnvelope: PayloadEnvelope,
    @RLP(1) val envelopeSignatures: List<EnvelopeSignature>
)

internal class TransactionEnvelope(
    @RLP(0) val payload: Payload = Payload(),
    @RLP(1) val payloadSignatures: List<EnvelopeSignature> = emptyList(),
    @RLP(2) val envelopeSignatures: List<EnvelopeSignature> = emptyList()
)

internal class EnvelopeSignature(
    @RLP(0) val signerIndex: Int,
    @RLP(1) val keyIndex: Int,
    @RLP(2) val signature: ByteArray
) {
    // no-arg constructor required for decoding
    constructor() : this(0, 0, byteArrayOf())
}

data class FlowTransaction(
    val script: FlowScript,
    val arguments: List<FlowArgument>,
    val referenceBlockId: FlowId,
    val gasLimit: Long,
    val proposalKey: FlowTransactionProposalKey,
    val payerAddress: FlowAddress,
    val authorizers: List<FlowAddress>,
    val payloadSignatures: List<FlowTransactionSignature> = emptyList(),
    val envelopeSignatures: List<FlowTransactionSignature> = emptyList()
) : Serializable {
    private val payload: Payload
        get() = Payload(
            script = script.bytes,
            arguments = arguments.map { it.bytes },
            referenceBlockId = referenceBlockId.bytes,
            gasLimit = gasLimit,
            proposalKeyAddress = proposalKey.address.bytes,
            proposalKeyIndex = proposalKey.keyIndex.toLong(), // TODO: type mismatch here
            proposalKeySequenceNumber = proposalKey.sequenceNumber,
            payer = payerAddress.bytes,
            authorizers = authorizers.map { it.bytes }
        )

    private val authorization: PayloadEnvelope
        get() = PayloadEnvelope(
            payload = payload,
            payloadSignatures = payloadSignatures.map {
                EnvelopeSignature(
                    signerIndex = it.signerIndex,
                    keyIndex = it.keyIndex,
                    signature = it.signature.bytes
                )
            }
        )

    private val payment: PaymentEnvelope
        get() = PaymentEnvelope(
            payloadEnvelope = authorization,
            envelopeSignatures = envelopeSignatures.map {
                EnvelopeSignature(
                    signerIndex = it.signerIndex,
                    keyIndex = it.keyIndex,
                    signature = it.signature.bytes
                )
            }
        )

    private val transaction: TransactionEnvelope
        get() = TransactionEnvelope(
            payload = payload,
            payloadSignatures = payloadSignatures.map {
                EnvelopeSignature(
                    signerIndex = it.signerIndex,
                    keyIndex = it.keyIndex,
                    signature = it.signature.bytes
                )
            },
            envelopeSignatures = envelopeSignatures.map {
                EnvelopeSignature(
                    signerIndex = it.signerIndex,
                    keyIndex = it.keyIndex,
                    signature = it.signature.bytes
                )
            }
        )

    val canonicalPayload: ByteArray get() = RLPCodec.encode(payload)
    val canonicalAuthorizationEnvelope: ByteArray get() = RLPCodec.encode(authorization)
    val canonicalPaymentEnvelope: ByteArray get() = RLPCodec.encode(payment)
    val canonicalTransaction: ByteArray get() = RLPCodec.encode(transaction)
    val id: FlowId get() = FlowId.of(canonicalTransaction.sha3256Hash())

    val signerList: List<FlowAddress>
        get() {
            val ret = mutableListOf<FlowAddress>()
            val seen = mutableSetOf<FlowAddress>()
            val addSigner = fun(address: FlowAddress) {
                if (address in seen) {
                    return
                }
                ret.add(address)
                seen.add(address)
            }
            addSigner(proposalKey.address)
            addSigner(payerAddress)
            authorizers.forEach(addSigner)
            return ret
        }

    val signerMap: Map<FlowAddress, Int> get() {
        return signerList
            .withIndex()
            .map { it.value to it.index }
            .toMap()
    }

    companion object {
        @JvmStatic
        fun of(value: TransactionOuterClass.Transaction): FlowTransaction = FlowTransaction(
            script = FlowScript(value.script.toByteArray()),
            arguments = value.argumentsList.map { FlowArgument(it.toByteArray()) },
            referenceBlockId = FlowId.of(value.referenceBlockId.toByteArray()),
            gasLimit = value.gasLimit,
            proposalKey = FlowTransactionProposalKey.of(value.proposalKey),
            payerAddress = FlowAddress.of(value.payer.toByteArray()),
            authorizers = value.authorizersList.map { FlowAddress.of(it.toByteArray()) },
            payloadSignatures = value.payloadSignaturesList.map { FlowTransactionSignature.of(it) },
            envelopeSignatures = value.envelopeSignaturesList.map { FlowTransactionSignature.of(it) }
        )
        @JvmStatic
        fun of(bytes: ByteArray): FlowTransaction {
            val txEnvelope: TransactionEnvelope = RLPCodec.decode(bytes, TransactionEnvelope::class.java)
            var tx = FlowTransaction(
                script = FlowScript(txEnvelope.payload.script),
                arguments = txEnvelope.payload.arguments.map { FlowArgument(it) },
                referenceBlockId = FlowId.of(txEnvelope.payload.referenceBlockId),
                gasLimit = txEnvelope.payload.gasLimit,
                proposalKey = FlowTransactionProposalKey(
                    FlowAddress.of(txEnvelope.payload.proposalKeyAddress),
                    txEnvelope.payload.proposalKeyIndex.toInt(),
                    txEnvelope.payload.proposalKeySequenceNumber
                ),
                payerAddress = FlowAddress.of(txEnvelope.payload.payer),
                authorizers = txEnvelope.payload.authorizers.map { FlowAddress.of(it) }
            )
            txEnvelope.payloadSignatures.map {
                tx = tx.addPayloadSignature(tx.signerList[it.signerIndex], it.keyIndex, FlowSignature(it.signature))
            }
            txEnvelope.envelopeSignatures.map {
                tx = tx.addEnvelopeSignature(tx.signerList[it.signerIndex], it.keyIndex, FlowSignature(it.signature))
            }
            return tx
        }
    }

    @JvmOverloads
    fun builder(builder: TransactionOuterClass.Transaction.Builder = TransactionOuterClass.Transaction.newBuilder()): TransactionOuterClass.Transaction.Builder = builder
        .setScript(script.byteStringValue)
        .addAllArguments(arguments.map { it.byteStringValue })
        .setReferenceBlockId(referenceBlockId.byteStringValue)
        .setGasLimit(gasLimit)
        .setProposalKey(proposalKey.builder().build())
        .setPayer(payerAddress.byteStringValue)
        .addAllAuthorizers(authorizers.map { it.byteStringValue })
        .addAllPayloadSignatures(payloadSignatures.map { it.builder().build() })
        .addAllEnvelopeSignatures(envelopeSignatures.map { it.builder().build() })

    fun addPayloadSignature(address: FlowAddress, keyIndex: Int, signer: Signer): FlowTransaction = addPayloadSignature(address, keyIndex, FlowSignature(signer.signAsTransaction(canonicalPayload)))

    fun addPayloadSignature(address: FlowAddress, keyIndex: Int, signature: FlowSignature): FlowTransaction {
        val payloadSignatures = this.payloadSignatures.toMutableList()
        payloadSignatures.add(
            FlowTransactionSignature(
                address = address,
                signerIndex = signerMap[address] ?: -1,
                keyIndex = keyIndex,
                signature = signature
            )
        )
        return this
            .copy(
                payloadSignatures = payloadSignatures.sortedWith(compareBy<FlowTransactionSignature> { it.signerIndex }.thenBy { it.keyIndex })
            ).updateSignerIndices()
    }

    fun addEnvelopeSignature(address: FlowAddress, keyIndex: Int, signer: Signer): FlowTransaction = addEnvelopeSignature(address, keyIndex, FlowSignature(signer.signAsTransaction(canonicalAuthorizationEnvelope)))

    fun addEnvelopeSignature(address: FlowAddress, keyIndex: Int, signature: FlowSignature): FlowTransaction {
        val envelopeSignatures = this.envelopeSignatures.toMutableList()
        envelopeSignatures.add(
            FlowTransactionSignature(
                address = address,
                signerIndex = signerMap[address] ?: -1,
                keyIndex = keyIndex,
                signature = signature
            )
        )
        return this
            .copy(
                envelopeSignatures = envelopeSignatures.sortedWith(compareBy<FlowTransactionSignature> { it.signerIndex }.thenBy { it.keyIndex })
            ).updateSignerIndices()
    }

    fun updateSignerIndices(): FlowTransaction {
        val map = signerMap
        val payloadSignatures = this.payloadSignatures.toMutableList()
        for ((i, sig) in payloadSignatures.withIndex()) {
            if (map.containsKey(sig.address)) {
                continue
            }
            payloadSignatures[i] = payloadSignatures[i].copy(
                signerIndex = i
            )
        }
        val envelopeSignatures = this.envelopeSignatures.toMutableList()
        for ((i, sig) in envelopeSignatures.withIndex()) {
            if (map.containsKey(sig.address)) {
                continue
            }
            envelopeSignatures[i] = envelopeSignatures[i].copy(
                signerIndex = i
            )
        }
        return this.copy(
            payloadSignatures = payloadSignatures,
            envelopeSignatures = envelopeSignatures
        )
    }
}

data class FlowTransactionProposalKey(
    val address: FlowAddress,
    val keyIndex: Int,
    val sequenceNumber: Long
) : Serializable {
    companion object {
        @JvmStatic
        fun of(value: TransactionOuterClass.Transaction.ProposalKey): FlowTransactionProposalKey =
            FlowTransactionProposalKey(
                address = FlowAddress.of(value.address.toByteArray()),
                keyIndex = value.keyId,
                sequenceNumber = value.sequenceNumber
            )
    }

    @JvmOverloads
    fun builder(builder: TransactionOuterClass.Transaction.ProposalKey.Builder = TransactionOuterClass.Transaction.ProposalKey.newBuilder()): TransactionOuterClass.Transaction.ProposalKey.Builder = builder
        .setAddress(address.byteStringValue)
        .setKeyId(keyIndex)
        .setSequenceNumber(sequenceNumber)
}

data class FlowTransactionSignature(
    val address: FlowAddress,
    val signerIndex: Int,
    val keyIndex: Int,
    val signature: FlowSignature
) : Serializable {
    companion object {
        @JvmStatic
        fun of(value: TransactionOuterClass.Transaction.Signature): FlowTransactionSignature =
            FlowTransactionSignature(
                address = FlowAddress.of(value.address.toByteArray()),
                signerIndex = value.keyId,
                keyIndex = value.keyId,
                signature = FlowSignature(value.signature.toByteArray())
            )
    }
    @JvmOverloads
    fun builder(builder: TransactionOuterClass.Transaction.Signature.Builder = TransactionOuterClass.Transaction.Signature.newBuilder()): TransactionOuterClass.Transaction.Signature.Builder = builder
        .setAddress(address.byteStringValue)
        .setKeyId(keyIndex)
        .setSignature(signature.byteStringValue)
}

data class FlowBlockHeader(
    val id: FlowId,
    val parentId: FlowId,
    val height: Long
) : Serializable {
    companion object {
        @JvmStatic
        fun of(value: BlockHeaderOuterClass.BlockHeader): FlowBlockHeader = FlowBlockHeader(
            id = FlowId.of(value.id.toByteArray()),
            parentId = FlowId.of(value.parentId.toByteArray()),
            height = value.height
        )
    }

    @JvmOverloads
    fun builder(builder: BlockHeaderOuterClass.BlockHeader.Builder = BlockHeaderOuterClass.BlockHeader.newBuilder()): BlockHeaderOuterClass.BlockHeader.Builder = builder
        .setId(id.byteStringValue)
        .setParentId(parentId.byteStringValue)
        .setHeight(height)
}

data class FlowBlock(
    val id: FlowId,
    val parentId: FlowId,
    val height: Long,
    val timestamp: LocalDateTime,
    val collectionGuarantees: List<FlowCollectionGuarantee>,
    val blockSeals: List<FlowBlockSeal>,
    val signatures: List<FlowSignature>,
) : Serializable {
    companion object {
        @JvmStatic
        fun of(value: BlockOuterClass.Block) = FlowBlock(
            id = FlowId.of(value.id.toByteArray()),
            parentId = FlowId.of(value.parentId.toByteArray()),
            height = value.height,
            timestamp = value.timestamp.asLocalDateTime(),
            collectionGuarantees = value.collectionGuaranteesList.map { FlowCollectionGuarantee.of(it) },
            blockSeals = value.blockSealsList.map { FlowBlockSeal.of(it) },
            signatures = value.signaturesList.map { FlowSignature(it.toByteArray()) },
        )
    }

    @JvmOverloads
    fun builder(builder: BlockOuterClass.Block.Builder = BlockOuterClass.Block.newBuilder()): BlockOuterClass.Block.Builder = builder
        .setId(id.byteStringValue)
        .setParentId(parentId.byteStringValue)
        .setHeight(height)
        .setTimestamp(timestamp.asTimestamp())
        .addAllCollectionGuarantees(collectionGuarantees.map { it.builder().build() })
        .addAllBlockSeals(blockSeals.map { it.builder().build() })
        .addAllSignatures(signatures.map { it.byteStringValue })
}

data class FlowChunk(
    val collectionIndex: Int,
    val startState: ByteArray,
    val eventCollection: ByteArray,
    val blockId: FlowId,
    val totalComputationUsed: Long,
    val numberOfTransactions: Int,
    val index: Long,
    val endState: ByteArray,
    val executionDataId: FlowId,
    val stateDeltaCommitment: ByteArray,
) : Serializable {
    companion object {
        fun of(grpcExecutionResult: ExecutionResultOuterClass.Chunk) = FlowChunk(
            collectionIndex = grpcExecutionResult.collectionIndex,
            startState = grpcExecutionResult.startState.toByteArray(),
            eventCollection = grpcExecutionResult.eventCollection.toByteArray(),
            blockId = FlowId.of(grpcExecutionResult.blockId.toByteArray()),
            totalComputationUsed = grpcExecutionResult.totalComputationUsed,
            numberOfTransactions = grpcExecutionResult.numberOfTransactions,
            index = grpcExecutionResult.index,
            endState = grpcExecutionResult.endState.toByteArray(),
            executionDataId = FlowId.of(grpcExecutionResult.executionDataId.toByteArray()),
            stateDeltaCommitment = grpcExecutionResult.stateDeltaCommitment.toByteArray()
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FlowChunk) return false

        if (collectionIndex != other.collectionIndex) return false
        if (!startState.contentEquals(other.startState)) return false
        if (!eventCollection.contentEquals(other.eventCollection)) return false
        if (blockId != other.blockId) return false
        if (totalComputationUsed != other.totalComputationUsed) return false
        if (numberOfTransactions != other.numberOfTransactions) return false
        if (index != other.index) return false
        if (!endState.contentEquals(other.endState)) return false
        if (executionDataId != other.executionDataId) return false
        if (!stateDeltaCommitment.contentEquals(other.stateDeltaCommitment)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = collectionIndex
        result = 31 * result + startState.contentHashCode()
        result = 31 * result + eventCollection.contentHashCode()
        result = 31 * result + blockId.hashCode()
        result = 31 * result + totalComputationUsed.hashCode()
        result = 31 * result + numberOfTransactions
        result = 31 * result + index.hashCode()
        result = 31 * result + endState.contentHashCode()
        result = 31 * result + executionDataId.hashCode()
        result = 31 * result + stateDeltaCommitment.contentHashCode()
        return result
    }
}

data class FlowServiceEvent(
    val type: String,
    val payload: ByteArray,
) : Serializable {
    companion object {
        fun of(grpcExecutionResult: ExecutionResultOuterClass.ServiceEvent) = FlowServiceEvent(
            type = grpcExecutionResult.type,
            payload = grpcExecutionResult.payload.toByteArray(),
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FlowServiceEvent) return false

        if (type != other.type) return false
        if (!payload.contentEquals(other.payload)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + payload.contentHashCode()
        return result
    }
}

data class FlowExecutionResult(
    val blockId: FlowId,
    val previousResultId: FlowId,
    val chunks: List<FlowChunk>,
    val serviceEvents: List<FlowServiceEvent>,
) : Serializable {
    companion object {
        fun of(grpcExecutionResult: Access.ExecutionResultByIDResponse) = FlowExecutionResult(
            blockId = FlowId.of(grpcExecutionResult.executionResult.blockId.toByteArray()),
            previousResultId = FlowId.of(grpcExecutionResult.executionResult.previousResultId.toByteArray()),
            chunks = grpcExecutionResult.executionResult.chunksList.map { FlowChunk.of(it) },
            serviceEvents = grpcExecutionResult.executionResult.serviceEventsList.map { FlowServiceEvent.of(it) },
        )
    }
}

data class FlowChunkExecutionData(
    val collection: FlowExecutionDataCollection,
    val events: List<FlowEvent>,
    val trieUpdate: FlowTrieUpdate,
    val transactionResults: List<FlowExecutionDataTransactionResult>
) : Serializable {
    companion object {
        fun of(grpcExecutionResult: BlockExecutionDataOuterClass.ChunkExecutionData) = FlowChunkExecutionData(
            collection = FlowExecutionDataCollection.of(grpcExecutionResult.collection),
            events = grpcExecutionResult.eventsList.map { FlowEvent.of(it) },
            trieUpdate = FlowTrieUpdate.of(grpcExecutionResult.trieUpdate),
            transactionResults = grpcExecutionResult.transactionResultsList.map { FlowExecutionDataTransactionResult.of(it) },
        )
    }
}

data class FlowTrieUpdate(
    val rootHash: ByteArray,
    val paths: List<ByteArray>,
    val payloads: List<FlowPayload>
) : Serializable {
    companion object {
        fun of(grpcExecutionResult: BlockExecutionDataOuterClass.TrieUpdate) = FlowTrieUpdate(
            rootHash = grpcExecutionResult.rootHash.toByteArray(),
            paths = grpcExecutionResult.pathsList.map { it.toByteArray() },
            payloads = grpcExecutionResult.payloadsList.map { FlowPayload.of(it) },
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FlowTrieUpdate) return false

        if (!rootHash.contentEquals(other.rootHash)) return false
        if (paths != other.paths) return false
        if (payloads != other.payloads) return false

        return true
    }

    override fun hashCode(): Int {
        var result = rootHash.contentHashCode()
        result = 31 * result + paths.hashCode()
        result = 31 * result + payloads.hashCode()
        return result
    }
}

data class FlowPayload(
    val keyParts: List<FlowKeyPart>,
    val value: ByteArray,
) : Serializable {
    companion object {
        fun of(grpcExecutionResult: BlockExecutionDataOuterClass.Payload) = FlowPayload(
            keyParts = grpcExecutionResult.keyPartList.map { FlowKeyPart.of(it) },
            value = grpcExecutionResult.value.toByteArray()
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FlowPayload) return false

        if (keyParts != other.keyParts) return false
        if (!value.contentEquals(other.value)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = keyParts.hashCode()
        result = 31 * result + value.contentHashCode()
        return result
    }
}

data class FlowKeyPart(
    val type: Int,
    val value: ByteArray,
) : Serializable {
    companion object {
        fun of(grpcExecutionResult: BlockExecutionDataOuterClass.KeyPart) = FlowKeyPart(
            type = grpcExecutionResult.type,
            value = grpcExecutionResult.value.toByteArray()
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FlowKeyPart) return false

        if (type != other.type) return false
        if (!value.contentEquals(other.value)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type
        result = 31 * result + value.contentHashCode()
        return result
    }
}

data class FlowExecutionDataTransactionResult(
    val transactionId: FlowId,
    val failed: Boolean,
    val computationUsed: Long,
) : Serializable {
    companion object {
        fun of(grpcExecutionResult: BlockExecutionDataOuterClass.ExecutionDataTransactionResult) =
            FlowExecutionDataTransactionResult(
                transactionId = FlowId.of(grpcExecutionResult.transactionId.toByteArray()),
                failed = grpcExecutionResult.failed,
                computationUsed = grpcExecutionResult.computationUsed
            )
    }
}

data class FlowExecutionDataCollection(
    val transactions: List<FlowTransaction>,
) : Serializable {
    companion object {
        fun of(grpcExecutionResult: BlockExecutionDataOuterClass.ExecutionDataCollection) = FlowExecutionDataCollection(
            transactions = grpcExecutionResult.transactionsList.map { FlowTransaction.of(it) }
        )
    }
}

data class FlowBlockExecutionData(
    val blockId: FlowId,
    val chunkExecutionData: List<FlowChunkExecutionData>,
) : Serializable {
    companion object {
        fun of(grpcExecutionResult: BlockExecutionDataOuterClass.BlockExecutionData) = FlowBlockExecutionData(
            blockId = FlowId.of(grpcExecutionResult.blockId.toByteArray()),
            chunkExecutionData = grpcExecutionResult.chunkExecutionDataList.map { chunkExecutionData ->
                FlowChunkExecutionData(collection = FlowExecutionDataCollection.of(chunkExecutionData.collection), events = chunkExecutionData.eventsList.map { FlowEvent.of(it) }, trieUpdate = FlowTrieUpdate.of(chunkExecutionData.trieUpdate), transactionResults = chunkExecutionData.transactionResultsList.map { FlowExecutionDataTransactionResult.of(it) })
            }
        )
    }
}

data class FlowCollectionGuarantee(
    val id: FlowId,
    val signatures: List<FlowSignature>
) : Serializable {
    companion object {
        @JvmStatic
        fun of(value: CollectionOuterClass.CollectionGuarantee) = FlowCollectionGuarantee(
            id = FlowId.of(value.collectionId.toByteArray()),
            signatures = value.signaturesList.map { FlowSignature(it.toByteArray()) }
        )
    }

    @JvmOverloads
    fun builder(builder: CollectionOuterClass.CollectionGuarantee.Builder = CollectionOuterClass.CollectionGuarantee.newBuilder()): CollectionOuterClass.CollectionGuarantee.Builder = builder
        .setCollectionId(id.byteStringValue)
        .addAllSignatures(signatures.map { it.byteStringValue })
}

data class FlowBlockSeal(
    val id: FlowId,
    val executionReceiptId: FlowId,
    val executionReceiptSignatures: List<FlowSignature>,
    val resultApprovalSignatures: List<FlowSignature>
) : Serializable {
    companion object {
        @JvmStatic
        fun of(value: BlockSealOuterClass.BlockSeal) = FlowBlockSeal(
            id = FlowId.of(value.blockId.toByteArray()),
            executionReceiptId = FlowId.of(value.executionReceiptId.toByteArray()),
            executionReceiptSignatures = value.executionReceiptSignaturesList.map { FlowSignature(it.toByteArray()) },
            resultApprovalSignatures = value.executionReceiptSignaturesList.map { FlowSignature(it.toByteArray()) }
        )
    }

    @JvmOverloads
    fun builder(builder: BlockSealOuterClass.BlockSeal.Builder = BlockSealOuterClass.BlockSeal.newBuilder()): BlockSealOuterClass.BlockSeal.Builder = builder
        .setBlockId(id.byteStringValue)
        .setExecutionReceiptId(executionReceiptId.byteStringValue)
        .addAllExecutionReceiptSignatures(executionReceiptSignatures.map { it.byteStringValue })
        .addAllResultApprovalSignatures(resultApprovalSignatures.map { it.byteStringValue })
}

data class FlowCollection(
    val id: FlowId,
    val transactionIds: List<FlowId>
) : Serializable {
    companion object {
        @JvmStatic
        fun of(value: CollectionOuterClass.Collection) = FlowCollection(
            id = FlowId.of(value.id.toByteArray()),
            transactionIds = value.transactionIdsList.map { FlowId.of(it.toByteArray()) }
        )
    }

    @JvmOverloads
    fun builder(builder: CollectionOuterClass.Collection.Builder = CollectionOuterClass.Collection.newBuilder()): CollectionOuterClass.Collection.Builder = builder
        .setId(id.byteStringValue)
        .addAllTransactionIds(transactionIds.map { it.byteStringValue })
}

interface BytesHolder {
    val bytes: ByteArray
    val base16Value: String get() = bytes.bytesToHex()
    val stringValue: String get() = String(bytes)
    val byteStringValue: ByteString get() = UnsafeByteOperations.unsafeWrap(bytes)
    val integerValue: BigInteger get() = BigInteger(base16Value, 16)
}

data class FlowAddress private constructor(
    override val bytes: ByteArray
) : Serializable,
    BytesHolder {
    companion object {
        @JvmStatic
        fun of(bytes: ByteArray): FlowAddress = FlowAddress(fixedSize(bytes, FLOW_ADDRESS_SIZE_BYTES))
    }

    constructor(hex: String) : this(fixedSize(hex.hexToBytes(), FLOW_ADDRESS_SIZE_BYTES))

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as FlowAddress
        return bytes.contentEquals(other.bytes)
    }

    val formatted: String = "0x$base16Value"

    override fun hashCode(): Int = bytes.contentHashCode()
}

data class FlowArgument(
    override val bytes: ByteArray
) : Serializable,
    BytesHolder {
    constructor(jsonCadence: Field<*>) : this(Flow.encodeJsonCadence(jsonCadence))

    private var _jsonCadence: Field<*>? = null
    val jsonCadence: Field<*>
        get() {
            if (_jsonCadence == null) {
                _jsonCadence = Flow.decodeJsonCadence(bytes)
            }
            return _jsonCadence!!
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as FlowArgument
        return bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int = bytes.contentHashCode()
}

data class FlowScript(
    override val bytes: ByteArray
) : Serializable,
    BytesHolder {
    constructor(script: String) : this(script.encodeToByteArray())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as FlowScript
        return bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int = bytes.contentHashCode()
}

data class FlowScriptResponse(
    override val bytes: ByteArray
) : Serializable,
    BytesHolder {
    constructor(jsonCadence: Field<*>) : this(Flow.encodeJsonCadence(jsonCadence))

    private var _jsonCadence: Field<*>? = null
    val jsonCadence: Field<*>
        get() {
            if (_jsonCadence == null) {
                _jsonCadence = Flow.decodeJsonCadence(bytes)
            }
            return _jsonCadence!!
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as FlowScriptResponse
        return bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int = bytes.contentHashCode()
}

@kotlin.jvm.Throws
fun FlowScriptResponse.decodeToAny() {
    jsonCadence.decodeToAny()
}

@kotlin.jvm.Throws
inline fun <reified T> FlowScriptResponse.decode(): T = jsonCadence.decode()

data class FlowSignature(
    override val bytes: ByteArray
) : Serializable,
    BytesHolder {
    constructor(hex: String) : this(hex.hexToBytes())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as FlowSignature
        return bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int = bytes.contentHashCode()
}

data class FlowId private constructor(
    override val bytes: ByteArray
) : Serializable,
    BytesHolder {
    companion object {
        @JvmStatic
        fun of(bytes: ByteArray): FlowId = FlowId(fixedSize(bytes, FLOW_ID_SIZE_BYTES))
    }

    constructor(hex: String) : this(fixedSize(hex.hexToBytes(), FLOW_ID_SIZE_BYTES))

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as FlowId
        return bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int = bytes.contentHashCode()
}

data class FlowCode(
    override val bytes: ByteArray
) : Serializable,
    BytesHolder {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as FlowCode
        return bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int = bytes.contentHashCode()
}

data class FlowPublicKey(
    override val bytes: ByteArray
) : Serializable,
    BytesHolder {
    constructor(hex: String) : this(hex.hexToBytes())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as FlowPublicKey
        return bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int = bytes.contentHashCode()
}

data class FlowSnapshot(
    override val bytes: ByteArray
) : Serializable,
    BytesHolder {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as FlowSnapshot
        return bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int = bytes.contentHashCode()
}

data class FlowCompatibleRange(
    val startHeight: Long,
    val endHeight: Long
) : Serializable

data class FlowNodeVersionInfo(
    val semver: String,
    val commit: String,
    val sporkId: ByteArray,
    val protocolVersion: Long,
    val sporkRootBlockHeight: Long,
    val nodeRootBlockHeight: Long,
    val compatibleRange: FlowCompatibleRange?
) : Serializable {
    companion object {
        @JvmStatic
        fun of(value: NodeVersionInfoOuterClass.NodeVersionInfo) = FlowNodeVersionInfo(
            semver = value.semver,
            commit = value.commit,
            sporkId = value.sporkId.toByteArray(),
            protocolVersion = value.protocolVersion,
            sporkRootBlockHeight = value.sporkRootBlockHeight,
            nodeRootBlockHeight = value.nodeRootBlockHeight,
            compatibleRange = if (value.hasCompatibleRange()) {
                FlowCompatibleRange(value.compatibleRange.startHeight, value.compatibleRange.endHeight)
            } else {
                null
            }
        )
    }

    @JvmOverloads
    fun builder(builder: NodeVersionInfoOuterClass.NodeVersionInfo.Builder = NodeVersionInfoOuterClass.NodeVersionInfo.newBuilder()): NodeVersionInfoOuterClass.NodeVersionInfo.Builder = builder
        .setSemver(semver)
        .setCommit(commit)
        .setSporkId(UnsafeByteOperations.unsafeWrap(sporkId))
        .setProtocolVersion(protocolVersion)
        .setSporkRootBlockHeight(sporkRootBlockHeight)
        .setNodeRootBlockHeight(nodeRootBlockHeight)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FlowNodeVersionInfo) return false

        if (semver != other.semver) return false
        if (commit != other.commit) return false
        if (!sporkId.contentEquals(other.sporkId)) return false
        if (protocolVersion != other.protocolVersion) return false
        if (sporkRootBlockHeight != other.sporkRootBlockHeight) return false
        if (nodeRootBlockHeight != other.nodeRootBlockHeight) return false
        if (compatibleRange != other.compatibleRange) return false

        return true
    }

    override fun hashCode(): Int {
        var result = semver.hashCode()
        result = 31 * result + commit.hashCode()
        result = 31 * result + sporkId.contentHashCode()
        result = 31 * result + protocolVersion.hashCode()
        result = 31 * result + sporkRootBlockHeight.hashCode()
        result = 31 * result + nodeRootBlockHeight.hashCode()
        result = 31 * result + compatibleRange.hashCode()
        return result
    }
}

data class FlowEventPayload(
    override val bytes: ByteArray
) : Serializable,
    BytesHolder {
    constructor(jasonCadence: Field<*>) : this(Flow.encodeJsonCadence(jasonCadence))

    private var _jsonCadence: Field<*>? = null
    val jsonCadence: Field<*>
        get() {
            if (_jsonCadence == null) {
                _jsonCadence = Flow.decodeJsonCadence(bytes)
            }
            return _jsonCadence!!
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as FlowEventPayload
        return bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int = bytes.contentHashCode()
}

@kotlin.jvm.Throws
fun FlowEventPayload.decodeToAny() {
    jsonCadence.decodeToAny()
}

@kotlin.jvm.Throws
inline fun <reified T> FlowEventPayload.decode(): T = jsonCadence.decode()
