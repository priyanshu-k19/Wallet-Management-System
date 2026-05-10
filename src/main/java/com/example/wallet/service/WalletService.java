package com.example.wallet.service;

import com.example.wallet.dto.response.WalletResponseDto;
import java.math.BigDecimal;

public interface WalletService {

    WalletResponseDto getBalance(Long userId);

    WalletResponseDto credit(Long userId, BigDecimal amount);

    WalletResponseDto debit(Long userId, BigDecimal amount);

    WalletResponseDto transfer(Long fromUserId, Long toUserId, BigDecimal amount);
}
