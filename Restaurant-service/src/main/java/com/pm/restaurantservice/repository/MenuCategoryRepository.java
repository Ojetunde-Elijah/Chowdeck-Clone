package com.pm.restaurantservice.repository;

import com.pm.restaurantservice.model.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MenuCategoryRepository extends JpaRepository<MenuCategory, Long> {

    // Finds all categories belonging to a specific restaurant
    List<MenuCategory> findByRestaurantId(UUID restaurantId);
}