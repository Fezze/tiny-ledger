package com.teya.tinyledger.domain;

import java.util.UUID;

public final class Account {

    private final UUID id;
    private Money balance;

    private Account(UUID id, Money balance){
        if(id == null) {
            throw new RuntimeException("Account id is required");
        }
        if(balance == null) {
            throw new RuntimeException("Account balance is required");
        }
        this.id = id;
        this.balance = balance;
    }

    public static Account open(Money openingBalance) {
        return new Account(UUID.randomUUID(), openingBalance);
    }

    public UUID id() {
        return id;
    }

    public Money balance() {
        return balance;
    }

    public AccountSnapshot snapshot() {
        return new AccountSnapshot(id, balance);
    }

}
