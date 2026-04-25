package com.fm.foodmanagementsystem.modules.auth_service.models.repositories;

import com.fm.foodmanagementsystem.modules.auth_service.models.entities.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {
    List<UserAddress> findAllByUserId(String userId);
}
