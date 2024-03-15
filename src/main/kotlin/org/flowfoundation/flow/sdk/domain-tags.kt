package org.flowfoundation.flow.sdk

object DomainTag {
    @JvmStatic
    val TRANSACTION_DOMAIN_TAG = normalize("FLOW-V0.0-transaction")

    @JvmStatic
    val USER_DOMAIN_TAG = normalize("FLOW-V0.0-user")

    @JvmStatic
    fun normalize(tag: String): ByteArray {
        val bytes = tag.toByteArray(Charsets.UTF_8)
        return when {
            bytes.size > 32 -> throw IllegalArgumentException("Domain tags cannot be longer than 32 characters")
            bytes.size < 32 -> bytes + ByteArray(32 - bytes.size)
            else -> bytes
        }
    }
}
