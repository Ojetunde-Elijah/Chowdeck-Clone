package com.pm.paymentservice.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.paymentservice.model.Payment;
import com.pm.paymentservice.service.PaymentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Value("${paystack.secret.key}")
    private String paystackSecretKey;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * ENDPOINT 1: Initialize Payment
     * POST http://localhost:7011/api/v1/payments/initialize
     */
    @PostMapping("/initialize")
    public ResponseEntity<Map<String, String>> initialize(@RequestBody Map<String, Object> request) {
        Payment payment = new Payment();
        payment.setOrderId(UUID.fromString(request.get("orderId").toString()));
        payment.setCustomerEmail(request.get("customerEmail").toString());
        payment.setAmount(Double.parseDouble(request.get("amount").toString()));
        payment.setReference("PAY-" + UUID.randomUUID().toString().substring(0, 8));

        // Get the Paystack redirect URL
        String authUrl = paymentService.initializePaystack(payment);

        return ResponseEntity.ok(Map.of("authorization_url", authUrl));
    }

    /**
     * ENDPOINT 2: Paystack Webhook
     * POST https://7c26617a4df0.ngrok-free.app/api/v1/payments/webhook
     */
    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("x-paystack-signature") String signature) {

        // 1. Security Check
        if (!isValidSignature(payload, signature)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 2. Parse and Process
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> eventMap = mapper.readValue(payload, Map.class);
            paymentService.processPaystackEvent(eventMap);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private boolean isValidSignature(String payload, String signature) {
        try {
            String HMAC_SHA512 = "HmacSHA512";
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    paystackSecretKey.getBytes(StandardCharsets.UTF_8), HMAC_SHA512);
            Mac mac = Mac.getInstance(HMAC_SHA512);
            mac.init(secretKeySpec);
            byte[] bytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            StringBuilder hash = new StringBuilder();
            for (byte b : bytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hash.append('0');
                hash.append(hex);
            }
            return hash.toString().equals(signature);
        } catch (Exception e) {
            return false;
        }
    }
}