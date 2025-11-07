package com.github.jutionck.service;

import com.github.jutionck.dto.request.RegisterRequest;
import com.github.jutionck.entity.User;
import com.github.jutionck.enums.UserRole;
import com.github.jutionck.exceptions.ResourceDuplicateException;
import com.github.jutionck.exceptions.ValidationException;
import com.github.jutionck.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email).orElseThrow(() ->
                new UsernameNotFoundException("Email or password is incorrect"));
    }

    public User findById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Transactional
    public User createUser(RegisterRequest request) {
        log.info("Creating new user with email: {}", request.getEmail());

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new ValidationException("Password and confirm password do not match");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceDuplicateException("Email already exists");
        }

        // Parse role from request, default to CUSTOMER
        UserRole userRole = UserRole.CUSTOMER;
        if (request.getRole() != null && !request.getRole().isEmpty()) {
            try {
                userRole = UserRole.valueOf(request.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ValidationException("Invalid role: " + request.getRole());
            }
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(userRole)
                .enabled(true)
                .build();

        return userRepository.save(user);
    }
}
