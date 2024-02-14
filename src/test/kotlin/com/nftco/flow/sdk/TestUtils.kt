package com.nftco.flow.sdk

object TestUtils {
    fun newMainnetAccessApi(): FlowAccessApi = Flow.newAccessApi(MAINNET_HOSTNAME)

    fun newTestnetAccessApi(): FlowAccessApi = Flow.newAccessApi(TESTNET_HOSTNAME)

    val MAINNET_HOSTNAME = "access.mainnet.nodes.onflow.org"
    val TESTNET_HOSTNAME = "access.devnet.nodes.onflow.org"
}
