package com.pm.orderservice.controller;

import com.pm.orderservice.dto.OrderRequestDto;
import com.pm.orderservice.model.Order;
import com.pm.orderservice.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/place")
    public ResponseEntity<Order> placeOrder(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody OrderRequestDto request) {

        // userId comes from the Gateway's JWT extraction
        Order placedOrder = orderService.placeOrder(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(placedOrder);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderDetails(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    @GetMapping("/history")
    public ResponseEntity<List<Order>> getMyOrderHistory(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(orderService.getOrdersByUser(UUID.fromString(userId)));
    }
}