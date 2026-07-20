package com.github.kazikw.boisgo.repository;

import com.github.kazikw.boisgo.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByFirstName(String firstName);
    Optional<User> findByEmail(String email);
    boolean existsByFirstName(String firstName);
    boolean existsByEmail(String email);
}