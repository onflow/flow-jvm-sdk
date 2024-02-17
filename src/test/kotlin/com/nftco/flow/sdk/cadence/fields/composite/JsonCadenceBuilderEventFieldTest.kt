package com.nftco.flow.sdk.cadence.fields.composite

import com.nftco.flow.sdk.cadence.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class JsonCadenceBuilderEventFieldTest {
    @Test
    fun `Test EventField type and value properties`() {
        val fieldValue = CompositeValue("eventId", emptyArray())
        val eventField = EventField(fieldValue)

        assertEquals(TYPE_EVENT, eventField.type)
        assertEquals(fieldValue, eventField.value)
    }

    @Test
    fun `Test equals`() {
        val fieldValue1 = CompositeValue("eventId1", emptyArray())
        val fieldValue2 = CompositeValue("eventId1", emptyArray())
        val fieldValue3 = CompositeValue("eventId2", emptyArray())

        val eventField1 = EventField(fieldValue1)
        val eventField2 = EventField(fieldValue2)
        val eventField3 = EventField(fieldValue3)

        assertTrue(eventField1 == eventField2)
        assertFalse(eventField1 == eventField3)
    }

    @Test
    fun `Test hashCode`() {
        val fieldValue1 = CompositeValue("eventId1", emptyArray())
        val fieldValue2 = CompositeValue("eventId1", emptyArray())
        val fieldValue3 = CompositeValue("eventId2", emptyArray())

        val eventField1 = EventField(fieldValue1)
        val eventField2 = EventField(fieldValue2)
        val eventField3 = EventField(fieldValue3)

        assertEquals(eventField1.hashCode(), eventField2.hashCode())

        assertNotEquals(eventField1.hashCode(), eventField3.hashCode())
    }

    @Test
    fun `Test decoding EventField`() {
        val fieldValue = CompositeValue("eventId", arrayOf(CompositeAttribute("id", StringField("event"))))
        val eventField = EventField(fieldValue)

        val decodedValue = eventField.decodeToAny()
        assertEquals(mapOf("id" to "event"), decodedValue)
    }
}
