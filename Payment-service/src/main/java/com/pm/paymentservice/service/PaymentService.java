package com.pm.paymentservice.service;

import com.pm.paymentservice.Enum.PaymentStatus;
import com.pm.paymentservice.model.Payment;
//import com.pm.paymentservice.model.PaymentStatus;
import com.pm.paymentservice.repository.PaymentRepository;
//import com.pm.grpc.order.OrderInternalServiceGrpc; // Generated from proto
//import com.pm.grpc.order.PaymentStatusRequest;    // Generated from proto
//import com.pm.grpc.order.PaymentStatusResponse;   // Generated from proto
import net.devh.boot.grpc.client.inject.GrpcClient;
import order_service.OrderInternalServiceGrpc;
import order_service.PaymentStatusRequest;
import order_service.PaymentStatusResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RestTemplate restTemplate;

    @Value("${paystack.secret.key}")
    private String secretKey;

    @GrpcClient("order-service")
    private order_service.OrderInternalServiceGrpc.OrderInternalServiceBlockingStub orderStub;

    public PaymentService(PaymentRepository paymentRepository, RestTemplate restTemplate) {
        this.paymentRepository = paymentRepository;
        this.restTemplate = restTemplate;
    }

    /**
     * Initializes payment with Paystack
     */
    public String initializePaystack(Payment payment) {
        String url = "https://api.paystack.co/transaction/initialize";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(secretKey);

        Map<String, Object> body = new HashMap<>();
        body.put("email", payment.getCustomerEmail());
        body.put("amount", (long) (payment.getAmount() * 100)); // Paystack uses Kobo
        body.put("reference", payment.getReference());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

        // Extract the payment URL to return to the frontend
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        return (String) data.get("authorization_url");
    }

    /**
     * Called by Webhook Controller to handle Paystack notifications
     */
    @Transactional
    public void processPaystackEvent(Map<String, Object> payload) {
        String event = (String) payload.get("event");

        if ("charge.success".equals(event)) {
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            String reference = (String) data.get("reference");

            Payment payment = paymentRepository.findByReference(reference)
                    .orElseThrow(() -> new RuntimeException("Payment reference not found"));

            payment.setStatus(PaymentStatus.SUCCESS);
            paymentRepository.save(payment);

            // Inside PaymentService.java
//            OrderNotificationRequest emailRequest = OrderNotificationRequest.newBuilder()
//                    .setUserEmail(payment.getCustomerEmail())
//                    .setOrderId(payment.getOrderId().toString())
//                    .setTotalAmount(payment.getAmount())
//                    .setType(NotificationType.PAYMENT_SUCCESS) // Explicitly set the category
//                    .build();
//
//            notificationStub.sendOrderEmail(emailRequest);        }
        }


    }

    public void notifyOrderService(UUID orderId, String status) {
        order_service.PaymentStatusRequest request = order_service.PaymentStatusRequest.newBuilder()
                .setOrderId(orderId.toString())
                .setStatus(status)
                .build();

        try {
            order_service.PaymentStatusResponse response = orderStub.updateOrderStatus(request);
            if (!response.getSuccess()) {
                throw new RuntimeException("Failed to update order service: " + response.getMessage());
            }
        } catch (Exception e) {
            System.err.println("gRPC call failed: " + e.getMessage());
        }
    }
}