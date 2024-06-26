package org.onflow.flow.sdk.crypto

import org.bouncycastle.crypto.macs.KMAC
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.jcajce.provider.digest.Keccak
import org.onflow.flow.sdk.*
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.ECPointUtil
import org.bouncycastle.jce.interfaces.ECPrivateKey
import org.bouncycastle.jce.interfaces.ECPublicKey
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jce.spec.ECNamedCurveSpec
import org.bouncycastle.jce.spec.ECPrivateKeySpec
import org.onflow.flow.sdk.Signer
import java.math.BigInteger
import java.security.*
import java.security.spec.ECGenParameterSpec
import java.security.spec.ECPublicKeySpec
import kotlin.experimental.and
import kotlin.math.max

data class KeyPair(
    val private: PrivateKey,
    val public: PublicKey
)

data class PrivateKey(
    val key: java.security.PrivateKey,
    val ecCoupleComponentSize: Int,
    val hex: String
)

data class PublicKey(
    val key: java.security.PublicKey,
    val hex: String
)

object Crypto {
    init {
        Security.addProvider(BouncyCastleProvider())
    }

    @JvmStatic
    @JvmOverloads
    fun generateKeyPair(algo: SignatureAlgorithm = SignatureAlgorithm.ECDSA_P256): KeyPair {
        val generator = KeyPairGenerator.getInstance("EC", "BC")
        generator.initialize(ECGenParameterSpec(algo.curve), SecureRandom())
        val keyPair = generator.generateKeyPair()
        val privateKey = keyPair.private
        val publicKey = keyPair.public
        return KeyPair(
            private = PrivateKey(
                key = keyPair.private,
                ecCoupleComponentSize = if (privateKey is ECPrivateKey) {
                    privateKey.parameters.n.bitLength() / 8
                } else {
                    0
                },
                hex = if (privateKey is ECPrivateKey) {
                    privateKey.d.toByteArray().bytesToHex()
                } else {
                    throw IllegalArgumentException("PrivateKey must be an ECPublicKey")
                }
            ),
            public = PublicKey(
                key = publicKey,
                hex = if (publicKey is ECPublicKey) {
                    (publicKey.q.xCoord.encoded + publicKey.q.yCoord.encoded).bytesToHex()
                } else {
                    throw IllegalArgumentException("PublicKey must be an ECPublicKey")
                }
            )
        )
    }

    @JvmStatic
    @JvmOverloads
    fun decodePrivateKey(key: String, algo: SignatureAlgorithm = SignatureAlgorithm.ECDSA_P256): PrivateKey {
        val ecParameterSpec = ECNamedCurveTable.getParameterSpec(algo.curve)
        val keyFactory = KeyFactory.getInstance(algo.algorithm, "BC")
        val ecPrivateKeySpec = ECPrivateKeySpec(BigInteger(key, 16), ecParameterSpec)
        val pk = keyFactory.generatePrivate(ecPrivateKeySpec)
        return PrivateKey(
            key = pk,
            ecCoupleComponentSize = if (pk is ECPrivateKey) {
                pk.parameters.n.bitLength() / 8
            } else {
                0
            },
            hex = if (pk is ECPrivateKey) {
                pk.d.toByteArray().bytesToHex()
            } else {
                throw IllegalArgumentException("PrivateKey must be an ECPublicKey")
            }
        )
    }

    @JvmStatic
    @JvmOverloads
    fun decodePublicKey(key: String, algo: SignatureAlgorithm = SignatureAlgorithm.ECDSA_P256): PublicKey {
        val ecParameterSpec = ECNamedCurveTable.getParameterSpec(algo.curve)
        val keyFactory = KeyFactory.getInstance("EC", "BC")
        val params = ECNamedCurveSpec(
            algo.curve,
            ecParameterSpec.curve, ecParameterSpec.g, ecParameterSpec.n
        )
        val point = ECPointUtil.decodePoint(params.curve, byteArrayOf(0x04) + key.hexToBytes())
        val pubKeySpec = ECPublicKeySpec(point, params)
        val publicKey = keyFactory.generatePublic(pubKeySpec)
        return PublicKey(
            key = publicKey,
            hex = if (publicKey is ECPublicKey) {
                (publicKey.q.xCoord.encoded + publicKey.q.yCoord.encoded).bytesToHex()
            } else {
                throw IllegalArgumentException("PublicKey must be an ECPublicKey")
            }
        )
    }

    @JvmStatic
    @JvmOverloads
    fun getSigner(privateKey: PrivateKey, hashAlgo: HashAlgorithm = HashAlgorithm.SHA3_256): Signer {
        return SignerImpl(privateKey, hashAlgo)
    }

    @JvmStatic
    @JvmOverloads
    fun getHasher(hashAlgo: HashAlgorithm = HashAlgorithm.SHA3_256): Hasher {
        return HasherImpl(hashAlgo)
    }

    @JvmStatic
    fun normalizeSignature(signature: ByteArray, ecCoupleComponentSize: Int): ByteArray {
        val (r, s) = extractRS(signature)

        val paddedSignature = ByteArray(2 * ecCoupleComponentSize)

        val rBytes = r.toByteArray()
        val sBytes = s.toByteArray()

        // occasionally R/S bytes representation has leading zeroes, so make sure we trim them appropriately
        rBytes.copyInto(paddedSignature, max(ecCoupleComponentSize - rBytes.size, 0), max(0, rBytes.size - ecCoupleComponentSize))
        sBytes.copyInto(paddedSignature, max(2 * ecCoupleComponentSize - sBytes.size, ecCoupleComponentSize), max(0, sBytes.size - ecCoupleComponentSize))

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
    private val outputSize: Int? = 0
) : Hasher {
    override fun hash(bytes: ByteArray): ByteArray {
        return when (hashAlgo) {
            HashAlgorithm.KECCAK256 -> {
                val keccakDigest = Keccak.Digest256()
                keccakDigest.digest(bytes)
            }
            HashAlgorithm.KMAC128 -> {
                if (key == null) {
                    throw IllegalArgumentException("KMAC128 requires a key")
                }
                val kmac = KMAC(128, customizer)
                kmac.init(KeyParameter(key))
                val output = ByteArray(outputSize ?: kmac.digestSize)
                kmac.update(bytes, 0, output.size)
                kmac.doFinal(output, 0, output.size)
                output
            }
            else -> {
                val digest = MessageDigest.getInstance(hashAlgo.algorithm)
                digest.digest(bytes)
            }
        }
    }
}

internal class SignerImpl(
    private val privateKey: PrivateKey,
    private val hashAlgo: HashAlgorithm,
    override val hasher: Hasher = HasherImpl(hashAlgo)
) : Signer {
    override fun sign(bytes: ByteArray): ByteArray {
        val signature: ByteArray

        if (hashAlgo == HashAlgorithm.KECCAK256 || hashAlgo == HashAlgorithm.KMAC128) {
            // Handle Keccak-256 and KMAC128 separately
            val hash = hasher.hash(bytes)
            val ecdsaSign = Signature.getInstance("NONEwithECDSA")
            ecdsaSign.initSign(privateKey.key)
            ecdsaSign.update(hash)
            signature = ecdsaSign.sign()
        } else {
            // Handle other algorithms with valid IDs
            val ecdsaSign = Signature.getInstance(hashAlgo.id)
            ecdsaSign.initSign(privateKey.key)
            ecdsaSign.update(bytes)
            signature = ecdsaSign.sign()
        }

        if (privateKey.ecCoupleComponentSize <= 0) {
            return signature
        }

        return Crypto.normalizeSignature(signature, privateKey.ecCoupleComponentSize)
    }
}
