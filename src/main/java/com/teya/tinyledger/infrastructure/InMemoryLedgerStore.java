package com.teya.tinyledger.infrastructure;

import com.teya.tinyledger.domain.Account;
import com.teya.tinyledger.domain.AccountOpening;
import com.teya.tinyledger.domain.LedgerTransaction;
import com.teya.tinyledger.domain.Money;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class InMemoryLedgerStore {

    private final Map<UUID, Account> accountsById = new HashMap<>();
    private final Map<UUID, List<LedgerTransaction>> transactionsByAccountId = new HashMap<>();

    public void save(AccountOpening opening) {
        accountsById.put(opening.account().id(), opening.account());
        transactionsByAccountId.put(
                opening.account().id(),
                new ArrayList<>(List.of(opening.openingTransaction()))
        );
    }

    public void append(LedgerTransaction transaction) {
        getLedgerTransactions(transaction.accountId()).add(transaction);
    }

    public Money balance(UUID accountId) {
        return getAccount(accountId).balance();
    }

    public List<LedgerTransaction> transactions(UUID accountId) {
        return getLedgerTransactions(accountId).stream().toList();
    }

    private List<LedgerTransaction> getLedgerTransactions(UUID accountId) {
        List<LedgerTransaction> ledgerTransactions = transactionsByAccountId.get(accountId);
        if (ledgerTransactions == null) {
            throw new RuntimeException("Account not found: " + accountId);
        }
        return ledgerTransactions;
    }

    public Account getAccount(UUID accountId) {
        return Optional.ofNullable(accountsById.get(accountId))
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountId));
    }

}
