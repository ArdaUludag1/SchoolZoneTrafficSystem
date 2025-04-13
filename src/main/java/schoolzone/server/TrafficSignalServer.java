package schoolzone.server;

import SchoolZoneTraffic.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;


/**
 *
 * @author ardau
 */
public class TrafficSignalServer {

    public static void main(String[] args) throws Exception {
        Server server = ServerBuilder.forPort(50052)
                .addService(new TrafficSignalServiceImpl())
                .build();

        System.out.println("🚦 Traffic Signal Server started on port 50052");
        server.start();
        server.awaitTermination();
    }

    static class TrafficSignalServiceImpl extends TrafficSignalServiceGrpc.TrafficSignalServiceImplBase {

        @Override
        public void getCurrentSignal(SignalRequest request, StreamObserver<SignalResponse> responseObserver) {
            SignalResponse response = SignalResponse.newBuilder()
                    .setStatus("GREEN")
                    .setCountdownSeconds(15)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void streamSignalCycle(SignalRequest request, StreamObserver<SignalResponse> responseObserver) {
            String[] sequence = {"GREEN", "YELLOW", "RED"};
            int[] timers = {10, 3, 7};
            try {
                for (int i = 0; i < sequence.length; i++) {
                    SignalResponse response = SignalResponse.newBuilder()
                            .setStatus(sequence[i])
                            .setCountdownSeconds(timers[i])
                            .build();
                    responseObserver.onNext(response);
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            responseObserver.onCompleted();
        }

        @Override
        public StreamObserver<SignalEvent> reportTrafficEvents(StreamObserver<SignalSummary> responseObserver) {
            return new StreamObserver<SignalEvent>() {
                int count = 0;

                @Override
                public void onNext(SignalEvent event) {
                    count++;
                    System.out.println("Event: " + event.getEventType() + " at " + event.getIntersectionId());
                }

                @Override
                public void onError(Throwable t) {
                    System.err.println("Error in client streaming: " + t.getMessage());
                }

                @Override
                public void onCompleted() {
                    SignalSummary summary = SignalSummary.newBuilder()
                            .setTotalEvents(count)
                            .setMessage("Events processed.")
                            .build();
                    responseObserver.onNext(summary);
                    responseObserver.onCompleted();
                }
            };
        }

        @Override
        public StreamObserver<SignalRequest> adjustSignalsLive(StreamObserver<SignalResponse> responseObserver) {
            return new StreamObserver<SignalRequest>() {
                @Override
                public void onNext(SignalRequest request) {
                    SignalResponse response = SignalResponse.newBuilder()
                            .setStatus("Adjusted for " + request.getIntersectionId())
                            .setCountdownSeconds(5)
                            .build();
                    responseObserver.onNext(response);
                }

                @Override
                public void onError(Throwable t) {
                    System.err.println("Error in bidi stream: " + t.getMessage());
                }

                @Override
                public void onCompleted() {
                    responseObserver.onCompleted();
                    System.out.println("BiDi stream completed.");
                }
            };
        }
    }
}