package com.pm.restaurantservice.repository;

import com.pm.restaurantservice.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    // Pulls items for a specific category that are currently in stock
    List<MenuItem> findByCategoryIdAndIsAvailableTrue(Long categoryId);
}