package com.example.wallet.controller;

import com.example.wallet.dto.request.LoginRequest;
import com.example.wallet.dto.request.RegisterRequest;
import com.example.wallet.dto.response.ApiResponse;
import com.example.wallet.dto.response.LoginResponse;
import com.example.wallet.service.AuthService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        authService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.<Void>builder()
                .success(true)
                .message("User registered successfully")
                .data(null)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(
            ApiResponse.<LoginResponse>builder()
                .success(true)
                .message("Login successful")
                .data(authService.loginUser(request))
                .timestamp(LocalDateTime.now())
                .build()
        );
    }
}
