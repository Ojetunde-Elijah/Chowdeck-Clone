package com.pm.userservice.repository;

import com.pm.userservice.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.userProfile.id = :userId")
    void resetDefaultAddresses(UUID userId);
}
