package com.pm.orderservice.repository;

import com.pm.orderservice.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    /**
     * Retrieves all orders placed by a specific user.
     * This is required for the 'history' endpoint in your controller.
     */
    List<Order> findByUserId(UUID userId);

    /**
     * Retrieves orders by a specific restaurant.
     * Helpful for a future 'Vendor Dashboard' feature.
     */
    List<Order> findByRestaurantId(UUID restaurantId);
}