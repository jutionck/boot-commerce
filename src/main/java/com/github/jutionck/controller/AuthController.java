package com.github.jutionck.controller;

import com.github.jutionck.dto.request.LoginRequest;
import com.github.jutionck.dto.request.RegisterRequest;
import com.github.jutionck.dto.response.LoginResponse;
import com.github.jutionck.entity.User;
import com.github.jutionck.service.AuthService;
import com.github.jutionck.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> registerHandler(@Valid @RequestBody RegisterRequest request) {
        return ResponseUtil.buildSingleResponse(
                HttpStatus.CREATED,
                "User registered successfully",
                authService.register(request)
        );
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginHandler(@Valid @RequestBody LoginRequest request) {
        return ResponseUtil.buildSingleResponse(
                HttpStatus.OK,
                "Login successful",
                authService.login(request)
        );
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal User user) {
        LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .build();

        return ResponseUtil.buildSingleResponse(
                HttpStatus.OK,
                "User retrieved successfully",
                userInfo
        );
    }
}
