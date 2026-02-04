package com.pm.userservice.grpc;

//import com.pm.grpc.user.UserProfileRequest;
//import com.pm.grpc.user.UserProfileResponse;
//import com.pm.grpc.user.UserProfileServiceGrpc;
import com.pm.userservice.model.UserProfile;
//import com.pm.userservice.repository.UserProfileRepository;
import com.pm.userservice.repository.UserRepository;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import userservice.UserProfileRequest;
import userservice.UserProfileResponse;

import java.util.UUID;

@GrpcService // Tells Spring Boot to start a gRPC server on the configured port
public class UserProfileGrpcServer extends userservice.UserProfileServiceGrpc.UserProfileServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(UserProfileGrpcServer.class);
    private final UserRepository profileRepository;

    public UserProfileGrpcServer(UserRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Override
    public void createProfile(userservice.UserProfileRequest request, StreamObserver<UserProfileResponse> responseObserver) {
        log.info("Received gRPC request to create profile for User ID: {}", request.getUserId());

        try {
            // 1. Map request to Entity
            UserProfile profile = new UserProfile();
            profile.setId(UUID.fromString(request.getUserId()));
            profile.setFirstName(request.getFirstName());
            profile.setLastName(request.getLastName());
            profile.setPhoneNumber(request.getPhone());

            // 2. Save to User-Service H2 Database
            profileRepository.save(profile);

            // 3. Send Success Response
            userservice.UserProfileResponse response = userservice.UserProfileResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Profile created successfully in User-Service")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to create profile: {}", e.getMessage());

            // Send Failure Response
            userservice.UserProfileResponse response = userservice.UserProfileResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Error: " + e.getMessage())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}