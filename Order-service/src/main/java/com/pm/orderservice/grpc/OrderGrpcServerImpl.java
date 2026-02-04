package com.pm.orderservice.grpc;

import com.pm.orderservice.Enum.OrderStatus;
import com.pm.orderservice.model.Order;
import com.pm.orderservice.repository.OrderRepository;
import io.grpc.stub.StreamObserver;
import jakarta.transaction.Transactional;
import net.devh.boot.grpc.server.service.GrpcService;
import order_service.OrderInternalServiceGrpc;
import order_service.PaymentStatusRequest;
import order_service.PaymentStatusResponse;

import java.util.UUID;

@GrpcService
public class OrderGrpcServerImpl extends OrderInternalServiceGrpc.OrderInternalServiceImplBase {

    private final OrderRepository orderRepository;

    public OrderGrpcServerImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public void updateOrderStatus(PaymentStatusRequest request, StreamObserver<PaymentStatusResponse> responseObserver) {
        try {
            Order order = orderRepository.findById(UUID.fromString(request.getOrderId()))
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            order.setStatus(OrderStatus.valueOf(request.getStatus()));
            orderRepository.save(order);

            PaymentStatusResponse response = PaymentStatusResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Order status updated to " + request.getStatus())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(PaymentStatusResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage(e.getMessage())
                    .build());
            responseObserver.onCompleted();
        }
    }
}