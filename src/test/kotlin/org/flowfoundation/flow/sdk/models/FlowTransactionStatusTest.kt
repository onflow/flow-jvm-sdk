package org.onflow.flow.sdk.models

import org.onflow.flow.sdk.FlowTransactionStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class FlowTransactionStatusTest {
    @Test
    fun `Test status retrieval by number`() {
        assertEquals(FlowTransactionStatus.UNKNOWN, FlowTransactionStatus.of(0))
        assertEquals(FlowTransactionStatus.PENDING, FlowTransactionStatus.of(1))
        assertEquals(FlowTransactionStatus.FINALIZED, FlowTransactionStatus.of(2))
        assertEquals(FlowTransactionStatus.EXECUTED, FlowTransactionStatus.of(3))
        assertEquals(FlowTransactionStatus.SEALED, FlowTransactionStatus.of(4))
        assertEquals(FlowTransactionStatus.EXPIRED, FlowTransactionStatus.of(5))
    }

    @Test
    fun `Test invalid status number`() {
        assertThrows(IllegalArgumentException::class.java) {
            FlowTransactionStatus.of(6)
        }
    }
}
