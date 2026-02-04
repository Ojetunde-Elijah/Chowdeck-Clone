package com.pm.restaurantservice.controller;

import com.pm.restaurantservice.dto.RestaurantRequestDTO;
import com.pm.restaurantservice.model.Restaurant;
import com.pm.restaurantservice.service.RestaurantService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<Restaurant>> getNearby(
            @RequestParam double lat,
            @RequestParam double lng) {
        return ResponseEntity.ok(restaurantService.findNearby(lat, lng));
    }

    @GetMapping("/{id}/menu")
    public ResponseEntity<Restaurant> getMenu(@PathVariable UUID id) {
        return ResponseEntity.ok(restaurantService.getMenu(id));
    }

    @PostMapping
    public ResponseEntity<Restaurant> create(@RequestBody RestaurantRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(restaurantService.createRestaurant(dto));
    }
}