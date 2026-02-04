package com.pm.userservice.controller;

import com.pm.userservice.dto.AddressRequestDTO;
import com.pm.userservice.dto.AddressResponseDTO;
import com.pm.userservice.model.Address;
import com.pm.userservice.model.UserProfile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.pm.userservice.service.UserProfileService;

import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserProfileService userProfileService;

    public UserController(UserProfileService userProfileService){
        this.userProfileService = userProfileService;
    }
    @GetMapping("/me")
    public ResponseEntity<UserProfile> getProfile(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(userProfileService.getFullProfile(UUID.fromString(userId)));
    }

    @PostMapping("/location")
    public ResponseEntity<AddressResponseDTO> updateLocation(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody AddressRequestDTO addressDTO) {

        // Frontend sends: { "formattedAddress": "...", "latitude": 6.5244, "longitude": 3.3792 ... }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userProfileService.saveLocation(UUID.fromString(userId), addressDTO));
    }
}
