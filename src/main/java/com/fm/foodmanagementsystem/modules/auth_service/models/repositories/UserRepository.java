package com.fm.foodmanagementsystem.modules.auth_service.models.repositories;

import com.fm.foodmanagementsystem.modules.auth_service.models.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    Optional<User> findByEmailIgnoreCase(String email);

    // Không dùng @EntityGraph ở đây: pagination + collection fetch gây HHH90003004
    // (Hibernate phân trang trong RAM). Roles được lazy-load trong @Transactional của service.
    @Override
    Page<User> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "roles")
    Optional<User> findById(String id);

    Page<User> findByEmailContainingIgnoreCaseOrPhoneContainingIgnoreCase(String email, String phone, Pageable pageable);

    // Không dùng @EntityGraph ở đây (lý do như findAll). Roles lazy-load trong transaction.
    @Query("SELECT u FROM User u WHERE u.isActive = true AND " +
            "(:role IS NULL OR EXISTS (SELECT r FROM u.roles r WHERE r.name = :role)) AND " +
            "(:search IS NULL OR (" +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.phone) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%'))))")
    Page<User> searchAndFilterUsers(
            @Param("search") String search,
            @Param("role") String role,
            Pageable pageable);
}
