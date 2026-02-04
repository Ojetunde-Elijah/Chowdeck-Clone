package com.pm.orderservice.Enum;

public enum OrderStatus {
    PENDING,        // Waiting for payment
    PAID,           // Payment confirmed
    PREPARING,      // Restaurant is cooking
    OUT_FOR_DELIVERY, // Rider has picked it up
    DELIVERED,      // User has the food
    CANCELLED       // Something went wrong
}