package com.pm.restaurantservice.repository;

import com.pm.restaurantservice.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {

    // Used by the Service to filter nearby restaurants that are actually open
    List<Restaurant> findAllByIsActiveTrue();

    // Useful for searching by cuisine type (e.g., "Pizza", "Nigerian")
    List<Restaurant> findByCuisineTypeIgnoreCase(String cuisineType);
}
