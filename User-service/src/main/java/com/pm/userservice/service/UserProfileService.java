package com.pm.userservice.service;

import com.pm.userservice.dto.AddressRequestDTO;
import com.pm.userservice.dto.AddressResponseDTO;
import com.pm.userservice.model.Address;
import com.pm.userservice.model.UserProfile;
import com.pm.userservice.repository.UserRepository;
import com.pm.userservice.repository.AddressRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserProfileService {
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    public UserProfileService(UserRepository userRepository, AddressRepository addressRepository){
       this.userRepository = userRepository;
       this.addressRepository = addressRepository;
    }

    @Transactional
    public AddressResponseDTO saveLocation(UUID userId, AddressRequestDTO dto) {
        UserProfile profile = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found. Sign up"));

        // Logic to ensure only one default address exists
        if (dto.isDefault()) {
            addressRepository.resetDefaultAddresses(userId);
        }

        Address address = new Address();
        address.setLabel(dto.getLabel());
        address.setFormattedAddress(dto.getFormattedAddress());
        address.setLatitude(dto.getLatitude());
        address.setLongitude(dto.getLongitude());
        address.setApartmentNumber(dto.getApartmentNumber());
        address.setDefault(dto.isDefault());
        address.setUserProfile(profile);

        Address saved = addressRepository.save(address);

        AddressResponseDTO responseDTO = new AddressResponseDTO();

        responseDTO.setId(address.getId());
        responseDTO.setDefault(address.isDefault());
        responseDTO.setLatitude(address.getLatitude());
        responseDTO.setLongitude(address.getLongitude());
        responseDTO.setLabel(address.getLabel());
        responseDTO.setApartmentNumber(address.getApartmentNumber());
        responseDTO.setFormattedAddress(address.getFormattedAddress());

        return responseDTO;
    }

    public UserProfile getFullProfile(UUID userId){
        return userRepository.findById(userId).orElseThrow();
    }
}
