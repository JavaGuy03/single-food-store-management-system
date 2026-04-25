package com.fm.foodmanagementsystem.modules.auth_service.models.repositories;

import com.fm.foodmanagementsystem.modules.auth_service.models.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {
}
