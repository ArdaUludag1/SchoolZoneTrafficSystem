package schoolzone.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import grpc.generated.schoolzone.*;

import java.io.IOException;
import java.util.*;
/**
 *
 * @author ardau
 */
public class TrafficSignalServer extends TrafficSignalServiceGrpc.TrafficSignalServiceImplBase {

    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder
                .forPort(50052)
                .addService(new TrafficSignalServer())
                .build();

        System.out.println(" TrafficSignalServer started on port 50052");
        server.start();
        server.awaitTermination();
    }

    // ? Unary RPC - Get current signal state
    @Override
    public void getCurrentSignal(SignalRequest request, StreamObserver<SignalResponse> responseObserver) {
        String location = request.getLocation().toLowerCase();

        SignalStatus status = SignalStatus.RED;
        int timeUntilGreen = 0;
        int greenDuration = 0;

        if (!location.equals("zone a") && !location.equals("zone b") && !location.equals("zone c")) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription(" Invalid location. Please try again with Zone A, B, or C.")
                    .asRuntimeException());
            return;
        }

        if (location.equals("zone a")) {
            status = SignalStatus.RED;
            timeUntilGreen = 8;
        } else if (location.equals("zone b")) {
            status = SignalStatus.YELLOW;
            timeUntilGreen = 3;
        } else if (location.equals("zone c")) {
            status = SignalStatus.GREEN;
            greenDuration = 10;
        }

        SignalResponse response = SignalResponse.newBuilder()
                .setLocation(location)
                .setStatus(status)
                .setTimeUntilGreen(timeUntilGreen)
                .setGreenDuration(greenDuration)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    // ? Server Streaming RPC - Stream full signal cycle
    @Override
    public void streamSignalCycle(SignalCycleRequest request, StreamObserver<SignalCycleResponse> responseObserver) {
        String location = request.getLocation().toLowerCase();
        int repeatCount = request.getRepeatCount();

        if (!location.equals("zone a") && !location.equals("zone b") && !location.equals("zone c")) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("? Invalid location. Please try again with Zone A, B, or C.")
                    .asRuntimeException());
            return;
        }

        List<SignalStatus> signalCycle = Arrays.asList(
                SignalStatus.GREEN,
                SignalStatus.YELLOW,
                SignalStatus.RED
        );

        try {
            for (int i = 0; i < repeatCount; i++) {
                Collections.shuffle(signalCycle); //  Random cycle

                for (SignalStatus status : signalCycle) {
                    int duration = getDurationForStatus(status);

                    for (int sec = duration; sec > 0; sec--) {
                        SignalCycleResponse response = SignalCycleResponse.newBuilder()
                                .setStatus(status)
                                .setDurationSeconds(sec)
                                .build();

                        responseObserver.onNext(response);
                        Thread.sleep(1000); // simulate countdown
                    }
                }
            }

            responseObserver.onCompleted();

        } catch (InterruptedException e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription(" Streaming interrupted.")
                    .asRuntimeException());
            e.printStackTrace();
        }
    }

    private int getDurationForStatus(SignalStatus status) {
        switch (status) {
            case GREEN:
                return 5;
            case YELLOW:
                return 3;
            case RED:
                return 7;
            default:
                return 5;
        }
    }

    // ? Client Streaming RPC - Collect sensor data
    @Override
    public StreamObserver<SignalEvent> reportTrafficEvents(StreamObserver<SignalSummary> responseObserver) {

        Map<String, Integer> sensorTotals = new HashMap<>();
        final int[] totalEvents = {0};

        return new StreamObserver<SignalEvent>() {
            @Override
            public void onNext(SignalEvent event) {
                String type = event.getSensorType().toLowerCase();
                int value = event.getSensorValue();

                totalEvents[0]++;
                sensorTotals.put(type, sensorTotals.getOrDefault(type, 0) + value);
            }

            @Override
            public void onError(Throwable t) {
                System.err.println(" Error receiving sensor stream: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                String highestType = "none";
                int max = -1;

                for (Map.Entry<String, Integer> entry : sensorTotals.entrySet()) {
                    if (entry.getValue() > max) {
                        max = entry.getValue();
                        highestType = entry.getKey();
                    }
                }

                SignalSummary summary = SignalSummary.newBuilder()
                        .setTotalEvents(totalEvents[0])
                        .setHighestDensityType(highestType)
                        .build();

                responseObserver.onNext(summary);
                responseObserver.onCompleted();
            }
        };
    }

    // Bi-Directional Streaming RPC - Live signal adjustment
    @Override
    public StreamObserver<SignalAdjustRequest> adjustSignalsLive(StreamObserver<SignalAdjustResponse> responseObserver) {

        return new StreamObserver<SignalAdjustRequest>() {
            @Override
            public void onNext(SignalAdjustRequest request) {
                String location = request.getLocation().toLowerCase();
                SignalStatus requestedStatus = request.getRequestedStatus();

                String message;
                switch (requestedStatus) {
                    case GREEN:
                        message = " GREEN light extended in " + location;
                        break;
                    case YELLOW:
                        message = " YELLOW light maintained in " + location;
                        break;
                    case RED:
                        message = " RED light shortened in " + location;
                        break;
                    default:
                        message = " Invalid signal request";
                }

                SignalAdjustResponse response = SignalAdjustResponse.newBuilder()
                        .setLocation(location)
                        .setMessage(message)
                        .build();

                responseObserver.onNext(response);
            }

            @Override
            public void onError(Throwable t) {
                System.err.println(" Error in BiDi stream: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println(" Bi-Directional Stream closed by client.");
                responseObserver.onCompleted();
            }
        };
    }
}
