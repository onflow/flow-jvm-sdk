package org.onflow.flow.sdk

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class AddressRegistryTest {
    private val registry = AddressRegistry()

    @BeforeEach
    fun setup() {
        Flow.configureDefaults()
        registry.defaultChainId = FlowChainId.MAINNET
        registry
            .clear()
            .registerDefaults()
    }

    @Test
    fun `Can register an address`() {
        val name = "SOME_ADDRESS"
        val address = FlowAddress("0x1a1f2e458a098135")

        assertNull(registry.addressOf(name))
        assertNull(registry.addressOf(name, FlowChainId.MAINNET))
        assertNull(registry.addressOf(name, FlowChainId.TESTNET))
        assertNull(registry.addressOf(name, FlowChainId.EMULATOR))

        registry.register(name, address)
        assertEquals(address, registry.addressOf(name))
        assertEquals(address, registry.addressOf(name, FlowChainId.MAINNET))
        assertNull(registry.addressOf(name, FlowChainId.TESTNET))
        assertNull(registry.addressOf(name, FlowChainId.EMULATOR))

        registry.register(name, address, FlowChainId.TESTNET)
        assertEquals(address, registry.addressOf(name))
        assertEquals(address, registry.addressOf(name, FlowChainId.MAINNET))
        assertEquals(address, registry.addressOf(name, FlowChainId.TESTNET))
        assertNull(registry.addressOf(name, FlowChainId.EMULATOR))

        registry.register(name, address, FlowChainId.EMULATOR)
        assertEquals(address, registry.addressOf(name))
        assertEquals(address, registry.addressOf(name, FlowChainId.MAINNET))
        assertEquals(address, registry.addressOf(name, FlowChainId.TESTNET))
        assertEquals(address, registry.addressOf(name, FlowChainId.EMULATOR))
    }

    @Test
    fun `Can deregister an address`() {
        assertNotNull(registry.addressOf(AddressRegistry.FUNGIBLE_TOKEN, FlowChainId.MAINNET))
        assertNotNull(registry.addressOf(AddressRegistry.FUNGIBLE_TOKEN, FlowChainId.TESTNET))
        assertNotNull(registry.addressOf(AddressRegistry.FUNGIBLE_TOKEN, FlowChainId.EMULATOR))

        registry.deregister(AddressRegistry.FUNGIBLE_TOKEN)
        assertNull(registry.addressOf(AddressRegistry.FUNGIBLE_TOKEN, FlowChainId.MAINNET))
        assertNull(registry.addressOf(AddressRegistry.FUNGIBLE_TOKEN, FlowChainId.TESTNET))
        assertNull(registry.addressOf(AddressRegistry.FUNGIBLE_TOKEN, FlowChainId.EMULATOR))
    }

    @Test
    fun `Can deregister an address for a specific chain`() {
        assertNotNull(registry.addressOf(AddressRegistry.FUNGIBLE_TOKEN, FlowChainId.MAINNET))
        assertNotNull(registry.addressOf(AddressRegistry.FUNGIBLE_TOKEN, FlowChainId.TESTNET))
        assertNotNull(registry.addressOf(AddressRegistry.FUNGIBLE_TOKEN, FlowChainId.EMULATOR))

        registry.deregister(AddressRegistry.FUNGIBLE_TOKEN, FlowChainId.TESTNET)
        assertNotNull(registry.addressOf(AddressRegistry.FUNGIBLE_TOKEN, FlowChainId.MAINNET))
        assertNull(registry.addressOf(AddressRegistry.FUNGIBLE_TOKEN, FlowChainId.TESTNET))
        assertNotNull(registry.addressOf(AddressRegistry.FUNGIBLE_TOKEN, FlowChainId.EMULATOR))
    }

    @Test
    fun `Can process a script`() {
        assertEquals(
            "fungibleToken: 0xee82856bf20e2aa6, flowToken: 0x0ae53cb6e3f42a79",
            registry.processScript(
                "fungibleToken: 0xFUNGIBLETOKEN, flowToken: 0xFLOWTOKEN",
                FlowChainId.EMULATOR
            )
        )
        assertEquals(
            "fungibleToken: 0x9a0766d93b6608b7, flowToken: 0x7e60df042a9c0868",
            registry.processScript(
                "fungibleToken: 0xFUNGIBLETOKEN, flowToken: 0xFLOWTOKEN",
                FlowChainId.TESTNET
            )
        )
        assertEquals(
            "fungibleToken: 0xf233dcee88fe0abe, flowToken: 0x1654653399040a61",
            registry.processScript(
                "fungibleToken: 0xFUNGIBLETOKEN, flowToken: 0xFLOWTOKEN",
                FlowChainId.MAINNET
            )
        )
    }
}
