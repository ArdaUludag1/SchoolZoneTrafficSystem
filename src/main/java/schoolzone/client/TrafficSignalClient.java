package schoolzone.client;

import grpc.generated.schoolzone.*;
import grpc.generated.schoolzone.TrafficSignalServiceGrpc.TrafficSignalServiceStub;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.time.LocalTime;

/**
 *
 * @author ardau
 */

public class TrafficSignalClient {

    private static TrafficSignalServiceStub asyncStub;

    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50052)
                .usePlaintext()
                .build();

        asyncStub = TrafficSignalServiceGrpc.newStub(channel);

        reportTrafficEvents(); // CLIENT STREAMING BASED ON SENSOR DATA

        // optional: simulate client cancellation
        // channel.shutdownNow();
    }

    private static void reportTrafficEvents() {
        // Response observer to handle server's SignalSummary
        StreamObserver<SignalSummary> responseObserver = new StreamObserver<SignalSummary>() {
            @Override
            public void onNext(SignalSummary summary) {
                System.out.println(LocalTime.now() + ": ? Server Summary -> Events: " +
                        summary.getTotalEvents() + ", Highest Density Type: " + summary.getHighestDensityType());
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("? Client received error: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println(LocalTime.now() + ": ? Stream completed.");
            }
        };

        // Request observer to send multiple SignalEvent entries
        StreamObserver<SignalEvent> requestObserver = asyncStub.reportTrafficEvents(responseObserver);

        try {
            // First event: Vehicle sensor reports 15 vehicles
            requestObserver.onNext(SignalEvent.newBuilder()
                    .setSensorType("vehicle")
                    .setSensorValue(15)
                    .build());
            Thread.sleep(300);

            // Second event: Pedestrian sensor reports 12 people
            requestObserver.onNext(SignalEvent.newBuilder()
                    .setSensorType("pedestrian")
                    .setSensorValue(12)
                    .build());
            Thread.sleep(300);

            // Third event: Bicycle sensor reports 5 bikes
            requestObserver.onNext(SignalEvent.newBuilder()
                    .setSensorType("bicycle")
                    .setSensorValue(5)
                    .build());
            Thread.sleep(300);

            // ? Complete the stream
            requestObserver.onCompleted();

        } catch (StatusRuntimeException e) {
            System.err.println("?? Status exception: " + e.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
