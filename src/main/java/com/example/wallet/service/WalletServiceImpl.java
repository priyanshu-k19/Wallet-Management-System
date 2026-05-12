package com.example.wallet.service;

import com.example.wallet.dto.response.WalletResponseDto;
import com.example.wallet.entity.Transaction;
import com.example.wallet.entity.TransactionStatus;
import com.example.wallet.entity.TransactionType;
import com.example.wallet.entity.Wallet;
import com.example.wallet.exception.InsufficientBalanceException;
import com.example.wallet.exception.WalletNotFoundException;
import com.example.wallet.repository.TransactionRepository;
import com.example.wallet.repository.WalletRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    @Override
    @Cacheable(value = "walletBalances", key = "#userId")
    public WalletResponseDto getBalance(Long userId) {
        Wallet wallet = getWallet(userId);
        return mapToResponse(wallet);
    }

    @Override
    @Transactional
    @CachePut(value = "walletBalances", key = "#userId")
    public WalletResponseDto credit(Long userId, BigDecimal amount) {
        validateAmount(amount);

        Wallet wallet = getWalletForUpdate(userId);
        wallet.setBalance(wallet.getBalance().add(amount));
        saveTransaction(wallet, TransactionType.CREDIT, amount, TransactionStatus.SUCCESS);

        return mapToResponse(wallet);
    }

    @Override
    @Transactional
    @CachePut(value = "walletBalances", key = "#userId")
    public WalletResponseDto debit(Long userId, BigDecimal amount) {
        validateAmount(amount);

        Wallet wallet = getWalletForUpdate(userId);
        validateSufficientBalance(wallet, amount);
        wallet.setBalance(wallet.getBalance().subtract(amount));
        saveTransaction(wallet, TransactionType.DEBIT, amount, TransactionStatus.SUCCESS);

        return mapToResponse(wallet);
    }

    @Override
    @Transactional
    @Caching(
        put = @CachePut(value = "walletBalances", key = "#fromUserId"),
        evict = @CacheEvict(value = "walletBalances", key = "#toUserId")
    )
    public WalletResponseDto transfer(Long fromUserId, Long toUserId, BigDecimal amount) {
        validateAmount(amount);

        if (fromUserId.equals(toUserId)) {
            throw new RuntimeException("Sender and receiver cannot be the same");
        }

        Wallet senderWallet;
        Wallet receiverWallet;

        if (fromUserId < toUserId) {
            senderWallet = getWalletForUpdate(fromUserId);
            receiverWallet = getWalletForUpdate(toUserId);
        } else {
            receiverWallet = getWalletForUpdate(toUserId);
            senderWallet = getWalletForUpdate(fromUserId);
        }

        validateSufficientBalance(senderWallet, amount);
        senderWallet.setBalance(senderWallet.getBalance().subtract(amount));
        receiverWallet.setBalance(receiverWallet.getBalance().add(amount));
        saveTransaction(senderWallet, TransactionType.TRANSFER, amount, TransactionStatus.SUCCESS);
        saveTransaction(receiverWallet, TransactionType.TRANSFER, amount, TransactionStatus.SUCCESS);

        return mapToResponse(senderWallet);
    }

    private Wallet getWallet(Long userId) {
        return walletRepository.findByUserId(userId)
            .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user id: " + userId));
    }

    private Wallet getWalletForUpdate(Long userId) {
        return walletRepository.findByUserIdForUpdate(userId)
            .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user id: " + userId));
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount must be greater than zero");
        }
    }

    private void validateSufficientBalance(Wallet wallet, BigDecimal amount) {
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }
    }

    private WalletResponseDto mapToResponse(Wallet wallet) {
        return WalletResponseDto.builder()
            .balance(wallet.getBalance())
            .build();
    }

    private void saveTransaction(
        Wallet wallet,
        TransactionType type,
        BigDecimal amount,
        TransactionStatus status
    ) {
        Transaction transaction = new Transaction();
        transaction.setUser(wallet.getUser());
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setStatus(status);
        transactionRepository.save(transaction);
    }
}
