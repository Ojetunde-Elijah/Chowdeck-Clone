package com.pm.authservice.grpc;


import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import userservice.UserProfileRequest;
import userservice.UserProfileResponse;
import userservice.UserProfileServiceGrpc;

@Service
public class UserProfileGrpcClient {
    private static final Logger log = LoggerFactory.getLogger(UserProfileGrpcClient.class);
    private final userservice.UserProfileServiceGrpc.UserProfileServiceBlockingStub blockingStub;

    // The constructor sets up the connection once when the app starts
    public UserProfileGrpcClient(
            @Value("${user.service.address:localhost}") String serverAddress,
            @Value("${user.service.grpc.port:7090}") int serverPort
    ){
        log.info("Connecting to User Service gRPC at {}:{}", serverAddress, serverPort);

        // Plaintext is required unless you've set up SSL certificates
        ManagedChannel channel = ManagedChannelBuilder.forAddress(serverAddress, serverPort)
                .usePlaintext()
                .build();

        blockingStub = userservice.UserProfileServiceGrpc.newBlockingStub(channel);
    }

    public userservice.UserProfileResponse createProfile(String userId, String firstName, String lastName, String phone) {
        userservice.UserProfileRequest request = userservice.UserProfileRequest.newBuilder()
                .setUserId(userId)
                .setFirstName(firstName)
                .setLastName(lastName)
                .setPhone(phone)
                .build();

        try {
            userservice.UserProfileResponse response = blockingStub.createProfile(request);
            log.info("Profile created successfully for User ID: {}", userId);
            return response;
        } catch (Exception e) {
            log.error("gRPC call failed: {}", e.getMessage());
            // Rethrow so the AuthService can catch it and rollback the transaction
            throw new RuntimeException("User-Service is unavailable: " + e.getMessage());
        }
    }
}