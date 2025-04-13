
package schoolzone.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import SchoolZoneTraffic.*;

/**
 *
 * @author ardau
 */

public class PedestrianCrossingClient {

    public static void main(String[] args) throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50502)
                .usePlaintext()
                .build();

        PedestrianCrossingServiceGrpc.PedestrianCrossingServiceBlockingStub blockingStub =
                PedestrianCrossingServiceGrpc.newBlockingStub(channel);
        PedestrianCrossingServiceGrpc.PedestrianCrossingServiceStub asyncStub =
                PedestrianCrossingServiceGrpc.newStub(channel);

        // Unary
        CrossingRequest unaryRequest = CrossingRequest.newBuilder().setCrossingId("A1").build();
        CrossingResponse unaryResponse = blockingStub.getCrossingStatus(unaryRequest);
        System.out.println("[UNARY] " + unaryResponse.getStatus());

        // Server Streaming
        asyncStub.streamCrossingUpdates(unaryRequest, new StreamObserver<CrossingResponse>() {
            @Override
            public void onNext(CrossingResponse res) {
                System.out.println("[STREAM] Seconds remaining: " + res.getSecondsRemaining());
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Stream error: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("[STREAM] Countdown complete.");
            }
        });

        Thread.sleep(6000);

        // Client Streaming
        StreamObserver<PedestrianData> uploadStream = asyncStub.sendPedestrianData(new StreamObserver<PedestrianAck>() {
            @Override
            public void onNext(PedestrianAck ack) {
                System.out.println("[UPLOAD] " + ack.getMessage());
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Upload error: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("[UPLOAD] Client stream closed.");
            }
        });

        uploadStream.onNext(PedestrianData.newBuilder().setCrossingId("A1").setPedestrianCount(3).build());
        uploadStream.onNext(PedestrianData.newBuilder().setCrossingId("A1").setPedestrianCount(6).build());
        uploadStream.onCompleted();

        Thread.sleep(1000);

        // Bi-Directional Streaming
        StreamObserver<PedestrianData> bidiStream = asyncStub.adjustCrossingSignals(new StreamObserver<CrossingResponse>() {
            @Override
            public void onNext(CrossingResponse res) {
                System.out.println("[BiDi] Signal: " + res.getStatus() + " for " + res.getSecondsRemaining() + "s");
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("BiDi error: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("[BiDi] Stream ended.");
                channel.shutdown();
            }
        });

        bidiStream.onNext(PedestrianData.newBuilder().setCrossingId("A1").setPedestrianCount(2).build());
        Thread.sleep(500);
        bidiStream.onNext(PedestrianData.newBuilder().setCrossingId("A1").setPedestrianCount(8).build());
        Thread.sleep(500);
        bidiStream.onCompleted();

        Thread.sleep(1000);
    }
}