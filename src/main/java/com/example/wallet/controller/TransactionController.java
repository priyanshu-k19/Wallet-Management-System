package com.example.wallet.controller;

import com.example.wallet.dto.response.ApiResponse;
import com.example.wallet.dto.response.PaginatedResponseDto;
import com.example.wallet.dto.response.TransactionResponseDto;
import com.example.wallet.service.TransactionService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponseDto<TransactionResponseDto>>> getTransactions(
        Authentication auth,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        PaginatedResponseDto<TransactionResponseDto> transactions = transactionService.getTransactions(
            auth.getName(),
            PageRequest.of(page, size)
        );

        return ResponseEntity.ok(
            ApiResponse.<PaginatedResponseDto<TransactionResponseDto>>builder()
                .success(true)
                .message("Transactions fetched successfully")
                .data(transactions)
                .timestamp(LocalDateTime.now())
                .build()
        );
    }
}
