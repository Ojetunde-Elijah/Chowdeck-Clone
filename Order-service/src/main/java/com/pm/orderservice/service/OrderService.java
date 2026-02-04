package com.pm.orderservice.service;

import com.pm.orderservice.Enum.OrderStatus;
import com.pm.orderservice.dto.CartItemRequestDto;
import com.pm.orderservice.dto.OrderItemRequestDto;
import com.pm.orderservice.dto.OrderRequestDto;
import com.pm.orderservice.model.Order;
import com.pm.orderservice.model.OrderItem;
import com.pm.orderservice.repository.OrderRepository;
import net.devh.boot.grpc.client.inject.GrpcClient;
import notification_service.NotificationInternalServiceGrpc;
import notification_service.OrderNotificationRequest;
import org.springframework.stereotype.Service;
import restaurant.MenuItemRequest;
import restaurant.MenuItemResponse;
import restaurant.RestaurantGrpcServiceGrpc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private static final double STATIC_DELIVERY_FEE = 500.0;

    // You need a stub for the Notification Service too!
    @GrpcClient("notification-service")
    private NotificationInternalServiceGrpc.NotificationInternalServiceBlockingStub
            notificationStub;

    @GrpcClient("restaurant-service")
    private RestaurantGrpcServiceGrpc.RestaurantGrpcServiceBlockingStub restaurantStub;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;

        System.out.println("DEBUG: Restaurant Address Env: " + System.getenv("GRPC_CLIENT_RESTAURANT_SERVICE_ADDRESS"));
    }

    public Order placeOrder(String userId, OrderRequestDto request) {
        Order order = new Order();
        order.setUserId(UUID.fromString(userId));
        order.setRestaurantId(request.getRestaurantId());
//        order.setDeliveryAddress(request.getDeliveryAddress());
//        order.setUserEmail(request.getUserEmail());
        order.setStatus(OrderStatus.PENDING);

        double subtotal = 0;

        // Fix: Create a list to hold the actual OrderItem entities
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequestDto itemReq : request.getItems()) {
            MenuItemResponse itemData = restaurantStub.getMenuItem(
                    MenuItemRequest.newBuilder().setItemId(itemReq.getMenuItemId()).build()
            );

            if (!itemData.getIsAvailable()) {
                throw new RuntimeException("Item " + itemData.getName() + " is out of stock!");
            }

            subtotal += (itemData.getPrice() * itemReq.getQuantity());

            // Fix: Actually create the OrderItem entity
            OrderItem entityItem = new OrderItem();
            entityItem.setMenuItemId(itemReq.getMenuItemId());
            entityItem.setQuantity(itemReq.getQuantity());
            entityItem.setPriceAtPurchase(itemData.getPrice());
            entityItem.setOrder(order); // Link back to the parent order
            orderItems.add(entityItem);
        }

        order.setItems(orderItems); // Link items to order
        order.setSubTotal(subtotal);
        order.setDeliveryFee(STATIC_DELIVERY_FEE);
        order.setTotalAmount(subtotal + STATIC_DELIVERY_FEE);

        return orderRepository.save(order);
    }

    public void updateStatus(UUID orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(newStatus);
        orderRepository.save(order);

        if (newStatus == OrderStatus.OUT_FOR_DELIVERY) {
            OrderNotificationRequest request = OrderNotificationRequest.newBuilder()
                    .setUserEmail(order.getUserEmail())
                    .setOrderId(orderId.toString())
                    .setTotalAmount(order.getTotalAmount())
                    .build();

            // Fix: Actually call the notification service!
            try {
                notificationStub.sendOrderEmail(request);
            } catch (Exception e) {
                // Log error but don't crash the transaction
                System.err.println("Failed to send notification: " + e.getMessage());
            }
        }
    }

    public Order getOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public List<Order> getOrdersByUser(UUID userId) {
        return orderRepository.findByUserId(userId);
    }
}