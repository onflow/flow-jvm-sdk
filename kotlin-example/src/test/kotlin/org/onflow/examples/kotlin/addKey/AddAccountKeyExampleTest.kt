package org.onflow.examples.kotlin.addKey

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.onflow.examples.kotlin.AccessAPIConnector
import org.onflow.flow.common.test.FlowEmulatorProjectTest
import org.onflow.flow.common.test.FlowServiceAccountCredentials
import org.onflow.flow.common.test.FlowTestClient
import org.onflow.flow.common.test.TestAccount
import org.onflow.flow.sdk.*

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
internal class AddAccountKeyExampleTest {
    @FlowServiceAccountCredentials
    lateinit var serviceAccount: TestAccount

    @FlowTestClient
    lateinit var accessAPI: FlowAccessApi

    private lateinit var accessAPIConnector: AccessAPIConnector
    private lateinit var connector: AddAccountKeyExample

    @BeforeEach
    fun setup() {
        connector = AddAccountKeyExample(serviceAccount.privateKey, accessAPI)
        accessAPIConnector = AccessAPIConnector(serviceAccount.privateKey, accessAPI)
    }

    @Test
    fun `Can add key to account`() {
    }

}
