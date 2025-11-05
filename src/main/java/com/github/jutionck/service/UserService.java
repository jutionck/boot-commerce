package com.github.jutionck.service;

import com.github.jutionck.dto.request.RegisterRequest;
import com.github.jutionck.entity.User;
import com.github.jutionck.enums.UserRole;
import com.github.jutionck.exceptions.ResourceDuplicateException;
import com.github.jutionck.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username).orElseThrow(() ->
                new UsernameNotFoundException("Username or password is incorrect"));
    }
    
    @Transactional
    public User createUser(RegisterRequest request) {
        log.info("Creating new user with email: {}", request.getEmail());
        
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Password and confirm password do not match");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceDuplicateException("Email already exists");
        }
        
        String username = request.getEmail().split("@")[0];
        int counter = 1;
        String originalUsername = username;
        
        while (userRepository.existsByUsername(username)) {
            username = originalUsername + counter++;
        }
        
        User user = User.builder()
                .email(username)
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .role(UserRole.USER)
                .enabled(true)
                .build();
        
        return userRepository.save(user);
    }
}
