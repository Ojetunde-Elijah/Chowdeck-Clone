package com.pm.restaurantservice.dto;

import java.util.List;

public class RestaurantRequestDTO {
    private String name;
    private double latitude;
    private double longitude;
    private double deliveryRadiusKm;
    private List<MenuCategoryDTO> categories;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getDeliveryRadiusKm() {
        return deliveryRadiusKm;
    }

    public void setDeliveryRadiusKm(double deliveryRadiusKm) {
        this.deliveryRadiusKm = deliveryRadiusKm;
    }

    public List<MenuCategoryDTO> getCategories() {
        return categories;
    }

    public void setCategories(List<MenuCategoryDTO> categories) {
        this.categories = categories;
    }
}