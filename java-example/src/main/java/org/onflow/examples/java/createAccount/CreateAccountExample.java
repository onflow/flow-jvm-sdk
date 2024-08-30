package org.onflow.examples.java.createAccount;

import org.onflow.examples.java.AccessAPIConnector;
import org.onflow.flow.sdk.*;
import org.onflow.flow.sdk.cadence.AddressField;
import org.onflow.flow.sdk.cadence.EventField;
import org.onflow.flow.sdk.cadence.StringField;
import org.onflow.flow.sdk.cadence.UInt8NumberField;
import org.onflow.flow.sdk.crypto.PrivateKey;
import org.onflow.flow.sdk.crypto.PublicKey;

import java.util.List;

import static org.onflow.examples.java.ExamplesUtils.loadScript;

public class CreateAccountExample {
    private final PrivateKey privateKey;
    private final FlowAccessApi accessAPI;
    private final AccessAPIConnector connector;

    public CreateAccountExample(PrivateKey privateKey, FlowAccessApi accessApiConnection) {
        this.privateKey = privateKey;
        this.accessAPI = accessApiConnection;
        this.connector = new AccessAPIConnector(privateKey, accessAPI);
    }

    public FlowAddress createAccount(FlowAddress payerAddress, PublicKey publicKey) {
        FlowAccountKey payerAccountKey = connector.getAccountKey(payerAddress, 0);
        FlowScript script = new FlowScript(loadScript("cadence/create_account.cdc"));
        List<FlowArgument> arguments = List.of(
                new FlowArgument(new StringField(publicKey.getHex())),
                new FlowArgument(new UInt8NumberField(Integer.toString(publicKey.getAlgo().getIndex())))
        );
        FlowTransaction tx = connector.createTransaction(payerAddress, payerAccountKey, script, arguments);
        FlowId txID = connector.signAndSendTransaction(tx, payerAddress, payerAccountKey);
        FlowTransactionResult txResult = connector.waitForSeal(txID);
        return getAccountCreatedAddress(txResult);
    }

    private FlowAddress getAccountCreatedAddress(FlowTransactionResult txResult) {
        String address = txResult.getEvents().stream()
                .filter(event -> event.getType().equals("flow.AccountCreated"))
                .findFirst()
                .map(event -> (EventField) event.getPayload().getJsonCadence())
                .map(eventField -> (AddressField) eventField.getValue().getRequiredField("address"))
                .map(AddressField::getValue)
                .orElseThrow(() -> new RuntimeException("Account creation event not found"));

        return new FlowAddress(address);
    }
}
