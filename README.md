# SchoolZoneTrafficSystem

This project is a gRPC-based Smart City simulation focused on improving safety and efficiency in a school zone environment. It includes three core services: Traffic Signal Controller, Pedestrian Crossing Management, and Speed Limit Enforcement.

## Project Summary

The system uses Java, gRPC, and Protocol Buffers to build distributed services with real-time interaction capabilities. Each service supports all four types of gRPC communication: Unary, Server Streaming, Client Streaming, and Bi-Directional Streaming.

## Features

### 1. Traffic Signal Controller
- Simulates signal changes at an intersection.
- Accepts commands to adjust signals.
- Streams signal states in real-time.
- Reports intersection traffic patterns.

### 2. Pedestrian Crossing Service
- Allows users to request crossing safely.
- Streams countdown before green light.
- Accepts total number of pedestrians.
- Adjusts signal behavior based on foot traffic.

### 3. Speed Limit Enforcement
- Simulates vehicles reporting their speed.
- Detects and flags speeding violations.
- Aggregates multiple reports from clients.
- Adjusts enforcement thresholds dynamically.

## Technologies Used

- Java 1.8
- Maven
- gRPC
- Protocol Buffers
- JmDNS (Service Discovery)
- SLF4J (Logging)

## Folder Structure

SchoolZoneTrafficSystem/
├── src/
│   └── main/
│       ├── java/
│       │   └── schoolzone/
│       │       ├── client/
│       │       └── server/
│       └── proto/
│           ├── TrafficSignalService.proto
│           ├── PedestrianCrossingService.proto
│           └── SpeedLimitEnforcementService.proto
├── pom.xml
├── .gitignore
└── README.md

## How to Run

1. Clone the project or download the source.
2. Place `.proto` files inside `src/main/proto/`.
3. Use `mvn clean install` to generate gRPC sources.
4. Run each server class one by one in NetBeans or via terminal.
5. Run the matching client classes to test each service.

## Requirements Checklist

- Three distinct gRPC services
- At least 12 gRPC methods:
  - Unary
  - Server Streaming
  - Client Streaming
  - Bi-Directional Streaming
- Maven for dependency management
- Service discovery using JmDNS
- Console output for service activity
- Separate proto files for each service
- Commit history maintained in GitHub
- Project exported as zip and hosted in GitHub
- Report written separately with method explanations and diagrams

## Author

Arda Uludag 
HDip in Computing Science # SchoolZoneTrafficSystem
