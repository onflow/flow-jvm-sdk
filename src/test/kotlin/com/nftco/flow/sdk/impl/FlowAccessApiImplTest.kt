package com.nftco.flow.sdk.impl

import com.google.protobuf.ByteString
import com.nftco.flow.sdk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.onflow.protobuf.access.Access
import org.onflow.protobuf.access.AccessAPIGrpc
import org.onflow.protobuf.entities.AccountOuterClass
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.math.BigDecimal
import java.time.LocalDateTime

class FlowAccessApiImplTest {
    private lateinit var flowAccessApiImpl: FlowAccessApiImpl
    private lateinit var mockApi: AccessAPIGrpc.AccessAPIBlockingStub
    private lateinit var outputStreamCaptor: ByteArrayOutputStream
    private lateinit var originalOut: PrintStream

    private val api = mock(AccessAPIGrpc.AccessAPIBlockingStub::class.java)
    private val flowAccessApi = FlowAccessApiImpl(api)

    @BeforeEach
    fun setUp() {
        mockApi = mock(AccessAPIGrpc.AccessAPIBlockingStub::class.java)
        flowAccessApiImpl = FlowAccessApiImpl(mockApi)
        outputStreamCaptor = ByteArrayOutputStream()
        originalOut = System.out
        System.setOut(PrintStream(outputStreamCaptor))
    }

    @AfterEach
    fun tearDown() {
        System.setOut(originalOut)
    }

    @Test
    fun `Test ping`() {
        flowAccessApi.ping()
        verify(api).ping(Access.PingRequest.newBuilder().build())
    }

    @Test
    fun `Test getLatestBlockHeader`() {
        val mockBlockHeader = FlowBlockHeader(FlowId("01"), FlowId("01"), 123L)

        val blockHeaderProto = Access.BlockHeaderResponse.newBuilder().setBlock(mockBlockHeader.builder().build()) // Pass the builder instance of mockBlockHeader
            .build()

        `when`(api.getLatestBlockHeader(any())).thenReturn(blockHeaderProto)

        val latestBlockHeader = flowAccessApi.getLatestBlockHeader(sealed = true)

        assertEquals(mockBlockHeader, latestBlockHeader)
    }

    @Test
    fun `Test getBlockHeaderById`() {
        val blockId = FlowId("01")
        val mockBlockHeader = FlowBlockHeader(blockId, FlowId("01"), 123L)

        val blockHeaderProto = Access.BlockHeaderResponse.newBuilder().setBlock(mockBlockHeader.builder().build()).build()

        `when`(api.getBlockHeaderByID(any())).thenReturn(blockHeaderProto)

        val blockHeader = flowAccessApi.getBlockHeaderById(blockId)

        assertEquals(mockBlockHeader, blockHeader)
    }

    @Test
    fun `Test getBlockHeaderByHeight`() {
        val height = 123L
        val mockBlockHeader = FlowBlockHeader(FlowId("01"), FlowId("01"), height)

        val blockHeaderProto = Access.BlockHeaderResponse.newBuilder().setBlock(mockBlockHeader.builder().build()).build()

        `when`(api.getBlockHeaderByHeight(any())).thenReturn(blockHeaderProto)

        val blockHeader = flowAccessApi.getBlockHeaderByHeight(height)

        assertEquals(mockBlockHeader, blockHeader)
    }

    @Test
    fun `Test getLatestBlock`() {
        val mockBlock = FlowBlock(FlowId("01"), FlowId("01"), 123L, LocalDateTime.now(), emptyList(), emptyList(), emptyList())

        val blockProto = Access.BlockResponse.newBuilder().setBlock(mockBlock.builder().build()).build()

        `when`(api.getLatestBlock(any())).thenReturn(blockProto)

        val latestBlock = flowAccessApi.getLatestBlock(sealed = true)

        assertEquals(mockBlock, latestBlock)
    }

    @Test
    fun `Test getBlockById`() {

        val blockId = FlowId("01")
        val mockBlock = FlowBlock(blockId, FlowId("01"), 123L, LocalDateTime.now(), emptyList(), emptyList(), emptyList())

        val blockProto = Access.BlockResponse.newBuilder()
            .setBlock(mockBlock.builder().build())
            .build()

        `when`(api.getBlockByID(any())).thenReturn(blockProto)


        val block = flowAccessApi.getBlockById(blockId)

        assertEquals(mockBlock, block)
    }

    @Test
    fun `Test getBlockByHeight`() {

        val height = 123L
        val mockBlock = FlowBlock(FlowId("01"), FlowId("01"), height, LocalDateTime.now(), emptyList(), emptyList(), emptyList())

        val blockProto = Access.BlockResponse.newBuilder()
            .setBlock(mockBlock.builder().build())
            .build()

        `when`(api.getBlockByHeight(any())).thenReturn(blockProto)

        val block = flowAccessApi.getBlockByHeight(height)

        assertEquals(mockBlock, block)
    }

    @Test
    fun `Test getCollectionById`() {

        val collectionId = FlowId("01")
        val mockCollection = FlowCollection(collectionId, emptyList())

        val collectionProto = Access.CollectionResponse.newBuilder()
            .setCollection(mockCollection.builder().build())
            .build()

        `when`(api.getCollectionByID(any())).thenReturn(collectionProto)

        val collection = flowAccessApi.getCollectionById(collectionId)

        assertEquals(mockCollection, collection)
    }

    @Test
    fun `Test sendTransaction`() {

        val mockTransaction = FlowTransaction(
            FlowScript("script"),
            emptyList(),
            FlowId.of("01".toByteArray()),
            123L,
            FlowTransactionProposalKey(FlowAddress("02"), 1, 123L),
            FlowAddress("02"),
            emptyList()
        )

        val transactionProto = Access.SendTransactionResponse.newBuilder()
            .setId(ByteString.copyFromUtf8("01"))
            .build()

        `when`(api.sendTransaction(any())).thenReturn(transactionProto)

        val transactionId = flowAccessApi.sendTransaction(mockTransaction)

        assertEquals(FlowId.of("01".toByteArray()), transactionId)
    }

    @Test
    fun `Test getTransactionById`() {
        val flowId = FlowId("01")

        val flowTransaction = FlowTransaction(
            FlowScript("script"),
            emptyList(),
            flowId,
            123L,
            FlowTransactionProposalKey(FlowAddress("02"), 1, 123L),
            FlowAddress("02"),
            emptyList()
        )
        val transactionProto = Access.TransactionResponse.newBuilder()
            .setTransaction(flowTransaction.builder().build())
            .build()

        `when`(api.getTransaction(any())).thenReturn(transactionProto)

        val retrievedTransaction = flowAccessApi.getTransactionById(flowId)

        assertEquals(flowTransaction, retrievedTransaction)
    }

//    @Test
//    fun `Test getTransactionResultById`() {
//        val flowId = FlowId("01")
//        val flowTransactionResult = FlowTransactionResult(FlowTransactionStatus.SEALED, 1, "message",emptyList())
//
//        val transactionResultProto = Access.TransactionResultResponse.newBuilder()
//
//            .setResult(flowTransactionResult.builder().build()) // Pass the builder instance of flowTransactionResult
//            .build()
//
//        `when`(api.getTransactionResult(any())).thenReturn(transactionResultProto)
//
//        val retrievedTransactionResult = flowAccessApi.getTransactionResultById(flowId)
//
//        assertEquals(flowTransactionResult, retrievedTransactionResult)
//    }

    @Test
    fun `Test getAccountByAddress`() {
        val flowAddress = FlowAddress("01")
        val flowAccount = FlowAccount(flowAddress, BigDecimal.ONE,FlowCode("code".toByteArray()), emptyList(), emptyMap())

        val accountProto = Access.GetAccountResponse.newBuilder()
            .setAccount(flowAccount.builder().build())
            .build()

        `when`(api.getAccount(any())).thenReturn(accountProto)

        val retrievedAccount = flowAccessApi.getAccountByAddress(flowAddress)

        assertEquals(flowAccount.address, retrievedAccount?.address)
        assertEquals(flowAccount.balance.stripTrailingZeros(), retrievedAccount?.balance?.stripTrailingZeros())
        assertEquals(flowAccount.keys, retrievedAccount?.keys)
        assertEquals(flowAccount.contracts, retrievedAccount?.contracts)
    }

    @Test
    fun `Test getAccountAtLatestBlock`() {
        val flowAddress = FlowAddress("01")
        val flowAccount = FlowAccount(flowAddress, BigDecimal.ONE,FlowCode("code".toByteArray()), emptyList(), emptyMap())

        val accountProto = Access.AccountResponse.newBuilder()
            .setAccount(flowAccount.builder().build())
            .build()

        `when`(api.getAccountAtLatestBlock(any())).thenReturn(accountProto)

        val retrievedAccount = flowAccessApi.getAccountAtLatestBlock(flowAddress)

        assertEquals(flowAccount.address, retrievedAccount?.address)
        assertEquals(flowAccount.balance.stripTrailingZeros(), retrievedAccount?.balance?.stripTrailingZeros())
        assertEquals(flowAccount.keys, retrievedAccount?.keys)
        assertEquals(flowAccount.contracts, retrievedAccount?.contracts)
    }

    @Test
    fun `Test getAccountByBlockHeight`() {
        val flowAddress = FlowAddress("01")
        val flowAccount = FlowAccount(flowAddress, BigDecimal.ONE, FlowCode("code".toByteArray()), emptyList(), emptyMap())

        val height = 123L

        val accountProto = Access.AccountResponse.newBuilder()
            .setAccount(flowAccount.builder().build())
            .build()

        `when`(api.getAccountAtBlockHeight(any())).thenReturn(accountProto)

        val retrievedAccount = flowAccessApi.getAccountByBlockHeight(flowAddress, height)
        assertEquals(flowAccount.address, retrievedAccount?.address)
        assertEquals(flowAccount.balance.stripTrailingZeros(), retrievedAccount?.balance?.stripTrailingZeros())
        assertEquals(flowAccount.keys, retrievedAccount?.keys)
        assertEquals(flowAccount.contracts, retrievedAccount?.contracts)
    }

//    @Test
//    fun `Test executeScriptAtLatestBlock`() {
//        val script = FlowScript("script".toByteArray())
//        val arguments = listOf(ByteString.copyFrom(byteArrayOf(0x04, 0x05, 0x06)))
//
//        val flowScriptResponse = FlowScriptResponse("script".toByteArray())
//
//        val scriptProto = Access.ExecuteScriptResponse.newBuilder().
//            .setValue(script.)
//            .build()
//
//        `when`(api.executeScriptAtLatestBlock(any())).thenReturn(scriptProto)
//
//        val result = flowAccessApi.executeScriptAtLatestBlock(script, arguments)
//        assertEquals(flowScriptResponse, retrievedAccount)
//    }
}




