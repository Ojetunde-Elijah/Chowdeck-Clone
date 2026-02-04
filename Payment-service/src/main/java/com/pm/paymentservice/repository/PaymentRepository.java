package com.pm.paymentservice.repository;

import com.pm.paymentservice.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    // Find payment by reference to update status when Paystack sends the webhook
    Optional<Payment> findByReference(String reference);
}