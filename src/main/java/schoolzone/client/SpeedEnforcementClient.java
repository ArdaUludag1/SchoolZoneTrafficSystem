/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package schoolzone.client;

/**
 *
 * @author ardau
 */
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import SchoolZoneTraffic.SpeedLimitEnforcementServiceGrpc;
import SchoolZoneTraffic.SpeedLimitEnforcementServiceOuterClass.*;

public class SpeedEnforcementClient {

    public static void main(String[] args) throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50503)
                .usePlaintext()
                .build();

        // UNARY RPC
        SpeedLimitEnforcementServiceGrpc.SpeedLimitEnforcementServiceBlockingStub stub =
                SpeedLimitEnforcementServiceGrpc.newBlockingStub(channel);

        SpeedRequest request = SpeedRequest.newBuilder()
                .setVehicleId("CAR-123")
                .setZoneId("ZONE-1")
                .build();

        SpeedResponse response = stub.getSpeedStatus(request);
        System.out.println("Vehicle: " + response.getVehicleId());
        System.out.println("Current Speed: " + response.getCurrentSpeed() + " km/h");
        System.out.println("Status: " + response.getStatus());

        // SERVER STREAMING RPC
        SpeedRequest streamRequest = SpeedRequest.newBuilder()
                .setVehicleId("CAR-456")
                .setZoneId("ZONE-2")
                .build();

        SpeedLimitEnforcementServiceGrpc.SpeedLimitEnforcementServiceStub asyncStub =
                SpeedLimitEnforcementServiceGrpc.newStub(channel);

        asyncStub.streamSpeedUpdates(streamRequest, new StreamObserver<SpeedResponse>() {
            @Override
            public void onNext(SpeedResponse res) {
                System.out.println("[STREAM] Vehicle: " + res.getVehicleId()
                        + ", Speed: " + res.getCurrentSpeed()
                        + " km/h, Status: " + res.getStatus());
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Streaming Error: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("[STREAM] Speed stream ended.");
            }
        });

        Thread.sleep(6000); // Wait for stream

        // CLIENT STREAMING RPC
        StreamObserver<SpeedRecord> uploadObserver = asyncStub.sendSpeedData(new StreamObserver<EnforcementSummary>() {
            @Override
            public void onNext(EnforcementSummary summary) {
                System.out.println("[UPLOAD COMPLETE]");
                System.out.println("Total Records: " + summary.getTotalRecords());
                System.out.println("Over Limit: " + summary.getOverLimitCount());
                System.out.println("Message: " + summary.getMessage());
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Client stream error: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                channel.shutdown();
            }
        });

        uploadObserver.onNext(SpeedRecord.newBuilder().setVehicleId("CAR-999").setSpeed(45).setTimestamp("10:00").build());
        uploadObserver.onNext(SpeedRecord.newBuilder().setVehicleId("CAR-999").setSpeed(62).setTimestamp("10:01").build());
        uploadObserver.onNext(SpeedRecord.newBuilder().setVehicleId("CAR-999").setSpeed(55).setTimestamp("10:02").build());

        uploadObserver.onCompleted();
        Thread.sleep(1000); // Wait for upload summary
    }
}
