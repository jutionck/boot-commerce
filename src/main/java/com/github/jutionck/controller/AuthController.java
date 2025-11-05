package com.github.jutionck.controller;

import com.github.jutionck.dto.request.LoginRequest;
import com.github.jutionck.dto.request.RegisterRequest;
import com.github.jutionck.service.AuthService;
import com.github.jutionck.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auths")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> loginHandler(@Valid @RequestBody LoginRequest request) {
        return ResponseUtil.buildSingleResponse(
                HttpStatus.CREATED,
                HttpStatus.CREATED.getReasonPhrase(),
                authService.login(request)
        );
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerHandler(@Valid @RequestBody RegisterRequest request) {
        return ResponseUtil.buildSingleResponse(
                HttpStatus.CREATED,
                HttpStatus.CREATED.getReasonPhrase(),
                authService.register(request)
        );
    }
}
