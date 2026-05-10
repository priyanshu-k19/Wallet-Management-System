package com.example.wallet.controller;

import com.example.wallet.dto.request.TransactionRequestDto;
import com.example.wallet.dto.request.TransferRequestDto;
import com.example.wallet.dto.response.ApiResponse;
import com.example.wallet.dto.response.WalletResponseDto;
import com.example.wallet.service.IdempotencyService;
import com.example.wallet.service.WalletService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final IdempotencyService idempotencyService;

    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<WalletResponseDto>> getBalance(Authentication auth) {
        Long userId = Long.parseLong(auth.getName());
        return ResponseEntity.ok(
            ApiResponse.<WalletResponseDto>builder()
                .success(true)
                .message("Wallet balance fetched successfully")
                .data(walletService.getBalance(userId))
                .timestamp(LocalDateTime.now())
                .build()
        );
    }

    @PostMapping("/credit")
    public ResponseEntity<ApiResponse<WalletResponseDto>> credit(
        Authentication auth,
        @RequestHeader("Idempotency-Key") String idempotencyKey,
        @RequestBody TransactionRequestDto request
    ) {
        Long userId = Long.parseLong(auth.getName());
        ApiResponse<WalletResponseDto> response = idempotencyService.executeWalletOperation(
            idempotencyKey,
            userId,
            () -> ApiResponse.<WalletResponseDto>builder()
                .success(true)
                .message("Wallet credited successfully")
                .data(walletService.credit(userId, request.getAmount()))
                .timestamp(LocalDateTime.now())
                .build()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/debit")
    public ResponseEntity<ApiResponse<WalletResponseDto>> debit(
        Authentication auth,
        @RequestHeader("Idempotency-Key") String idempotencyKey,
        @RequestBody TransactionRequestDto request
    ) {
        Long userId = Long.parseLong(auth.getName());
        ApiResponse<WalletResponseDto> response = idempotencyService.executeWalletOperation(
            idempotencyKey,
            userId,
            () -> ApiResponse.<WalletResponseDto>builder()
                .success(true)
                .message("Wallet debited successfully")
                .data(walletService.debit(userId, request.getAmount()))
                .timestamp(LocalDateTime.now())
                .build()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<WalletResponseDto>> transfer(
        Authentication auth,
        @RequestHeader("Idempotency-Key") String idempotencyKey,
        @RequestBody TransferRequestDto request
    ) {
        Long fromUserId = Long.parseLong(auth.getName());
        ApiResponse<WalletResponseDto> response = idempotencyService.executeWalletOperation(
            idempotencyKey,
            fromUserId,
            () -> ApiResponse.<WalletResponseDto>builder()
                .success(true)
                .message("Transfer completed successfully")
                .data(walletService.transfer(fromUserId, request.getToUserId(), request.getAmount()))
                .timestamp(LocalDateTime.now())
                .build()
        );
        return ResponseEntity.ok(response);
    }
}
