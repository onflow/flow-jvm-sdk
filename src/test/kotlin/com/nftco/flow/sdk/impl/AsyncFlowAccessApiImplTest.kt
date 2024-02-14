package com.nftco.flow.sdk.impl

import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.onflow.protobuf.access.Access
import org.onflow.protobuf.access.AccessAPIGrpc
import org.mockito.ArgumentMatchers.any

class AsyncFlowAccessApiImplTest {

    private val api = mock(AccessAPIGrpc.AccessAPIFutureStub::class.java)
    private val asyncFlowAccessApi = AsyncFlowAccessApiImpl(api)

    @Test
    fun `test ping`() {
        val pingResponse = Access.PingResponse.newBuilder().build()
        val future: ListenableFuture<Access.PingResponse> = SettableFuture.create()
        (future as SettableFuture<Access.PingResponse>).set(pingResponse)

        `when`(api.ping(any())).thenReturn(future)

        assertEquals(Unit, asyncFlowAccessApi.ping().get())
    }
}
