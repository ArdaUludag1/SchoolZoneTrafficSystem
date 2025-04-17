/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package schoolzone.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.Scanner;

import SchoolZoneTraffic.SpeedLimitEnforcementService.SpeedAck;
import SchoolZoneTraffic.SpeedLimitEnforcementService.SpeedData;
import SchoolZoneTraffic.SpeedEnforcementServiceGrpc;
import SchoolZoneTraffic.SpeedLimitEnforcementService.SpeedRequest;
import SchoolZoneTraffic.SpeedLimitEnforcementService.SpeedResponse;

/**
 *
 * @author ardau
 */


public class SpeedEnforcementClient {
 private static final String ZONE_ID = "SchoolZoneA";

    public static void main(String[] args) throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50503)
                .usePlaintext()
                .build();

        SpeedEnforcementServiceGrpc.SpeedEnforcementServiceBlockingStub blockingStub =
                SpeedEnforcementServiceGrpc.newBlockingStub(channel);
        SpeedEnforcementServiceGrpc.SpeedEnforcementServiceStub asyncStub =
                SpeedEnforcementServiceGrpc.newStub(channel);

        Scanner scanner = new Scanner(System.in);

        // Unary RPC: Get current speed status
        System.out.println("Requesting current speed status...");
        SpeedRequest statusRequest = SpeedRequest.newBuilder()
                .setZoneId(ZONE_ID)
                .build();
        SpeedResponse statusResponse = blockingStub.getSpeedStatus(statusRequest);
        System.out.println("Speed Limit: " + statusResponse.getSpeedLimit() + " km/h");
        System.out.println("Violations Today: " + statusResponse.getTotalViolationsToday());

        // Client Streaming: Send multiple vehicle data
        StreamObserver<SpeedAck> ackObserver = new StreamObserver<SpeedAck>() {
            @Override
            public void onNext(SpeedAck value) {
                System.out.println("Server Response: " + value.getMessage());
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("Error in client stream: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("Speed data stream completed.\n");
            }
        };

        StreamObserver<SpeedData> dataStream = asyncStub.sendSpeedData(ackObserver);

        for (int i = 0; i < 2; i++) {
            System.out.print("\nEnter vehicle ID: ");
            String vehicleId = scanner.nextLine();

            System.out.print("Enter current speed (km/h): ");
            int speed = Integer.parseInt(scanner.nextLine());

            SpeedData data = SpeedData.newBuilder()
                    .setVehicleId(vehicleId)
                    .setZoneId(ZONE_ID)
                    .setCurrentSpeed(speed)
                    .build();

            dataStream.onNext(data);
        }

        dataStream.onCompleted();

        Thread.sleep(1000); // wait for stream to finish

        // Server Streaming: Get live log from server
        System.out.println("\nLive Violation Log:");
        SpeedRequest logRequest = SpeedRequest.newBuilder()
                .setZoneId(ZONE_ID)
                .build();

        blockingStub.streamSpeedUpdates(logRequest)
                .forEachRemaining(response -> {
                    String line = response.getVehicleId() + " - "
                            + response.getCurrentSpeed() + " km/h - "
                            + (response.getViolationDetected() ? "Fined" : "Safe");
                    System.out.println(line);
                });

        // Bi-Directional Streaming: Adjust speed limits based on conditions
        StreamObserver<SpeedResponse> bidiResponse = new StreamObserver<SpeedResponse>() {
            @Override
            public void onNext(SpeedResponse value) {
                System.out.println("AI Response: " + value.getReason());
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("Error in BiDi stream: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("AI Monitor ended.");
            }
        };

        StreamObserver<SpeedData> bidiStream = asyncStub.adjustSpeedLimits(bidiResponse);

        String[] conditions = {"Heavy Traffic", "School Zone", "Road Work", "Clear Road"};

        for (String condition : conditions) {
            System.out.println("\nSending condition: " + condition);
            SpeedData conditionData = SpeedData.newBuilder()
                    .setVehicleId("monitor")
                    .setZoneId(ZONE_ID)
                    .setCurrentSpeed(0)
                    .setCondition(condition)
                    .build();
            bidiStream.onNext(conditionData);
            Thread.sleep(1000);
        }

        bidiStream.onCompleted();
        Thread.sleep(1000);
        channel.shutdown();
    }

}
