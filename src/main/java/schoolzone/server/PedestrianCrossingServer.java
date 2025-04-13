package schoolzone.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import SchoolZoneTraffic.*;

import java.io.IOException;


/**
 *
 * @author ardau
 */

public class PedestrianCrossingServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(50502)
                .addService(new PedestrianCrossingServiceImpl())
                .build();

        System.out.println("Pedestrian Crossing gRPC Server started on port 50502...");
        server.start();
        server.awaitTermination();
    }

    static class PedestrianCrossingServiceImpl extends PedestrianCrossingServiceGrpc.PedestrianCrossingServiceImplBase {

        // Unary
        @Override
        public void getCrossingStatus(CrossingRequest request, StreamObserver<CrossingResponse> responseObserver) {
            CrossingResponse response = CrossingResponse.newBuilder()
                    .setStatus("Signal: GREEN")
                    .setSecondsRemaining(0)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        // Server Streaming
        @Override
        public void streamCrossingUpdates(CrossingRequest request, StreamObserver<CrossingResponse> responseObserver) {
            try {
                for (int i = 5; i >= 0; i--) {
                    CrossingResponse response = CrossingResponse.newBuilder()
                            .setStatus("WAIT")
                            .setSecondsRemaining(i)
                            .build();
                    responseObserver.onNext(response);
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            responseObserver.onCompleted();
        }

        // Client Streaming
        @Override
        public StreamObserver<PedestrianData> sendPedestrianData(final StreamObserver<PedestrianAck> responseObserver) {
            return new StreamObserver<PedestrianData>() {
                int total = 0;

                @Override
                public void onNext(PedestrianData value) {
                    total += value.getPedestrianCount();
                    System.out.println("Received data from " + value.getCrossingId() + ": " + value.getPedestrianCount() + " people");
                }

                @Override
                public void onError(Throwable t) {
                    System.err.println("Client error: " + t.getMessage());
                }

                @Override
                public void onCompleted() {
                    PedestrianAck ack = PedestrianAck.newBuilder()
                            .setSuccess(true)
                            .setMessage("Total pedestrians: " + total)
                            .build();
                    responseObserver.onNext(ack);
                    responseObserver.onCompleted();
                }
            };
        }

        // Bi-Directional Streaming
        @Override
        public StreamObserver<PedestrianData> adjustCrossingSignals(final StreamObserver<CrossingResponse> responseObserver) {
            return new StreamObserver<PedestrianData>() {
                @Override
                public void onNext(PedestrianData value) {
                    String status = value.getPedestrianCount() > 5 ? "EXTENDED GREEN" : "NORMAL";
                    CrossingResponse response = CrossingResponse.newBuilder()
                            .setStatus(status)
                            .setSecondsRemaining(value.getPedestrianCount() * 2)
                            .build();
                    responseObserver.onNext(response);
                }

                @Override
                public void onError(Throwable t) {
                    System.err.println("BiDi error: " + t.getMessage());
                }

                @Override
                public void onCompleted() {
                    responseObserver.onCompleted();
                    System.out.println("Bi-Directional stream completed.");
                }
            };
        }
    }
}
