package com.nftco.flow.sdk.cadence.fields.number

import com.nftco.flow.sdk.cadence.*
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.math.BigInteger

class JsonCadenceBuilderNumberFieldTest {
    @Test
    fun `Test creating NumberField with valid values`() {
        val intField = NumberField(TYPE_INT, "42")
        assertEquals("42", intField.value)

        val uintField = NumberField(TYPE_UINT, "42")
        assertEquals("42", uintField.value)

        val int8Field = NumberField(TYPE_INT8, "42")
        assertEquals("42", int8Field.value)

        val uint8Field = NumberField(TYPE_UINT8, "42")
        assertEquals("42", uint8Field.value)

        val int16Field = NumberField(TYPE_INT16, "32767")
        assertEquals("32767", int16Field.value)

        val uint16Field = NumberField(TYPE_UINT16, "65535")
        assertEquals("65535", uint16Field.value)

        val int32Field = NumberField(TYPE_INT32, "-2147483648")
        assertEquals("-2147483648", int32Field.value)

        val uint32Field = NumberField(TYPE_UINT32, "4294967295")
        assertEquals("4294967295", uint32Field.value)

        val int64Field = NumberField(TYPE_INT64, "-9223372036854775808")
        assertEquals("-9223372036854775808", int64Field.value)

        val uint64Field = NumberField(TYPE_UINT64, "18446744073709551615")
        assertEquals("18446744073709551615", uint64Field.value)

        val int256Field = NumberField(
            TYPE_INT256,
            "12345678901234567890123456789012345678901234567890123456789012345678901234567890"
        )
        assertEquals(
            "12345678901234567890123456789012345678901234567890123456789012345678901234567890",
            int256Field.value
        )

        val fix64Field = NumberField(TYPE_FIX64, "42.123456789")
        assertEquals("42.123456789", fix64Field.value)

        val ufix64Field = NumberField(TYPE_UFIX64, "12345678901234567890.123456789012345678")
        assertEquals("12345678901234567890.123456789012345678", ufix64Field.value)
    }

    @Test
    fun `Test converting NumberField to UByte`() {
        val field = NumberField(TYPE_UINT8, "42")
        assertEquals(42.toUByte(), field.toUByte())
    }

    @Test
    fun `Test converting NumberField to Byte`() {
        val field = NumberField(TYPE_INT8, "-42")
        assertEquals((-42).toByte(), field.toByte())
    }

    @Test
    fun `Test converting NumberField to UShort`() {
        val field = NumberField(TYPE_UINT16, "65535")
        assertEquals(65535.toUShort(), field.toUShort())
    }

    @Test
    fun `Test converting NumberField to Short`() {
        val field = NumberField(TYPE_INT16, "-32768")
        assertEquals((-32768).toShort(), field.toShort())
    }

    @Test
    fun `Test converting NumberField to UInt`() {
        val field = NumberField(TYPE_UINT32, "4294967295")
        assertEquals(4294967295u, field.toUInt())
    }

    @Test
    fun `Test converting NumberField to Int`() {
        val field = NumberField(TYPE_INT32, "-2147483648")
        assertEquals(-2147483648, field.toInt())
    }

    @Test
    fun `Test converting NumberField to ULong`() {
        val field = NumberField(TYPE_UINT64, "18446744073709551615")
        assertEquals(18446744073709551615u.toULong(), field.toULong())
    }

    @Test
    fun `Test converting NumberField to Long`() {
        val field = NumberField(TYPE_INT64, "-922337203685477580")
        assertEquals(-922337203685477580, field.toLong())
    }

    @Test
    fun `Test converting NumberField to BigInteger`() {
        val field = NumberField(TYPE_INT256, "12345678901234567890123456789012345678901234567890123456789012345678901234567890")
        assertEquals(BigInteger("12345678901234567890123456789012345678901234567890123456789012345678901234567890"), field.toBigInteger())
    }

    @Test
    fun `Test converting NumberField to Float`() {
        val field = NumberField(TYPE_FIX64, "42.0")
        assertEquals(42.0f, field.toFloat())
    }

    @Test
    fun `Test converting NumberField to Double`() {
        val field = NumberField(TYPE_FIX64, "42.123456789")
        assertEquals(42.123456789, field.toDouble())
    }

    @Test
    fun `Test converting NumberField to BigDecimal`() {
        val field = NumberField(TYPE_UFIX64, "12345678901234567890.123456789012345678")
        assertEquals(BigDecimal("12345678901234567890.123456789012345678"), field.toBigDecimal())
    }

    @Test
    fun `Test creating NumberField with invalid values`() {
        assertThatThrownBy { NumberField(TYPE_INT, "abc").decodeToAny() }
            .isInstanceOf(Exception::class.java)

        assertThatThrownBy { NumberField(TYPE_UINT, "-42").decodeToAny() }
            .isInstanceOf(Exception::class.java)

        assertThatThrownBy { NumberField(TYPE_INT8, "128").decodeToAny() }
            .isInstanceOf(Exception::class.java)

        assertThatThrownBy { NumberField(TYPE_UINT8, "-1").decodeToAny() }
            .isInstanceOf(Exception::class.java)

        assertThatThrownBy { NumberField(TYPE_INT16, "32768").decodeToAny() }
            .isInstanceOf(Exception::class.java)

        assertThatThrownBy { NumberField(TYPE_UINT16, "-1").decodeToAny() }
            .isInstanceOf(Exception::class.java)

        assertThatThrownBy { NumberField(TYPE_INT32, "2147483648").decodeToAny() }
            .isInstanceOf(Exception::class.java)

        assertThatThrownBy { NumberField(TYPE_UINT32, "-1").decodeToAny() }
            .isInstanceOf(Exception::class.java)

        assertThatThrownBy { NumberField(TYPE_INT64, "9223372036854775808").decodeToAny() }
            .isInstanceOf(Exception::class.java)

        assertThatThrownBy { NumberField(TYPE_UINT64, "-1").decodeToAny() }
            .isInstanceOf(Exception::class.java)

        assertThatThrownBy { NumberField(TYPE_INT128, "170141183460469231731687303715884105728").decodeToAny() }
            .isInstanceOf(Exception::class.java)

        assertThatThrownBy { NumberField(TYPE_UINT128, "-1").decodeToAny() }
            .isInstanceOf(Exception::class.java)

        assertThatThrownBy { NumberField(TYPE_INT256, "115792089237316195423570985008687907853269984665640564039457584007913129639936").decodeToAny() }
            .isInstanceOf(Exception::class.java)

        assertThatThrownBy { NumberField(TYPE_UINT256, "-1").decodeToAny() }
            .isInstanceOf(Exception::class.java)

        assertThatThrownBy { NumberField(TYPE_WORD8, "256").decodeToAny() }
            .isInstanceOf(Exception::class.java)

        assertThatThrownBy { NumberField(TYPE_WORD16, "65536").decodeToAny() }
            .isInstanceOf(Exception::class.java)

        assertThatThrownBy { NumberField(TYPE_WORD32, "4294967296").decodeToAny() }
            .isInstanceOf(Exception::class.java)

        assertThatThrownBy { NumberField(TYPE_WORD64, "18446744073709551616").decodeToAny() }
            .isInstanceOf(Exception::class.java)

        assertThatThrownBy { NumberField(TYPE_FIX64, "3.14").decodeToAny() }
            .isInstanceOf(Exception::class.java)

        assertThatThrownBy { NumberField(TYPE_UFIX64, "-1.23").decodeToAny() }
            .isInstanceOf(Exception::class.java)
    }

    @Test
    fun `Test decoding NumberField with blank or empty value`() {
        assertThatThrownBy { NumberField(TYPE_UINT, "   ").decodeToAny() }
            .isInstanceOf(Exception::class.java)

        assertThatThrownBy { NumberField(TYPE_INT8, "").decodeToAny() }
            .isInstanceOf(Exception::class.java)
    }
}
