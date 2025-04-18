package schoolzone.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import SchoolZoneTraffic.*;

/**
 *
 * @author ardau
 */

public class PedestrianCrossingServer {

    public static void main(String[] args) throws Exception {
        Server server = ServerBuilder.forPort(50502)
                .addService(new PedestrianCrossingServiceImpl())
                .build();

        System.out.println("Pedestrian Crossing gRPC Server started on port 50502...");
        server.start();
        server.awaitTermination();
    }

    static class PedestrianCrossingServiceImpl extends PedestrianCrossingServiceGrpc.PedestrianCrossingServiceImplBase {

        // Unary RPC - handle single crossing request
        @Override
        public void getCrossingStatus(EmptyRequest request, StreamObserver<CrossingResponse> responseObserver) {
            CrossingResponse response = CrossingResponse.newBuilder()
                    .setStatus("Wait for green signal")
                    .setSecondsRemaining(0)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        // Client Streaming - receive multiple pedestrian data
        @Override
        public StreamObserver<PedestrianData> sendPedestrianData(StreamObserver<PedestrianAck> responseObserver) {
            return new StreamObserver<PedestrianData>() {
                int totalPedestrians = 0;

                @Override
                public void onNext(PedestrianData value) {
                    totalPedestrians += value.getPedestrianCount();
                }

                @Override
                public void onError(Throwable t) {
                    System.out.println("Error receiving pedestrian data: " + t.getMessage());
                }

                @Override
                public void onCompleted() {
                    String message = "Signal extended for " + totalPedestrians + " people.";

                    PedestrianAck response = PedestrianAck.newBuilder()
                            .setSuccess(true)
                            .setMessage(message)
                            .build();

                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                }
            };
        }

        // Server Streaming - send countdown from 10 to 0
        @Override
        public void streamCrossingUpdates(CountdownRequest request, StreamObserver<CrossingResponse> responseObserver) {
            int countdown = 10;

            for (int i = countdown; i >= 0; i--) {
                CrossingResponse response = CrossingResponse.newBuilder()
                        .setStatus("Countdown in progress")
                        .setSecondsRemaining(i)
                        .build();

                responseObserver.onNext(response);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }

            responseObserver.onCompleted();
        }

        // Bi-Directional Streaming - handle pedestrian actions and respond
        @Override
        public StreamObserver<PedestrianAction> streamPedestrianStatus(StreamObserver<CrossingResponse> responseObserver) {
            return new StreamObserver<PedestrianAction>() {

                @Override
                public void onNext(PedestrianAction action) {
                    String input = action.getAction().toLowerCase();
                    String message;

                    switch (input) {
                        case "waiting":
                            message = "Please wait at the signal.";
                            break;
                        case "started crossing":
                            message = "You may cross now.";
                            break;
                        case "crossed during red":
                            message = "Warning: You crossed during red.";
                            break;
                        case "crossing completed":
                            message = "Crossing completed successfully.";
                            break;
                        default:
                            message = "Unknown action.";
                    }

                    CrossingResponse response = CrossingResponse.newBuilder()
                            .setStatus(message)
                            .setSecondsRemaining(0)
                            .build();

                    responseObserver.onNext(response);
                }
                @Override
                public void onError(Throwable t) {
                    System.out.println("Error in pedestrian status stream: " + t.getMessage());
                }
                @Override
                public void onCompleted() {
                    responseObserver.onCompleted();
                }
            };
        }
    }
}
