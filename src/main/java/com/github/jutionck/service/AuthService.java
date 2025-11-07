package com.github.jutionck.service;

import com.github.jutionck.dto.request.LoginRequest;
import com.github.jutionck.dto.request.RegisterRequest;
import com.github.jutionck.dto.response.LoginResponse;
import com.github.jutionck.dto.response.RegisterResponse;
import com.github.jutionck.entity.User;
import com.github.jutionck.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword())
        );

        User user = (User) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(user);
        String TOKEN_TYPE = "Bearer";

        LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .build();

        return LoginResponse.builder()
                .accessToken(token)
                .tokenType(TOKEN_TYPE)
                .user(userInfo)
                .build();
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        log.info("Attempting to register new user with email: {}", request.getEmail());

        User user = userService.createUser(request);

        log.info("User registered successfully with email: {}", user.getEmail());
        return RegisterResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public User getCurrentUser(String email) {
        return (User) userService.loadUserByUsername(email);
    }

}
