package org.onflow.flow.sdk.crypto

import org.bouncycastle.crypto.macs.KMAC
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey
import org.bouncycastle.jcajce.provider.digest.Keccak
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.ECPointUtil
import org.bouncycastle.jce.interfaces.ECPrivateKey
import org.bouncycastle.jce.interfaces.ECPublicKey
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec
import org.bouncycastle.jce.spec.ECNamedCurveSpec
import org.bouncycastle.jce.spec.ECPrivateKeySpec
import org.bouncycastle.jce.spec.ECPublicKeySpec
import org.onflow.flow.sdk.*
import java.security.spec.ECGenParameterSpec
import org.onflow.flow.sdk.Signer
import java.math.BigInteger
import java.security.*
import java.security.spec.ECPoint
import kotlin.experimental.and
import kotlin.math.max


// TODO: keyPair is an obsolete class and should be deprecated.
// It is equivalent to the private key since it contains the private key value.
// This isn't deprecated for now till the breaking change impact is assessed.
data class KeyPair(
    val private: PrivateKey,
    val public: PublicKey
)

data class PrivateKey(
    val key: java.security.PrivateKey,
    val curve: ECNamedCurveParameterSpec,
    val hex: String,
    val publicKey: PublicKey
)

data class PublicKey(
    val key: java.security.PublicKey,
    val hex: String

    // TODO: add signature verification function
)

object Crypto {
    init {
        Security.addProvider(BouncyCastleProvider())
    }

    @JvmStatic
    @JvmOverloads
    // TODO: remove default `SignatureAlgorithm.ECDSA_P256` and add algo check
    fun generateKeyPair(algo: SignatureAlgorithm = SignatureAlgorithm.ECDSA_P256): KeyPair {
        val generator = KeyPairGenerator.getInstance("EC", "BC")
        generator.initialize(ECGenParameterSpec(algo.curve), SecureRandom())
        val keyPair = generator.generateKeyPair()
        val sk = keyPair.private
        val pk = keyPair.public
        val curveSpec = ECNamedCurveTable.getParameterSpec(algo.curve)

        val publicKey = PublicKey(
            key = pk,
            hex = jsecPublicKeyToHexString(pk)
        )
        val privateKey = PrivateKey(
            key = sk,
            curve = curveSpec,
            publicKey = publicKey,
            hex = jsecPrivateKeyToHexString(sk)
        )
        return KeyPair(
            private = privateKey,
            public = publicKey
        )
    }

    @JvmStatic
    @JvmOverloads
    // TODO: remove default `SignatureAlgorithm.ECDSA_P256` and add algo check
    fun decodePrivateKey(key: String, algo: SignatureAlgorithm = SignatureAlgorithm.ECDSA_P256): PrivateKey {
        // TODO: why not ECGenParameterSpec(algo.curve)?
        val curveSpec = ECNamedCurveTable.getParameterSpec(algo.curve)
        // TODO: missing check of the integer against the curve order
        val ecPrivateKeySpec = ECPrivateKeySpec(BigInteger(key, 16), curveSpec)
        val keyFactory = KeyFactory.getInstance(algo.algorithm, "BC")
        val sk = keyFactory.generatePrivate(ecPrivateKeySpec)
        val pk = derivePublicKey(sk)
        var publicKey = PublicKey(
            key = pk,
            hex = jsecPublicKeyToHexString(pk)
        )

        return PrivateKey(
            key = sk,
            curve = curveSpec,
            publicKey = publicKey,
            // TODO: why this test
            hex = if (sk is ECPrivateKey) {
                // TODO: padding
                sk.d.toByteArray().bytesToHex()
            } else {
                // TODO: update log
                throw IllegalArgumentException("PrivateKey must be an ECPublicKey")
            }
        )
    }

    @JvmStatic
    @JvmOverloads
    // TODO: remove default `SignatureAlgorithm.ECDSA_P256` and add algo check
    fun decodePublicKey(key: String, algo: SignatureAlgorithm = SignatureAlgorithm.ECDSA_P256): PublicKey {
        // TODO: why not ECGenParameterSpec(algo.curve)?
        val ecParameterSpec = ECNamedCurveTable.getParameterSpec(algo.curve)
        // TODO: check if that's necessary
        val params = ECNamedCurveSpec(
            algo.curve,
            ecParameterSpec.curve, ecParameterSpec.g, ecParameterSpec.n
        )
        val pointBytes = ECPointUtil.decodePoint(params.curve, byteArrayOf(0x04) + key.hexToBytes())
        val point = java.security.spec.ECPublicKeySpec(pointBytes, params)
        val keyFactory = KeyFactory.getInstance("EC", "BC")
        val publicKey = keyFactory.generatePublic(point)
        return PublicKey(
            key = publicKey,
            hex = key
        )
    }

    @JvmStatic
    @JvmOverloads
    fun getSigner(privateKey: PrivateKey, hashAlgo: HashAlgorithm = HashAlgorithm.SHA3_256): Signer {
        // TODO: missing validation of the Private key algo
        // input hash algo is validates in `SignerImpl`
        return SignerImpl(privateKey, hashAlgo)
    }

    @JvmStatic
    @JvmOverloads
    fun getHasher(hashAlgo: HashAlgorithm = HashAlgorithm.SHA3_256): Hasher {
        return HasherImpl(hashAlgo)
    }

    @JvmStatic
    fun jsecPrivateKeyToHexString(sk: java.security.PrivateKey): String {
        val hexString = if (sk is ECPrivateKey) {
            // TODO: would be good to add padding
            sk.d.toByteArray().bytesToHex()
        } else {
            throw IllegalArgumentException("PrivateKey must be an ECPublicKey")
        }

        return hexString
    }

    @JvmStatic
    fun jsecPublicKeyToHexString(pk: java.security.PublicKey): String {
        val hexString = if (pk is ECPublicKey) {
            // TODO: padding is missing
            (pk.q.xCoord.encoded + pk.q.yCoord.encoded).bytesToHex()
        } else {
            throw IllegalArgumentException("PublicKey must be an ECPublicKey")
        }
        return hexString
    }


    @JvmStatic
    fun derivePublicKey(sk: java.security.PrivateKey): java.security.PublicKey{
        val bcSK = if (sk is ECPrivateKey) {
            sk
        } else {
            throw IllegalArgumentException("Private key must be an ECPrivateKey")
        }
        // compute the point
        val curveParams = bcSK.parameters
        val BCPoint = curveParams.curve.multiplier.multiply(curveParams.g, bcSK.d)
        // convert to ECPublicKey
        var ECPointParams = ECPublicKeySpec(BCPoint, curveParams)
        val keyFactory = KeyFactory.getInstance("EC", "BC")
        val publicKey = keyFactory.generatePublic(ECPointParams)
        return publicKey
    }

    @JvmStatic
    // curve order size in bytes
    fun getCurveOrderSize(curve: ECNamedCurveParameterSpec): Int {
        val bitSize = curve.getN().bitLength()
        val byteSize = (bitSize + 7)/8
        return byteSize
    }

    // TODO: merge formatSignature and extractRS by shortcutting the bigInt step
    @JvmStatic
    fun formatSignature(signature: ByteArray, curveOrderSize: Int): ByteArray {
        val (r, s) = extractRS(signature)

        val paddedSignature = ByteArray(2 * curveOrderSize)

        val rBytes = r.toByteArray()
        val sBytes = s.toByteArray()

        // occasionally R/S bytes representation has leading zeroes, so make sure to copy them appropriately
        rBytes.copyInto(paddedSignature, max(curveOrderSize - rBytes.size, 0), max(0, rBytes.size - curveOrderSize))
        sBytes.copyInto(paddedSignature, max(2 * curveOrderSize - sBytes.size, curveOrderSize), max(0, sBytes.size - curveOrderSize))

        return paddedSignature
    }

    @JvmStatic
    fun extractRS(signature: ByteArray): Pair<BigInteger, BigInteger> {
        val startR = if ((signature[1] and 0x80.toByte()) != 0.toByte()) 3 else 2
        val lengthR = signature[startR + 1].toInt()
        val startS = startR + 2 + lengthR
        val lengthS = signature[startS + 1].toInt()
        return Pair(
            BigInteger(signature.copyOfRange(startR + 2, startR + 2 + lengthR)),
            BigInteger(signature.copyOfRange(startS + 2, startS + 2 + lengthS))
        )
    }
}

internal class HasherImpl(
    private val hashAlgo: HashAlgorithm,
    private val key: ByteArray? = null,
    private val customizer: ByteArray? = null,
    private val outputSize: Int = 32
) : Hasher {
    private var kmac: KMAC? = null

    init {
        if (hashAlgo == HashAlgorithm.KMAC128) {
            if (outputSize < 32) {
                throw IllegalArgumentException("KMAC128 output size must be at least 32 bytes")
            }

            if (key == null || key.size < 16) {
                throw IllegalArgumentException("KMAC128 requires a key of at least 16 bytes")
            }
            kmac = KMAC(128, customizer)
            kmac!!.init(KeyParameter(key))
        } else if (hashAlgo == HashAlgorithm.KECCAK256 ||
            hashAlgo == HashAlgorithm.SHA3_256 ||
            hashAlgo == HashAlgorithm.SHA2_256
        ) {
            if (key != null) {
                throw IllegalArgumentException("Key must be null")
            }
            if (customizer != null) {
                throw IllegalArgumentException("Customizer must be null")
            }
            if (outputSize != (hashAlgo.outputSize / 8)) {
                throw IllegalArgumentException("Output size must be 32 bytes")
            }
        } else {
            throw IllegalArgumentException("Unsupported hash algorithm: ${hashAlgo.algorithm}")
        }
    }

    override fun hash(bytes: ByteArray): ByteArray {
        return when (hashAlgo) {
            HashAlgorithm.KECCAK256 -> {
                val keccakDigest = Keccak.Digest256()
                keccakDigest.digest(bytes)
            }
            HashAlgorithm.KMAC128 -> {
                val output = ByteArray(outputSize)
                kmac!!.update(bytes, 0, bytes.size)
                kmac!!.doFinal(output, 0, outputSize)
                output
            }
            else -> {
                val digest = MessageDigest.getInstance(hashAlgo.algorithm)
                digest.digest(bytes)
            }
        }
    }

    fun update(bytes: ByteArray, off: Int, len: Int) {
        kmac?.update(bytes, off, len)
    }

    fun doFinal(outputSize: Int): ByteArray {
        val output = ByteArray(outputSize)
        kmac?.doFinal(output, 0, outputSize)
        return output
    }
}



internal class SignerImpl(
    private val privateKey: PrivateKey,
    private val hashAlgo: HashAlgorithm,
    override val hasher: Hasher = HasherImpl(hashAlgo)
) : Signer {

    override fun sign(bytes: ByteArray): ByteArray {
        // TODO: add check for private key algo
        val signature: ByteArray

        val ecdsaSign: Signature = when (hashAlgo) {
            // only allow hashes of 256 bits to match the supported curves (order of 256 bits),
            // although higher hashes could be used in theory
            HashAlgorithm.KECCAK256, HashAlgorithm.SHA2_256, HashAlgorithm.SHA3_256 -> {
                Signature.getInstance("NONEwithECDSA")
            }
            else -> throw IllegalArgumentException("Unsupported hash algorithm: ${hashAlgo.algorithm}")
        }
        val hash = hasher.hash(bytes)
        ecdsaSign.initSign(privateKey.key)
        ecdsaSign.update(hash)
        signature = ecdsaSign.sign()

        val curveOrderSize = Crypto.getCurveOrderSize(privateKey.curve)
        return Crypto.formatSignature(signature, curveOrderSize)
    }
}
