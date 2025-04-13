/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package schoolzone.client;

import SchoolZoneTraffic.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

/**
 *
 * @author ardau
 */

public class TrafficSignalClient {

    public static void main(String[] args) throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50052)
                .usePlaintext()
                .build();

        TrafficSignalServiceGrpc.TrafficSignalServiceBlockingStub blockingStub =
                TrafficSignalServiceGrpc.newBlockingStub(channel);
        TrafficSignalServiceGrpc.TrafficSignalServiceStub asyncStub =
                TrafficSignalServiceGrpc.newStub(channel);

        // Unary RPC
        SignalRequest request = SignalRequest.newBuilder().setIntersectionId("INT-101").build();
        SignalResponse response = blockingStub.getCurrentSignal(request);
        System.out.println("[UNARY] Signal: " + response.getStatus());

        // Server Streaming
        asyncStub.streamSignalCycle(request, new StreamObserver<SignalResponse>() {
            @Override
            public void onNext(SignalResponse res) {
                System.out.println("[STREAM] " + res.getStatus() + " for " + res.getCountdownSeconds() + "s");
            }
            @Override
            public void onError(Throwable t) {}
            @Override
            public void onCompleted() {
                System.out.println("[STREAM] Done.");
            }
        });

        Thread.sleep(4000);

        // Client Streaming
        StreamObserver<SignalEvent> clientStream;
        clientStream = asyncStub.reportTrafficEvents(new StreamObserver<SignalSummary>() {
            @Override
            public void onNext(SignalSummary summary) {
                System.out.println("[CLIENT STREAM] Total events: " + summary.getTotalEvents());
            }
            @Override
            public void onError(Throwable t) {}
            @Override
            public void onCompleted() {
                System.out.println("[CLIENT STREAM] Finished.");
            }
        });

        clientStream.onNext(SignalEvent.newBuilder().setIntersectionId("INT-101").setEventType("Accident").build());
        clientStream.onNext(SignalEvent.newBuilder().setIntersectionId("INT-102").setEventType("Heavy Traffic").build());
        clientStream.onCompleted();

        Thread.sleep(1000);

        // BiDi Streaming
        StreamObserver<SignalRequest> bidi = asyncStub.adjustSignalsLive(new StreamObserver<SignalResponse>() {
            @Override
            public void onNext(SignalResponse res) {
                System.out.println("[BIDI] " + res.getStatus());
            }
            @Override
            public void onError(Throwable t) {}
            @Override
            public void onCompleted() {
                System.out.println("[BIDI] Stream ended.");
                channel.shutdown();
            }
        });

        bidi.onNext(SignalRequest.newBuilder().setIntersectionId("INT-201").build());
        Thread.sleep(500);
        bidi.onNext(SignalRequest.newBuilder().setIntersectionId("INT-202").build());
        Thread.sleep(500);
        bidi.onCompleted();
    }
}