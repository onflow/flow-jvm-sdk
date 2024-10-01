package org.onflow.examples.java.streaming.streamEvents;

import kotlin.coroutines.CoroutineContext;
import kotlinx.coroutines.*;
import kotlinx.coroutines.channels.ReceiveChannel;
import org.onflow.examples.java.AccessAPIConnector;
import org.onflow.flow.sdk.*;
import org.onflow.flow.sdk.crypto.PrivateKey;
import kotlin.Triple;

import java.util.List;
import java.util.concurrent.CancellationException;

public class SubscribeEventsExample {

    private final FlowAccessApi accessAPI;
    private final AccessAPIConnector connector;
    private final Job parentJob;  // Declare the parent Job for the scope

    public SubscribeEventsExample(PrivateKey privateKey, FlowAccessApi accessApiConnection) {
        this.accessAPI = accessApiConnection;
        this.connector = new AccessAPIConnector(privateKey, accessAPI);
        this.parentJob = JobKt.Job(null);
    }

    private CoroutineScope createScope() {
        CoroutineContext context = Dispatchers.getDefault().plus((CoroutineContext) parentJob);
        return CoroutineScopeKt.CoroutineScope(context);
    }

    public void streamEvents(List<FlowEvent> receivedEvents) throws Exception {
        CoroutineScope scope = createScope();
        FlowId blockId = connector.getLatestBlockID();

        // Subscribe to events using the scope and blockId
        Triple<ReceiveChannel<List<FlowEvent>>, ReceiveChannel<Throwable>, Job> result = accessAPI.subscribeEventsByBlockId(scope, blockId);
        ReceiveChannel<List<FlowEvent>> dataChannel = result.getFirst();
        ReceiveChannel<Throwable> errorChannel = result.getSecond();
        Job job = result.getThird();

        processEvents(scope, dataChannel, errorChannel, receivedEvents);

        // Cancel the job when done
        job.cancel(new CancellationException("Job cancelled manually"));
    }

    private void processEvents(
            CoroutineScope scope,
            ReceiveChannel<List<FlowEvent>> dataChannel,
            ReceiveChannel<Throwable> errorChannel,
            List<FlowEvent> receivedEvents
    ) {
        Job coroutineJob = parentJob;

        Job dataJob = BuildersKt.launch(scope, Dispatchers.getDefault(), CoroutineStart.DEFAULT, (coroutineScope1, continuation) -> {
            try {
                // Loop through dataChannel and process events
                while (!dataChannel.isClosedForReceive()) {
                    List<FlowEvent> events = BuildersKt.runBlocking(scope.getCoroutineContext(), (suspensionPoint) -> dataChannel.receive(null));
                    if (!coroutineJob.isActive()) break;

                    if (events != null && !events.isEmpty()) {
                        receivedEvents.addAll(events);
                    }

                    Thread.yield();
                }
            } catch (CancellationException e) {
                System.out.println("Data channel processing cancelled");
            } finally {
                System.out.println("Data channel processing finished");
                dataChannel.cancel(null);
            }
            return null;
        });

        Job errorJob = BuildersKt.launch(scope, Dispatchers.getDefault(), CoroutineStart.DEFAULT, (coroutineScope1, continuation) -> {
            try {
                while (!errorChannel.isClosedForReceive()) {
                    Throwable error = BuildersKt.runBlocking(scope.getCoroutineContext(), (suspensionPoint) -> errorChannel.receive(null));
                    if (error != null) {
                        System.out.println("~~~ ERROR: " + error.getMessage() + " ~~~");
                    }
                    if (!coroutineJob.isActive()) break;
                    Thread.yield();
                }
            } catch (CancellationException e) {
                System.out.println("Error channel processing cancelled");
            } finally {
                System.out.println("Error channel processing finished");
                errorChannel.cancel(null);
            }
            return null;
        });

        dataJob.join(null);
        errorJob.join(null);
    }

}

