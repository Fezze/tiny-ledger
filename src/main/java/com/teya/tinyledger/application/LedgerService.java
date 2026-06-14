package com.teya.tinyledger.application;

import com.teya.tinyledger.domain.Account;
import com.teya.tinyledger.domain.AccountOpening;
import com.teya.tinyledger.domain.LedgerTransaction;
import com.teya.tinyledger.domain.Money;
import com.teya.tinyledger.infrastructure.InMemoryLedgerStore;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class LedgerService {

    private final InMemoryLedgerStore store;

    public LedgerService(InMemoryLedgerStore store) {
        this.store = store;
    }

    public LedgerTransaction openAccount(BigDecimal initialBalance) {
        AccountOpening opening =  Account.open(Money.of(initialBalance));
        store.save(opening);
        return opening.openingTransaction();
    }

    public LedgerTransaction deposit(UUID accountId, BigDecimal amount) {
        Account account = store.getAccount(accountId);
        LedgerTransaction transaction = account.deposit(Money.of(amount));
        store.append(transaction);
        return transaction;
    }

    public LedgerTransaction withdraw(UUID accountId, BigDecimal amount) {
        Account account = store.getAccount(accountId);
        LedgerTransaction transaction = account.withdraw(Money.of(amount));
        store.append(transaction);
        return transaction;
    }

    public Money balance(UUID accountId) {
        return store.balance(accountId);
    }

    public List<LedgerTransaction> history(UUID accountId) {
        return store.transactions(accountId);
    }
}
