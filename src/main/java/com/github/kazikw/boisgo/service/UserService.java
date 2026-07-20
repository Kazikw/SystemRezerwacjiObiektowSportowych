package com.github.kazikw.boisgo.service;

import com.github.kazikw.boisgo.domain.Role;
import com.github.kazikw.boisgo.domain.User;
import com.github.kazikw.boisgo.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User register(String firstName, String email, String rawPassword) {
        if (userRepository.existsByFirstName(firstName)) {
            throw new IllegalStateException("Użytkownik o takim loginie już istnieje.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException("Użytkownik o takim adresie email już istnieje.");
        }

        User user = User.builder()
                .firstName(firstName)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role(Role.USER)
                .build();

        return userRepository.save(user);
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new org.springframework.security.access.AccessDeniedException("Brak zalogowanego użytkownika.");
        }
        return userRepository.findByFirstName(authentication.getName())
                .orElseThrow(() -> new EntityNotFoundException("Zalogowany użytkownik nie istnieje w bazie."));
    }

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono użytkownika o ID: " + id));
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono użytkownika o email: " + email));
    }

    @Transactional
    public void promoteToAdmin(User user) {
        user.setRole(Role.ADMIN);
        userRepository.save(user);
    }
}