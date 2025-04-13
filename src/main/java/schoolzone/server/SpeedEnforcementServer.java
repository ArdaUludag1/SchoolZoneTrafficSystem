
package schoolzone.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import SchoolZoneTraffic.SpeedLimitEnforcementServiceGrpc;
import SchoolZoneTraffic.SpeedLimitEnforcementServiceOuterClass.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author ardau
 */
public class SpeedEnforcementServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        Server server;
        server = ServerBuilder.forPort(50503)
                .addService(new SpeedEnforcementServiceImpl())
                .build();

        System.out.println("Speed Limit Enforcement gRPC Server started on port 50503...");
        server.start();
        server.awaitTermination();
    }

    static class SpeedEnforcementServiceImpl extends SpeedLimitEnforcementServiceGrpc.SpeedLimitEnforcementServiceImplBase {

        private final Map<String, Integer> speedLimits = new HashMap<>();
        private final Random random = new Random();

        public SpeedEnforcementServiceImpl() {
            speedLimits.put("ZONE-1", 50);
            speedLimits.put("ZONE-2", 30);
            speedLimits.put("ZONE-3", 60);
        }

        // Unary RPC
        @Override
        public void getSpeedStatus(SpeedRequest request, StreamObserver<SpeedResponse> responseObserver) {
            String vehicleId = request.getVehicleId();
            String zoneId = request.getZoneId();

            int currentSpeed = random.nextInt(81);
            int limit = speedLimits.getOrDefault(zoneId, 50);
            String status = (currentSpeed > limit) ? "Over Limit" : "OK";

            SpeedResponse response;
            response = SpeedResponse.newBuilder()
                    .setVehicleId(vehicleId)
                    .setCurrentSpeed(currentSpeed)
                    .setStatus(status)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            System.out.println("Checked speed for vehicle " + vehicleId + " in " + zoneId +
                    " → Speed: " + currentSpeed + " km/h, Status: " + status);
        }

        // Server Streaming RPC
        @Override
        public void streamSpeedUpdates(SpeedRequest request, StreamObserver<SpeedResponse> responseObserver) {
            String vehicleId = request.getVehicleId();
            String zoneId = request.getZoneId();

            int limit = speedLimits.getOrDefault(zoneId, 50);

            System.out.println("Starting speed stream for " + vehicleId + " in " + zoneId);

            try {
                for (int i = 1; i <= 5; i++) {
                    int currentSpeed = random.nextInt(81);
                    String status = (currentSpeed > limit) ? "Over Limit" : "OK";

                    SpeedResponse response = SpeedResponse.newBuilder()
                            .setVehicleId(vehicleId)
                            .setCurrentSpeed(currentSpeed)
                            .setStatus(status)
                            .build();

                    responseObserver.onNext(response);
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            responseObserver.onCompleted();
            System.out.println("Speed stream completed for " + vehicleId);
        }

        // Client Streaming RPC
        @Override
        public StreamObserver<SpeedRecord> sendSpeedData(StreamObserver<EnforcementSummary> responseObserver) {
            return new StreamObserver<SpeedRecord>() {
                int total = 0;
                int overLimit = 0;

                @Override
                public void onNext(SpeedRecord record) {
                    total++;
                    int limit = speedLimits.getOrDefault("ZONE-1", 50); // default for now
                    if (record.getSpeed() > limit) {
                        overLimit++;
                    }
                    System.out.println("Received: " + record.getVehicleId() + " → " + record.getSpeed() + " km/h");
                }

                @Override
                public void onError(Throwable t) {
                    System.err.println("Client stream error: " + t.getMessage());
                }

                @Override
                public void onCompleted() {
                    EnforcementSummary summary = EnforcementSummary.newBuilder()
                            .setTotalRecords(total)
                            .setOverLimitCount(overLimit)
                            .setMessage("Data processed successfully")
                            .build();

                    responseObserver.onNext(summary);
                    responseObserver.onCompleted();

                    System.out.println("Stream completed. Records: " + total + ", Over Limit: " + overLimit);
                }
            };
        }
    }
}