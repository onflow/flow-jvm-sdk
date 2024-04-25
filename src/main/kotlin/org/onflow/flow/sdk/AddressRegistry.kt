package org.onflow.flow.sdk

/**
 * Contains addresses of contract/addresses on the blockchain and offers methods for processing scripts by performing
 * token replacement on them with the appropriate addresses.
 */
class AddressRegistry {
    companion object {
        const val FUNGIBLE_TOKEN = "0xFUNGIBLETOKEN"
        const val FLOW_TOKEN = "0xFLOWTOKEN"
        const val FLOW_FEES = "0xFLOWFEES"
        const val FLOW_TABLE_STAKING = "0xFLOWTABLESTAKING"
        const val LOCKED_TOKENS = "0xLOCKEDTOKENS"
        const val STAKING_PROXY = "0xSTAKINGPROXY"
        const val NON_FUNGIBLE_TOKEN = "0xNONFUNGIBLETOKEN"
        const val NFT_STOREFRONT = "0xNFTSTOREFRONT"
        const val TOKEN_FORWARDING = "0xTOKENFORWARDING"
    }

    private val SCRIPT_TOKEN_MAP: MutableMap<FlowChainId, MutableMap<String, FlowAddress>> = mutableMapOf()

    var defaultChainId = Flow.DEFAULT_CHAIN_ID

    init {
        registerDefaults()
    }

    @JvmOverloads
    fun processScript(script: String, chainId: FlowChainId = defaultChainId, addresses: Map<String, FlowAddress> = mapOf()): String {
        var ret = script
        SCRIPT_TOKEN_MAP[chainId]?.forEach {
            ret = ret.replace(it.key, it.value.formatted)
        }
        addresses.forEach {
            ret = ret.replace(it.key, it.value.formatted)
        }
        return ret
    }

    @JvmOverloads
    fun addressOf(contract: String, chainId: FlowChainId = defaultChainId): FlowAddress? = SCRIPT_TOKEN_MAP[chainId]?.get(contract)

    @JvmOverloads
    fun register(contract: String, address: FlowAddress, chainId: FlowChainId = defaultChainId): AddressRegistry {
        SCRIPT_TOKEN_MAP.computeIfAbsent(chainId) { mutableMapOf() }[contract] = address
        return this
    }

    @JvmOverloads
    fun deregister(contract: String, chainId: FlowChainId? = null): AddressRegistry {
        val chains = if (chainId != null) {
            arrayOf(chainId)
        } else {
            FlowChainId.entries.toTypedArray()
        }
        chains.forEach { SCRIPT_TOKEN_MAP[it]?.remove(contract) }
        return this
    }

    fun clear(): AddressRegistry {
        SCRIPT_TOKEN_MAP.clear()
        return this
    }

    fun registerDefaults(): AddressRegistry {
        mapOf(
            FlowChainId.EMULATOR to mutableMapOf(
                FUNGIBLE_TOKEN to FlowAddress("0xee82856bf20e2aa6"),
                FLOW_TOKEN to FlowAddress("0x0ae53cb6e3f42a79"),
                FLOW_FEES to FlowAddress("0xe5a8b7f23e8b548f")
            ),
            FlowChainId.TESTNET to mutableMapOf(
                FUNGIBLE_TOKEN to FlowAddress("0x9a0766d93b6608b7"),
                FLOW_TOKEN to FlowAddress("0x7e60df042a9c0868"),
                FLOW_FEES to FlowAddress("0x912d5440f7e3769e"),
                FLOW_TABLE_STAKING to FlowAddress("0x9eca2b38b18b5dfe"),
                LOCKED_TOKENS to FlowAddress("0x95e019a17d0e23d7"),
                STAKING_PROXY to FlowAddress("0x7aad92e5a0715d21"),
                NON_FUNGIBLE_TOKEN to FlowAddress("0x631e88ae7f1d7c20"),
                NFT_STOREFRONT to FlowAddress("0x94b06cfca1d8a476")
            ),
            FlowChainId.MAINNET to mutableMapOf(
                FUNGIBLE_TOKEN to FlowAddress("0xf233dcee88fe0abe"),
                FLOW_TOKEN to FlowAddress("0x1654653399040a61"),
                FLOW_FEES to FlowAddress("0xf919ee77447b7497"),
                FLOW_TABLE_STAKING to FlowAddress("0x8624b52f9ddcd04a"),
                LOCKED_TOKENS to FlowAddress("0x8d0e87b65159ae63"),
                STAKING_PROXY to FlowAddress("0x62430cf28c26d095"),
                NON_FUNGIBLE_TOKEN to FlowAddress("0x1d7e57aa55817448"),
                NFT_STOREFRONT to FlowAddress("0x4eb8a10cb9f87357"),
                TOKEN_FORWARDING to FlowAddress("0xe544175ee0461c4b")
            ),
        ).forEach { chain ->
            chain.value.forEach {
                register(it.key, it.value, chain.key)
            }
        }
        return this
    }
}
