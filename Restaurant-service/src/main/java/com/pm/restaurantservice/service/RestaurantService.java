package com.pm.restaurantservice.service;

import com.pm.restaurantservice.dto.RestaurantRequestDTO;
import com.pm.restaurantservice.model.MenuCategory;
import com.pm.restaurantservice.model.MenuItem;
import com.pm.restaurantservice.model.Restaurant;
import com.pm.restaurantservice.repository.RestaurantRepository;
import com.pm.restaurantservice.utils.DistanceUtils;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;

    public RestaurantService(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    public List<Restaurant> findNearby(double userLat, double userLng) {
        return restaurantRepository.findAllByIsActiveTrue().stream()
                .filter(r -> DistanceUtils.calculateDistance(userLat, userLng, r.getLatitude(), r.getLongitude())
                        <= r.getDeliveryRadiusKm())
                .collect(Collectors.toList());
    }

    public Restaurant getMenu(UUID restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));
    }

    @Transactional
    public Restaurant createRestaurant(RestaurantRequestDTO dto) {
        Restaurant restaurant = new Restaurant();
        restaurant.setName(dto.getName());
        restaurant.setLatitude(dto.getLatitude());
        restaurant.setLongitude(dto.getLongitude());
        restaurant.setDeliveryRadiusKm(dto.getDeliveryRadiusKm());
        restaurant.setActive(true);

        // Map Categories and Items
        List<MenuCategory> categories = dto.getCategories().stream().map(catDto -> {
            MenuCategory category = new MenuCategory();
            category.setName(catDto.getName());
            category.setRestaurant(restaurant);

            List<MenuItem> items = catDto.getItems().stream().map(itemDto -> {
                MenuItem item = new MenuItem();
                item.setName(itemDto.getName());
                item.setDescription(itemDto.getDescription());
                item.setPrice(itemDto.getPrice());
                item.setCategory(category);
                return item;
            }).collect(Collectors.toList());

            category.setItems(items);
            return category;
        }).collect(Collectors.toList());

        restaurant.setCategories(categories);
        return restaurantRepository.save(restaurant);
    }
}