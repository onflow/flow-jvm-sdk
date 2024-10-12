package org.onflow.flow.sdk

import com.google.common.io.BaseEncoding
import com.google.protobuf.Timestamp
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.MessageDigest
import java.security.Security
import java.time.LocalDateTime
import java.time.ZoneOffset

fun ByteArray.bytesToHex(): String = BaseEncoding.base16().lowerCase().encode(this)

fun String.hexToBytes(): ByteArray = BaseEncoding.base16().lowerCase().decode(
    if (this.lowercase().startsWith("0x")) {
        this.substring(2)
    } else {
        this
    }
)

fun Timestamp.asLocalDateTime(): LocalDateTime = LocalDateTime.ofEpochSecond(this.seconds, this.nanos, ZoneOffset.UTC)

fun LocalDateTime.asTimestamp(): Timestamp = Timestamp
    .newBuilder()
    .setSeconds(this.toEpochSecond(ZoneOffset.UTC))
    .setNanos(this.nano)
    .build()

fun ByteArray.sha3256Hash(): ByteArray {
    Security.addProvider(BouncyCastleProvider())
    return MessageDigest.getInstance("SHA3-256", BouncyCastleProvider.PROVIDER_NAME).digest(this)
}

fun ByteArray.sha2256Hash(): ByteArray {
    Security.addProvider(BouncyCastleProvider())
    return MessageDigest.getInstance("SHA2-256", "BC").digest(this)
}

fun fixedSize(bytes: ByteArray, size: Int): ByteArray {
    if (bytes.size > size) {
        throw IllegalArgumentException("must have no more than $size bytes long")
    }
    return if (bytes.size < size) {
        ByteArray(size - bytes.size) + bytes
    } else {
        bytes
    }
}
