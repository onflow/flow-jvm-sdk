package org.onflow.examples.kotlin.verifySiganture.userSignature

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.onflow.examples.kotlin.verifySignature.userSignature.UserSignatureExample
import org.onflow.flow.common.test.*
import org.onflow.flow.sdk.*
import org.onflow.flow.sdk.cadence.BooleanField

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
internal class UserSignatureExampleTest {
    @FlowTestAccount
    lateinit var testAccount: TestAccount

    @FlowTestAccount
    lateinit var testAccount2: TestAccount

    @FlowTestClient
    lateinit var accessAPI: FlowAccessApi

    private lateinit var connector: UserSignatureExample

    @BeforeEach
    fun setup() {
        connector = UserSignatureExample(accessAPI)
    }

    @Test
    fun `Can verify user signature`() {
        val txResult = connector.verifyUserSignature(testAccount.flowAddress, testAccount2.flowAddress)

        if (txResult is BooleanField) {
            assertTrue(txResult.value!!, "Signature verification failed")
        } else {
            fail("Expected BooleanField but got ${txResult::class.simpleName}")
        }
    }

}
