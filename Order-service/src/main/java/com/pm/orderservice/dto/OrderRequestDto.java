package com.pm.orderservice.dto;

import java.util.List;
import java.util.UUID;

public class OrderRequestDto {
    private UUID restaurantId;
    private List<OrderItemRequestDto> items;

    public UUID getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(UUID restaurantId) {
        this.restaurantId = restaurantId;
    }

    public List<OrderItemRequestDto> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequestDto> items) {
        this.items = items;
    }
}
