package com.example.wallet.service;

import com.example.wallet.dto.response.PaginatedResponseDto;
import com.example.wallet.dto.response.TransactionResponseDto;
import com.example.wallet.entity.Transaction;
import com.example.wallet.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public PaginatedResponseDto<TransactionResponseDto> getTransactions(String email, Pageable pageable) {
        Page<Transaction> transactions = transactionRepository.findByUserEmailOrderByCreatedAtDesc(email, pageable);

        return PaginatedResponseDto.<TransactionResponseDto>builder()
            .content(transactions.getContent().stream()
                .map(this::mapToResponse)
                .toList())
            .pageNumber(transactions.getNumber())
            .totalPages(transactions.getTotalPages())
            .build();
    }

    private TransactionResponseDto mapToResponse(Transaction transaction) {
        return TransactionResponseDto.builder()
            .id(transaction.getId())
            .type(transaction.getType())
            .amount(transaction.getAmount())
            .status(transaction.getStatus())
            .createdAt(transaction.getCreatedAt())
            .build();
    }
}
