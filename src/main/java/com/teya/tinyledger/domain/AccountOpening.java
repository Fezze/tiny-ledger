package com.teya.tinyledger.domain;

public record AccountOpening(Account account, LedgerTransaction openingTransaction) {
}
