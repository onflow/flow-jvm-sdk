package org.onflow.sdk

object DomainTag {

    @JvmStatic
    val TRANSACTION_DOMAIN_TAG = normalize("FLOW-V0.0-transaction")

    @JvmStatic
    val USER_DOMAIN_TAG = normalize("FLOW-V0.0-user")

    @JvmStatic
    fun normalize(tag: String?): ByteArray {
        val normalizedTag = when {
            tag == null -> null
            tag.length > 32 -> throw IllegalArgumentException("Domain tags cannot be longer than 32 characters")
            tag.length < 32 -> tag.padEnd(32, '0')
            else -> tag
        }
        return normalizedTag?.toByteArray(Charsets.UTF_8) ?: byteArrayOf()
    }
}
