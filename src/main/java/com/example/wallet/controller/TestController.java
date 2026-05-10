package com.example.wallet.controller;

import com.example.wallet.dto.response.ApiResponse;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/")
    public ResponseEntity<ApiResponse<Map<String, Object>>> home() {
        Map<String, Object> data = Map.of(
            "endpoints", new String[] {
                "/test",
                "/api/auth/register",
                "/api/auth/login",
                "/api/wallet/balance",
                "/api/wallet/credit",
                "/api/wallet/debit",
                "/api/wallet/transfer",
                "/api/transactions"
            }
        );

        return ResponseEntity.ok(
            ApiResponse.<Map<String, Object>>builder()
                .success(true)
                .message("Wallet API is running")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build()
        );
    }

    @GetMapping("/test")
    public ResponseEntity<ApiResponse<String>> test() {
        return ResponseEntity.ok(
            ApiResponse.<String>builder()
                .success(true)
                .message("Test endpoint is working")
                .data("Working")
                .timestamp(LocalDateTime.now())
                .build()
        );
    }
}
