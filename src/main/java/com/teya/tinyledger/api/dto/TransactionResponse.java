package com.teya.tinyledger.api.dto;

import com.teya.tinyledger.domain.LedgerTransaction;
import com.teya.tinyledger.domain.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        UUID accountId,
        TransactionType type,
        BigDecimal amount,
        Instant createdAt
) {
    public static TransactionResponse from(LedgerTransaction transaction) {
        return new TransactionResponse(
                transaction.id(),
                transaction.accountId(),
                transaction.type(),
                transaction.amount().amount(),
                transaction.createdAt()
        );
    }
}
