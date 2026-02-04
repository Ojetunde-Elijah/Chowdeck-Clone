package com.pm.restaurantservice.grpc;

import com.pm.restaurantservice.model.MenuItem;
import com.pm.restaurantservice.repository.MenuItemRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restaurant.MenuItemRequest;
import restaurant.MenuItemResponse;
import restaurant.RestaurantGrpcServiceGrpc; // Updated to match your proto service name

@GrpcService
public class RestaurantGrpcServer extends RestaurantGrpcServiceGrpc.RestaurantGrpcServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(RestaurantGrpcServer.class);
    private final MenuItemRepository menuItemRepository;

    public RestaurantGrpcServer(MenuItemRepository menuItemRepository) {
        this.menuItemRepository = menuItemRepository;
    }

    @Override
    public void getMenuItem(MenuItemRequest request, StreamObserver<MenuItemResponse> responseObserver) {
        log.info("Received gRPC request to fetch Item ID: {}", request.getItemId());

        try {
            // 1. Fetch from Database
            MenuItem item = menuItemRepository.findById(request.getItemId())
                    .orElseThrow(() -> new RuntimeException("Menu Item not found"));

            // 2. Map Entity to gRPC Response
            MenuItemResponse response = MenuItemResponse.newBuilder()
                    .setId(item.getId())           // Maps to int64 in proto
                    .setName(item.getName())       // Maps to string in proto
                    .setPrice(item.getPrice())     // Maps to double in proto
                    .setIsAvailable(item.isAvailable()) // Maps to bool in proto
                    .build();

            // 3. Send Response
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (RuntimeException e) {
            log.error("Item not found: {}", e.getMessage());
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            log.error("Internal gRPC error: {}", e.getMessage());
            responseObserver.onError(Status.INTERNAL
                    .withDescription("An unexpected error occurred")
                    .asRuntimeException());
        }
    }
}