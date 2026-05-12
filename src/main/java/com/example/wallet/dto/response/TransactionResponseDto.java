package com.example.wallet.dto.response;

import com.example.wallet.entity.TransactionStatus;
import com.example.wallet.entity.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TransactionResponseDto {

    private Long id;
    private TransactionType type;
    private BigDecimal amount;
    private TransactionStatus status;
    private LocalDateTime createdAt;
}
