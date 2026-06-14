package com.teya.tinyledger.infrastructure;

import com.teya.tinyledger.domain.Account;
import com.teya.tinyledger.domain.LedgerTransaction;
import com.teya.tinyledger.domain.Money;
import com.teya.tinyledger.domain.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryLedgerStoreTest {

    private final InMemoryLedgerStore store = new InMemoryLedgerStore();

    @Test
    void storesAccountAndOpeningTransactionTogether() {
        var opening = Account.open(Money.of(new BigDecimal("100.00")));

        store.save(opening);

        assertThat(store.balance(opening.account().id()).amount()).isEqualByComparingTo("100.00");
        assertThat(store.transactions(opening.account().id()))
                .hasSize(1)
                .first()
                .satisfies(transaction -> {
                    assertThat(transaction.type()).isEqualTo(TransactionType.OPENING_BALANCE);
                    assertThat(transaction.amount().amount()).isEqualByComparingTo("100.00");
                });
    }

    @Test
    void recordsTransactionsUnderTheCorrectAccountIndex() {
        var first = Account.open(Money.of(new BigDecimal("100.00")));
        var second = Account.open(Money.of(new BigDecimal("50.00")));
        store.save(first);
        store.save(second);

        store.append(first.account().deposit(Money.of(new BigDecimal("25.00"))));
        store.append(second.account().withdraw(Money.of(new BigDecimal("10.00"))));

        assertThat(store.transactions(first.account().id()))
                .extracting(LedgerTransaction::type)
                .containsExactly(TransactionType.OPENING_BALANCE, TransactionType.DEPOSIT);
        assertThat(store.transactions(second.account().id()))
                .extracting(LedgerTransaction::type)
                .containsExactly(TransactionType.OPENING_BALANCE, TransactionType.WITHDRAWAL);
    }

    @Test
    void returnedHistoryDoesNotExposeTheStoreCollection() {
        var opening = Account.open(Money.of(new BigDecimal("100.00")));
        store.save(opening);

        var history = store.transactions(opening.account().id());

        assertThatThrownBy(history::clear)
                .isInstanceOf(UnsupportedOperationException.class);
        assertThat(store.transactions(opening.account().id())).hasSize(1);
    }

    @Test
    void rejectsUnknownAccount() {
        UUID unknownAccountId = UUID.randomUUID();

        assertThatThrownBy(() -> store.balance(unknownAccountId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Account not found: " + unknownAccountId);
    }
}