package org.onflow.examples.kotlin.verifySiganture.userSignatureValidateAll

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.onflow.examples.kotlin.verifySignature.userSignatureValidateAll.UserSignatureValidateAllExample
import org.onflow.flow.common.test.*
import org.onflow.flow.sdk.*
import org.onflow.flow.sdk.cadence.BooleanField

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
internal class UserSignatureValidateAllExampleTest {
    @FlowTestAccount
    lateinit var testAccount: TestAccount

    @FlowTestClient
    lateinit var accessAPI: FlowAccessApi

    private lateinit var connector: UserSignatureValidateAllExample

    @BeforeEach
    fun setup() {
        connector = UserSignatureValidateAllExample(accessAPI)
    }

    @Test
    fun `Can verify user signature`() {
        val txResult = connector.verifyUserSignatureValidateAll(testAccount.flowAddress, testAccount.privateKey)

        if (txResult is BooleanField) {
            assertTrue(txResult.value!!, "Signature verification failed")
        } else {
            fail("Expected BooleanField but got ${txResult::class.simpleName}")
        }
    }
}
