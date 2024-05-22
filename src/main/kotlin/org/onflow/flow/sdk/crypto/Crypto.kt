package org.onflow.flow.sdk.crypto

import it.unisa.dia.gas.crypto.jpbc.signature.bls01.engines.BLS01Signer
import it.unisa.dia.gas.crypto.jpbc.signature.bls01.generators.BLS01KeyPairGenerator
import it.unisa.dia.gas.crypto.jpbc.signature.bls01.generators.BLS01ParametersGenerator
import it.unisa.dia.gas.crypto.jpbc.signature.bls01.params.BLS01KeyGenerationParameters
import it.unisa.dia.gas.crypto.jpbc.signature.bls01.params.BLS01Parameters
import it.unisa.dia.gas.crypto.jpbc.signature.bls01.params.BLS01PublicKeyParameters
import it.unisa.dia.gas.plaf.jpbc.field.z.ImmutableZrElement
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.crypto.AsymmetricCipherKeyPair
import org.bouncycastle.crypto.CryptoException
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.params.AsymmetricKeyParameter
import org.bouncycastle.crypto.util.PrivateKeyFactory
import org.bouncycastle.crypto.util.PrivateKeyInfoFactory
import org.bouncycastle.crypto.util.PublicKeyFactory
import org.bouncycastle.crypto.util.SubjectPublicKeyInfoFactory
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.ECPointUtil
import org.bouncycastle.jce.interfaces.ECPrivateKey
import org.bouncycastle.jce.interfaces.ECPublicKey
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jce.spec.ECNamedCurveSpec
import org.bouncycastle.jce.spec.ECPrivateKeySpec
import org.onflow.flow.sdk.*
import org.onflow.flow.sdk.Signer
import java.io.*
import java.lang.reflect.Field
import java.math.BigInteger
import java.security.*
import java.security.spec.ECGenParameterSpec
import java.security.spec.ECPublicKeySpec
import kotlin.experimental.and
import kotlin.math.max


// BLS docs: http://gas.dia.unisa.it/projects/jpbc/schemes/ss_bls01.html#.XBpXKFWYWV4
// old docs: http://gas.dia.unisa.it/projects/jpbc/schemes/bls.html

data class KeyPair(
    val private: PrivateKey,
    val public: PublicKey
)

sealed class PrivateKeyType {
    data class ECDSA(val privateKey: java.security.PrivateKey) : PrivateKeyType()
    data class BLS(val privateKeyBytes: ByteArray) : PrivateKeyType() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is BLS) return false

            if (!privateKeyBytes.contentEquals(other.privateKeyBytes)) return false

            return true
        }

        override fun hashCode(): Int {
            return privateKeyBytes.contentHashCode()
        }
    }
}

sealed class PublicKeyType {
    data class ECDSA(private val publicKey: java.security.PublicKey) : PublicKeyType()
    data class BLS(private val publicKeyBytes: ByteArray) : PublicKeyType() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is BLS) return false

            if (!publicKeyBytes.contentEquals(other.publicKeyBytes)) return false

            return true
        }

        override fun hashCode(): Int {
            return publicKeyBytes.contentHashCode()
        }
    }
}

data class PrivateKey(
    val key: PrivateKeyType,
    val ecCoupleComponentSize: Int,
    val hex: String
)

data class PublicKey(
    val key: PublicKeyType,
    val hex: String
)


object Crypto {
    init {
        Security.addProvider(BouncyCastleProvider())
    }


    private const val PARAMETERS_FILE = "params/a_320_512.properties"

    private fun setupBLSParameters(): BLS01Parameters {
        val setup = BLS01ParametersGenerator()
        setup.init(PairingFactory.getPairingParameters(PARAMETERS_FILE))

        return setup.generateParameters()
    }

    private fun generateBLSKeyPair(parameters: BLS01Parameters): AsymmetricCipherKeyPair {
        val keyGen = BLS01KeyPairGenerator()
        keyGen.init(BLS01KeyGenerationParameters(SecureRandom(), parameters))

        return keyGen.generateKeyPair()
    }

    @Throws(FileNotFoundException::class, IOException::class)
    fun storePrivateKey(key: AsymmetricCipherKeyPair): ByteArray {
        val f: Field = key.private.javaClass.getDeclaredField("sk")
        f.setAccessible(true)
        val fieldContent: Any = f.get(key.private)
        var data: ByteArray? = null
        data = (fieldContent as ImmutableZrElement).toBytes()
        return data
    }

    @Throws(FileNotFoundException::class, IOException::class)
    fun storePublicKey(key: AsymmetricCipherKeyPair): ByteArray {
        val f: Field = key.public.javaClass.getDeclaredField("pk")
        f.setAccessible(true)
        val fieldContent: Any = f.get(key.public)
        var data: ByteArray? = null
        data = (fieldContent as ImmutableZrElement).toBytes()
        return data
    }

    private fun byteArrayToAsymmetricCipherKeyPair(byteArray: ByteArray): AsymmetricCipherKeyPair {
        val inputStream = ByteArrayInputStream(byteArray)
        val objectInputStream = ObjectInputStream(inputStream)

        // Deserialize the objects from the byte array
        val privateKeyObj = objectInputStream.readObject() as ByteArray
        val publicKeyObj = objectInputStream.readObject() as ByteArray

        // Construct AsymmetricKeyParameter objects from the decoded bytes
        val privateKeyParam: AsymmetricKeyParameter = PrivateKeyFactory.createKey(privateKeyObj)
        val publicKeyParam: AsymmetricKeyParameter = PublicKeyFactory.createKey(publicKeyObj)

        // Create the AsymmetricCipherKeyPair
        return AsymmetricCipherKeyPair(publicKeyParam, privateKeyParam)
    }

    fun serializeBLSPrivateKey(privateKey: AsymmetricKeyParameter): ByteArray {
        val k: PrivateKeyInfo = PrivateKeyInfoFactory.createPrivateKeyInfo(privateKey)
        val serializedKey: ByteArray = k.toASN1Primitive().encoded
        return serializedKey
    }

    fun serializeBLSPublicKey(publicKey: AsymmetricKeyParameter): ByteArray {
        val k: SubjectPublicKeyInfo = SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(publicKey)
        val serializedKey: ByteArray = k.toASN1Primitive().encoded
        return serializedKey
    }

    @JvmStatic
    @JvmOverloads
    fun generateKeyPair(algo: SignatureAlgorithm = SignatureAlgorithm.ECDSA_P256): KeyPair {
        when (algo.algorithm) {
            "ECDSA" -> {
                val gen = KeyPairGenerator.getInstance("EC", "BC")
                gen.initialize(ECGenParameterSpec(algo.curve), SecureRandom())
                val keyPair = gen.generateKeyPair()
                val privateKey = keyPair.private
                val publicKey = keyPair.public
                return KeyPair(
                    private = PrivateKey(
                        key = PrivateKeyType.ECDSA(privateKey),
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
                        key = PublicKeyType.ECDSA(publicKey),
                        hex = if (publicKey is ECPublicKey) {
                            (publicKey.q.xCoord.encoded + publicKey.q.yCoord.encoded).bytesToHex()
                        } else {
                            throw IllegalArgumentException("PublicKey must be an ECPublicKey")
                        }
                    )
                )
            }

            "BLS" -> {
                val parameters = setupBLSParameters()
                val keyPair = generateBLSKeyPair(parameters)

                val privateKeyBytes = storePrivateKey(keyPair)
                val publicKeyBytes = storePublicKey(keyPair)

                return KeyPair(
                    private = PrivateKey(
                        key = PrivateKeyType.BLS(privateKeyBytes),
                        ecCoupleComponentSize = 0, // Not applicable for BLS
                        hex = privateKeyBytes.bytesToHex()
                    ),
                    public = PublicKey(
                        key = PublicKeyType.BLS(publicKeyBytes),
                        hex = publicKeyBytes.bytesToHex()
                    )
                )
            }

            else -> throw IllegalArgumentException("Unsupported algorithm: ${algo.algorithm}")
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    @JvmStatic
    @JvmOverloads
    fun decodePrivateKey(key: String, algo: SignatureAlgorithm = SignatureAlgorithm.ECDSA_P256): PrivateKey {
        return when (algo.algorithm) {
            "ECDSA" -> {
                val ecParameterSpec = ECNamedCurveTable.getParameterSpec(algo.curve)
                val keyFactory = KeyFactory.getInstance(algo.algorithm, "BC")
                val ecPrivateKeySpec = ECPrivateKeySpec(BigInteger(key, 16), ecParameterSpec)
                val pk = keyFactory.generatePrivate(ecPrivateKeySpec) as ECPrivateKey
                PrivateKey(
                    key = PrivateKeyType.ECDSA(pk),
                    ecCoupleComponentSize = pk.parameters.n.bitLength() / 8,
                    hex = pk.d.toByteArray().bytesToHex()
                )
            }

            "BLS" -> {
                // Deserialize the BLS private key
                val keyBytes = key.hexToByteArray()
                val privateKeyPair = byteArrayToAsymmetricCipherKeyPair(keyBytes)
                val privateKey = privateKeyPair.private

                // Convert the private key to the required format
                val encodedPrivateKey = serializeBLSPrivateKey(privateKey)

                PrivateKey(
                    key = PrivateKeyType.BLS(encodedPrivateKey),
                    ecCoupleComponentSize = 0, // Not applicable for BLS
                    hex = encodedPrivateKey.bytesToHex()
                )
            }

            else -> throw IllegalArgumentException("Unsupported algorithm: ${algo.algorithm}")
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    @JvmStatic
    @JvmOverloads
    fun decodePublicKey(key: String, algo: SignatureAlgorithm = SignatureAlgorithm.ECDSA_P256): PublicKey {
        return when (algo.algorithm) {
            "ECDSA" -> {
                val ecParameterSpec = ECNamedCurveTable.getParameterSpec(algo.curve)
                val keyFactory = KeyFactory.getInstance(algo.algorithm, "BC")
                val params = ECNamedCurveSpec(
                    algo.curve,
                    ecParameterSpec.curve, ecParameterSpec.g, ecParameterSpec.n
                )
                val point = ECPointUtil.decodePoint(params.curve, byteArrayOf(0x04) + key.hexToBytes())
                val pubKeySpec = ECPublicKeySpec(point, params)
                val publicKey = keyFactory.generatePublic(pubKeySpec) as ECPublicKey
                PublicKey(
                    key = PublicKeyType.ECDSA(publicKey),
                    hex = (publicKey.q.xCoord.encoded + publicKey.q.yCoord.encoded).bytesToHex()
                )
            }

            "BLS" -> {
                // Deserialize the BLS public key
                val keyBytes = key.hexToByteArray()
                val publicKeyPair = byteArrayToAsymmetricCipherKeyPair(keyBytes)
                val publicKey = publicKeyPair.private as BLS01PublicKeyParameters

                // Convert the public key to the required format
                val encodedPublicKey = serializeBLSPublicKey(publicKey)
                PublicKey(
                    key = PublicKeyType.BLS(encodedPublicKey),
                    hex = encodedPublicKey.bytesToHex()
                )
            }

            else -> throw IllegalArgumentException("Unsupported algorithm: ${algo.algorithm}")
        }
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
    private val hashAlgo: HashAlgorithm
) : Hasher {
    override fun hash(bytes: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance(hashAlgo.algorithm)
        return digest.digest(bytes)
    }
}

internal class SignerImpl(
    private val privateKey: PrivateKey,
    private val hashAlgo: HashAlgorithm,
    override val hasher: Hasher = HasherImpl(hashAlgo)
) : Signer {
    private fun byteArrayToAsymmetricCipherKeyPair(byteArray: ByteArray): AsymmetricCipherKeyPair {
        val inputStream = ByteArrayInputStream(byteArray)
        val objectInputStream = ObjectInputStream(inputStream)

        // Deserialize the objects from the byte array
        val privateKeyObj = objectInputStream.readObject() as ByteArray
        val publicKeyObj = objectInputStream.readObject() as ByteArray

        // Construct AsymmetricKeyParameter objects from the decoded bytes
        val privateKeyParam: AsymmetricKeyParameter = PrivateKeyFactory.createKey(privateKeyObj)
        val publicKeyParam: AsymmetricKeyParameter = PublicKeyFactory.createKey(publicKeyObj)

        // Create the AsymmetricCipherKeyPair
        return AsymmetricCipherKeyPair(publicKeyParam, privateKeyParam)
    }

    override fun sign(bytes: ByteArray): ByteArray {
        val signature: ByteArray = when (privateKey.key) {
            is PrivateKeyType.ECDSA -> {
                val ecdsaSign = Signature.getInstance(hashAlgo.id)
                ecdsaSign.initSign(privateKey.key.privateKey)
                ecdsaSign.update(bytes)
                ecdsaSign.sign()
            }

            is PrivateKeyType.BLS -> {
                val signer = BLS01Signer(SHA256Digest())
                signer.init(true, byteArrayToAsymmetricCipherKeyPair(privateKey.key.privateKeyBytes).private)
                signer.update(bytes, 0, bytes.size)

                return try {
                    signer.generateSignature()
                } catch (e: CryptoException) {
                    throw RuntimeException(e)
                }
            }
        }
        return Crypto.normalizeSignature(signature, privateKey.ecCoupleComponentSize)
    }
}
