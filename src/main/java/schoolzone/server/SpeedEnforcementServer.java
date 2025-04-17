package schoolzone.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import SchoolZoneTraffic.SpeedLimitEnforcementService.SpeedAck;
import SchoolZoneTraffic.SpeedLimitEnforcementService.SpeedData;
import SchoolZoneTraffic.SpeedEnforcementServiceGrpc;
import SchoolZoneTraffic.SpeedLimitEnforcementService.SpeedRequest;
import SchoolZoneTraffic.SpeedLimitEnforcementService.SpeedResponse;
/**
 *
 * @author ardau
 */
public class SpeedEnforcementServer {

    private static int speedLimit = 50;
    private static int totalViolations = 0;

    private static final List<SpeedResponse> logEntries = new ArrayList<>();

    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(50503)
                .addService(new SpeedEnforcementServiceImpl())
                .build();

        System.out.println("Speed Enforcement gRPC Server started on port 50503...");
        server.start();
        server.awaitTermination();
    }

    static class SpeedEnforcementServiceImpl extends SpeedEnforcementServiceGrpc.SpeedEnforcementServiceImplBase {

        @Override
        public void getSpeedStatus(SpeedRequest request, StreamObserver<SpeedResponse> responseObserver) {
            SpeedResponse response = SpeedResponse.newBuilder()
                    .setZoneId(request.getZoneId())
                    .setSpeedLimit(speedLimit)
                    .setViolationDetected(false)
                    .setTotalViolationsToday(totalViolations)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public StreamObserver<SpeedData> sendSpeedData(StreamObserver<SpeedAck> responseObserver) {
            return new StreamObserver<SpeedData>() {

                int entries = 0;
                int localViolations = 0;

                @Override
                public void onNext(SpeedData data) {
                    entries++;

                    boolean violation = data.getCurrentSpeed() > speedLimit;
                    if (violation) {
                        totalViolations++;
                        localViolations++;
                    }

                    SpeedResponse entry = SpeedResponse.newBuilder()
                            .setVehicleId(data.getVehicleId())
                            .setCurrentSpeed(data.getCurrentSpeed())
                            .setViolationDetected(violation)
                            .build();

                    logEntries.add(entry);
                }

                @Override
                public void onError(Throwable t) {
                    System.out.println("Error receiving speed data: " + t.getMessage());
                }

                @Override
                public void onCompleted() {
                    String msg = "Speed recorded. Total " + entries + " entries. " + localViolations + " violations.";
                    SpeedAck ack = SpeedAck.newBuilder()
                            .setSuccess(true)
                            .setMessage(msg)
                            .build();

                    responseObserver.onNext(ack);
                    responseObserver.onCompleted();
                }
            };
        }

        @Override
        public void streamSpeedUpdates(SpeedRequest request, StreamObserver<SpeedResponse> responseObserver) {
            for (SpeedResponse entry : logEntries) {
                SpeedResponse response = SpeedResponse.newBuilder()
                        .setVehicleId(entry.getVehicleId())
                        .setCurrentSpeed(entry.getCurrentSpeed())
                        .setViolationDetected(entry.getViolationDetected())
                        .build();

                responseObserver.onNext(response);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            responseObserver.onCompleted();
        }

        @Override
public StreamObserver<SpeedData> adjustSpeedLimits(StreamObserver<SpeedResponse> responseObserver) {
    return new StreamObserver<SpeedData>() {

        @Override
        public void onNext(SpeedData data) {
            String condition = data.getZoneId().toLowerCase(); // Değiştirilen satır
            int newLimit;
            String reason;

            switch (condition) {
                case "heavy traffic":
                    newLimit = 30;
                    reason = "Speed limit updated to 30 km/h due to heavy traffic.";
                    break;
                case "school zone":
                    newLimit = 30;
                    reason = "Speed limit updated to 30 km/h due to school zone.";
                    break;
                case "road work":
                    newLimit = 30;
                    reason = "Speed limit updated to 30 km/h due to road work.";
                    break;
                case "clear road":
                default:
                    newLimit = 50;
                    reason = "Speed limit restored to 50 km/h due to clear road.";
                    break;
            }

            speedLimit = newLimit;

            SpeedResponse response = SpeedResponse.newBuilder()
                    .setSpeedLimit(newLimit)
                    .setReason(reason)
                    .build();

            responseObserver.onNext(response);
        }

        @Override
        public void onError(Throwable t) {
            System.out.println("Error in Bi-Directional stream: " + t.getMessage());
        }

        @Override
        public void onCompleted() {
            responseObserver.onCompleted();
        }
    };
}

    }
}