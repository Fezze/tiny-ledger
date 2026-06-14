package com.teya.tinyledger.domain;

import java.util.UUID;

public final class Account {

    private final UUID id;
    private Money balance;

    private Account(UUID id, Money balance) {
        if (id == null) {
            throw new RuntimeException("Account id is required");
        }
        if (balance == null) {
            throw new RuntimeException("Account balance is required");
        }
        this.id = id;
        this.balance = balance;
    }

    public static AccountOpening open(Money openingBalance) {
        Account account = new Account(UUID.randomUUID(), openingBalance);
        LedgerTransaction ledgerTransaction = LedgerTransaction.openingBalance(account.id, openingBalance);
        return new AccountOpening(account, ledgerTransaction);
    }

    public UUID id() {
        return id;
    }

    public Money balance() {
        return balance;
    }

    public LedgerTransaction deposit(Money amount) {
        requirePositiveMovement(amount);
        balance = balance.add(amount);
        return LedgerTransaction.deposit(this.id, amount);
    }

    public LedgerTransaction withdraw(Money amount) {
        requirePositiveMovement(amount);
        if (balance.isLessThan(amount)) {
            throw new RuntimeException("Insufficient funds");
        }
        balance = balance.subtract(amount);
        return LedgerTransaction.withdrawal(this.id, amount);
    }

    private void requirePositiveMovement(Money amount) {
        if (amount == null) {
            throw new RuntimeException("Amount is required");
        }
        if (!amount.isPositive()) {
            throw new RuntimeException("Amount must be greater than zero");
        }
    }

}
