package schoolzone.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import SchoolZoneTraffic.*;
import java.util.Iterator;

import java.util.Scanner;

/**
 *
 * @author ardau
 */

public class PedestrianCrossingClient {

    public static void main(String[] args) throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50502)
                .usePlaintext()
                .build();

        PedestrianCrossingServiceGrpc.PedestrianCrossingServiceStub asyncStub =
                PedestrianCrossingServiceGrpc.newStub(channel);
        PedestrianCrossingServiceGrpc.PedestrianCrossingServiceBlockingStub blockingStub =
                PedestrianCrossingServiceGrpc.newBlockingStub(channel);

        Scanner scanner = new Scanner(System.in);

        System.out.println("1 - Request to cross");
        System.out.println("2 - Send pedestrian count");
        System.out.println("3 - Start countdown");
        System.out.println("4 - Stream pedestrian status");
        System.out.print("Choose option: ");
        int option = scanner.nextInt();
        scanner.nextLine();

        switch (option) {

            case 1:
                EmptyRequest request = EmptyRequest.newBuilder().build();
                CrossingResponse response = blockingStub.getCrossingStatus(request);
                System.out.println("Server says: " + response.getStatus());
                break;

            case 2:
                StreamObserver<PedestrianData> requestStream =
                        asyncStub.sendPedestrianData(new StreamObserver<PedestrianAck>() {
                            @Override
                            public void onNext(PedestrianAck ack) {
                                System.out.println("Server says: " + ack.getMessage());
                            }

                            @Override
                            public void onError(Throwable t) {
                                System.out.println("Error: " + t.getMessage());
                            }

                            @Override
                            public void onCompleted() {
                                System.out.println("Finished sending pedestrian data.");
                            }
                        });

                System.out.print("Enter number of pedestrians: ");
                int count = scanner.nextInt();
                for (int i = 0; i < count; i++) {
                    PedestrianData data = PedestrianData.newBuilder()
                            .setPedestrianCount(1)
                            .build();
                    requestStream.onNext(data);
                }
                requestStream.onCompleted();
                Thread.sleep(1000);
                break;

            case 3:
                CountdownRequest countdownRequest = CountdownRequest.newBuilder().build();
                Iterator<CrossingResponse> countdownResponses = blockingStub.streamCrossingUpdates(countdownRequest);
                while (countdownResponses.hasNext()) {
                    CrossingResponse countdown = countdownResponses.next();
                    System.out.println("Countdown: " + countdown.getSecondsRemaining());
                }
                break;

            case 4:
                StreamObserver<PedestrianAction> actionStream =
                        asyncStub.streamPedestrianStatus(new StreamObserver<CrossingResponse>() {
                            @Override
                            public void onNext(CrossingResponse value) {
                                System.out.println("Server: " + value.getStatus());
                            }

                            @Override
                            public void onError(Throwable t) {
                                System.out.println("Error: " + t.getMessage());
                            }

                            @Override
                            public void onCompleted() {
                                System.out.println("Live stream ended.");
                            }
                        });

                while (true) {
                    System.out.print("Enter action (waiting, started crossing, crossed during red, crossing completed, stop): ");
                    String input = scanner.nextLine();

                    if (input.equalsIgnoreCase("stop")) {
                        actionStream.onCompleted();
                        break;
                    }

                    PedestrianAction action = PedestrianAction.newBuilder()
                            .setAction(input)
                            .build();

                    actionStream.onNext(action);
                }
                break;

            default:
                System.out.println("Invalid option.");
        }

        channel.shutdown();
    }
}
