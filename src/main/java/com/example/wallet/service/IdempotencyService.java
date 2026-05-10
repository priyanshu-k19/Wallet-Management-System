package com.example.wallet.service;

import com.example.wallet.dto.response.ApiResponse;
import com.example.wallet.dto.response.WalletResponseDto;
import com.example.wallet.entity.IdempotencyKey;
import com.example.wallet.repository.IdempotencyKeyRepository;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public ApiResponse<WalletResponseDto> executeWalletOperation(
        String key,
        Long userId,
        Supplier<ApiResponse<WalletResponseDto>> operation
    ) {
        validateKey(key);

        return idempotencyKeyRepository.findByKey(key)
            .map(existingKey -> replay(existingKey, userId))
            .orElseGet(() -> processAndStore(key, userId, operation));
    }

    private void validateKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Idempotency-Key header is required");
        }
    }

    private ApiResponse<WalletResponseDto> replay(IdempotencyKey existingKey, Long userId) {
        if (!existingKey.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Idempotency key belongs to a different user");
        }
        if (existingKey.getResponse() == null) {
            throw new IllegalStateException("Idempotency request is still processing");
        }

        try {
            return objectMapper.readValue(
                existingKey.getResponse(),
                new TypeReference<ApiResponse<WalletResponseDto>>() {
                }
            );
        } catch (JacksonException ex) {
            throw new IllegalStateException("Unable to replay idempotent response", ex);
        }
    }

    private ApiResponse<WalletResponseDto> processAndStore(
        String key,
        Long userId,
        Supplier<ApiResponse<WalletResponseDto>> operation
    ) {
        IdempotencyKey idempotencyKey = claimKey(key, userId);
        ApiResponse<WalletResponseDto> response = operation.get();

        idempotencyKey.setResponse(writeResponse(response));
        idempotencyKeyRepository.save(idempotencyKey);

        return response;
    }

    private IdempotencyKey claimKey(String key, Long userId) {
        IdempotencyKey idempotencyKey = new IdempotencyKey();
        idempotencyKey.setKey(key);
        idempotencyKey.setUserId(userId);
        return idempotencyKeyRepository.saveAndFlush(idempotencyKey);
    }

    private String writeResponse(ApiResponse<WalletResponseDto> response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JacksonException ex) {
            throw new IllegalStateException("Unable to store idempotent response", ex);
        }
    }
}
