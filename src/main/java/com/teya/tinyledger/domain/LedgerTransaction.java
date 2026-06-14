package com.teya.tinyledger.domain;

import java.time.Instant;
import java.util.UUID;

public final class LedgerTransaction {

    private final UUID id;
    private final UUID accountId;
    private final TransactionType type;
    private final Money amount;
    private final Instant createdAt;

    private LedgerTransaction(UUID id, UUID accountId, TransactionType type, Money amount, Instant createdAt) {
        if (id == null) {
            throw new RuntimeException("Transaction id is required");
        }
        if (accountId == null) {
            throw new RuntimeException("Account id is required");
        }
        if (type == null) {
            throw new RuntimeException("Transaction type is required");
        }
        if (amount == null) {
            throw new RuntimeException("Transaction amount is required");
        }
        if (createdAt == null) {
            throw new RuntimeException("Transaction creation time is required");
        }
        this.id = id;
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    static LedgerTransaction openingBalance(UUID accountId, Money amount) {
        return new LedgerTransaction(UUID.randomUUID(), accountId, TransactionType.OPENING_BALANCE, amount, Instant.now());
    }

    static LedgerTransaction deposit(UUID accountId, Money amount) {
        return new LedgerTransaction(UUID.randomUUID(), accountId, TransactionType.DEPOSIT, amount, Instant.now());
    }

    static LedgerTransaction withdrawal(UUID accountId, Money amount) {
        return new LedgerTransaction(UUID.randomUUID(), accountId, TransactionType.WITHDRAWAL, amount, Instant.now());
    }

    public UUID id() {
        return id;
    }

    public UUID accountId() {
        return accountId;
    }

    public TransactionType type() {
        return type;
    }

    public Money amount() {
        return amount;
    }

    public Instant createdAt() {
        return createdAt;
    }
}
