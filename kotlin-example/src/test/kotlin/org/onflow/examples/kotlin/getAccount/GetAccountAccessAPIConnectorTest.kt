package org.onflow.examples.kotlin.getAccount

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.onflow.flow.common.test.*
import org.onflow.flow.sdk.FlowAccessApi
import java.math.BigDecimal

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
internal class GetAccountAccessAPIConnectorTest {

    @FlowServiceAccountCredentials
    lateinit var testAccount: TestAccount

    @FlowTestClient
    lateinit var accessAPI: FlowAccessApi

    private lateinit var accountConnector: GetAccountAccessAPIConnector

    @BeforeEach
    fun setup() {
        accountConnector = GetAccountAccessAPIConnector(accessAPI)
    }

    @Test
    fun `Can fetch account from the latest block`() {
        val address = testAccount.flowAddress
        val account = accountConnector.getAccountAtLatestBlock(address)

        assertNotNull(account, "Account should not be null")
        assertEquals(address.base16Value, account.address.base16Value, "Address should match")
        assertTrue(account.balance >= BigDecimal.ZERO, "Account balance should be non-negative")
    }

    @Test
    fun `Can fetch account from block by height 0`() {
        val address = testAccount.flowAddress
        val account = accountConnector.getAccountAtBlockHeight(address, 0)

        assertNotNull(account, "Account should not be null")
        assertEquals(address.base16Value, account.address.base16Value, "Address should match")
        assertTrue(account.balance >= BigDecimal.ZERO, "Account balance should be non-negative")
    }

    @Test
    fun `Can fetch account balance`() {
        val address = testAccount.flowAddress
        val balance = accountConnector.getAccountBalance(address)

        assertNotNull(balance, "Balance should not be null")
        assertTrue(balance >= BigDecimal.ZERO, "Balance should be non-negative")
    }
}
