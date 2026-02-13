package com.restaurant.repository;

import com.restaurant.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    List<User> findByRole(User.Role role);
    long countByRole(User.Role role);
    long countByRoleAndCreatedAtAfter(User.Role role, LocalDateTime after);
}
