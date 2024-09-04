package org.onflow.examples.kotlin.verifySiganture.userSignatureValidateAny

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.onflow.examples.kotlin.verifySignature.userSignatureValidateAny.UserSignatureValidateAnyExample
import org.onflow.flow.common.test.*
import org.onflow.flow.sdk.*
import org.onflow.flow.sdk.cadence.BooleanField

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
internal class UserSignatureValidateAnyExampleTest {
    @FlowTestAccount
    lateinit var testAccount: TestAccount

    @FlowTestClient
    lateinit var accessAPI: FlowAccessApi

    private lateinit var connector: UserSignatureValidateAnyExample

    @BeforeEach
    fun setup() {
        connector = UserSignatureValidateAnyExample(accessAPI)
    }

    @Test
    fun `Can verify user signature`() {
        val txResult = connector.verifyUserSignatureValidateAny(testAccount.flowAddress, testAccount.privateKey, "ananas")

        if (txResult is BooleanField) {
            assertTrue(txResult.value!!, "Signature verification failed")
        } else {
            fail("Expected BooleanField but got ${txResult::class.simpleName}")
        }
    }
}
