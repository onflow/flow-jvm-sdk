package org.onflow.flow.sdk

import org.onflow.flow.sdk.cadence.AddressField

object IntegrationTestUtils {
    fun loadScript(name: String): ByteArray =
        javaClass.classLoader.getResourceAsStream(name)?.use { it.readAllBytes() }
            ?: throw IllegalArgumentException("Script not found: $name")
    fun newMainnetAccessApi(): FlowAccessApi = Flow.newAccessApi(MAINNET_HOSTNAME)

    fun newTestnetAccessApi(): FlowAccessApi = Flow.newAccessApi(TESTNET_HOSTNAME)

    private const val MAINNET_HOSTNAME = "access.mainnet.nodes.onflow.org"
    private const val TESTNET_HOSTNAME = "access.devnet.nodes.onflow.org"

    var transaction = FlowTransaction(
        script = FlowScript("import 0xsomething \n {}"),
        arguments = listOf(FlowArgument(byteArrayOf(2, 2, 3)), FlowArgument(byteArrayOf(3, 3, 3))),
        referenceBlockId = FlowId.of(byteArrayOf(3, 3, 3, 6, 6, 6)),
        gasLimit = 44,
        proposalKey = FlowTransactionProposalKey(
            address = FlowAddress.of(byteArrayOf(4, 5, 4, 5, 4, 5)),
            keyIndex = 11,
            sequenceNumber = 7
        ),
        payerAddress = FlowAddress.of(byteArrayOf(6, 5, 4, 3, 2)),
        authorizers = listOf(FlowAddress.of(byteArrayOf(9, 9, 9, 9, 9)), FlowAddress.of(byteArrayOf(8, 9, 9, 9, 9)))
    )

    fun <T> handleResult(result: FlowAccessApi.AccessApiCallResponse<T>, errorMessage: String): T {
        return when (result) {
            is FlowAccessApi.AccessApiCallResponse.Success -> result.data ?: throw IllegalStateException("$errorMessage: result data is null")
            is FlowAccessApi.AccessApiCallResponse.Error -> throw IllegalStateException("$errorMessage: ${result.message}", result.throwable)
        }
    }

    fun getAccountAddressFromResult(result: Any): FlowAddress {
        val addressField = (result as List<*>).find { it is AddressField } as? AddressField
        return addressField?.value?.let { FlowAddress(it) }
            ?: throw IllegalStateException("AccountCreated event not found")
    }

    fun getAccount(api: FlowAccessApi, address: FlowAddress): FlowAccount {
        val result = api.getAccountAtLatestBlock(address)
        return handleResult(result, "Failed to get account at latest block")
    }
}
