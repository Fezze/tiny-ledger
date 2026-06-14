package com.teya.tinyledger.application;

import com.teya.tinyledger.domain.LedgerTransaction;
import com.teya.tinyledger.domain.TransactionType;
import com.teya.tinyledger.infrastructure.InMemoryLedgerStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LedgerServiceTest {

    private LedgerService ledgerService;

    @BeforeEach
    void setUp() {
        ledgerService = new LedgerService(new InMemoryLedgerStore());
    }

    @Test
    void opensAccountWithOpeningTransaction() {
        var openingTransaction = ledgerService.openAccount(new BigDecimal("100.00"));

        assertThat(openingTransaction.id()).isNotNull();
        assertThat(openingTransaction.accountId()).isNotNull();
        assertThat(openingTransaction.type()).isEqualTo(TransactionType.OPENING_BALANCE);
        assertThat(openingTransaction.amount().amount()).isEqualByComparingTo("100.00");
        List<LedgerTransaction> history = ledgerService.history(openingTransaction.accountId());
        assertThat(history).hasSize(1);

        LedgerTransaction savedTransaction = history.get(0);
        assertThat(savedTransaction).isSameAs(openingTransaction);
        assertThat(savedTransaction.accountId()).isEqualTo(openingTransaction.accountId());
        assertThat(savedTransaction.type()).isEqualTo(TransactionType.OPENING_BALANCE);
        assertThat(savedTransaction.amount().amount()).isEqualByComparingTo("100.00");
    }

    @Test
    void opensAccountWithZeroInitialBalance() {
        var openingTransaction = ledgerService.openAccount(new BigDecimal("0.00"));

        assertThat(openingTransaction.amount().amount()).isEqualByComparingTo("0.00");
        List<LedgerTransaction> history = ledgerService.history(openingTransaction.accountId());
        assertThat(history).hasSize(1);
        assertThat(history.get(0).type()).isEqualTo(TransactionType.OPENING_BALANCE);
    }

    @Test
    void recordsDepositAndWithdrawalForTheSameAccount() {
        var openingTransaction = ledgerService.openAccount(new BigDecimal("100.00"));
        UUID accountId = openingTransaction.accountId();

        ledgerService.deposit(accountId, new BigDecimal("25.50"));
        ledgerService.withdraw(accountId, new BigDecimal("40.25"));

        assertThat(ledgerService.balance(accountId).amount()).isEqualByComparingTo("85.25");
        List<LedgerTransaction> history = ledgerService.history(accountId);
        assertThat(history).hasSize(3);
        assertThat(history.get(0).type()).isEqualTo(TransactionType.OPENING_BALANCE);
        assertThat(history.get(1).type()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(history.get(2).type()).isEqualTo(TransactionType.WITHDRAWAL);
    }

    @Test
    void keepsAccountsIndependent() {
        UUID firstAccountId = ledgerService.openAccount(new BigDecimal("100.00")).accountId();
        UUID secondAccountId = ledgerService.openAccount(new BigDecimal("50.00")).accountId();

        ledgerService.deposit(firstAccountId, new BigDecimal("20.00"));
        ledgerService.withdraw(secondAccountId, new BigDecimal("10.00"));

        assertThat(ledgerService.balance(firstAccountId).amount()).isEqualByComparingTo("120.00");
        assertThat(ledgerService.balance(secondAccountId).amount()).isEqualByComparingTo("40.00");
    }

    @Test
    void rejectedWithdrawalDoesNotCreateLedgerTransaction() {
        UUID accountId = ledgerService.openAccount(new BigDecimal("20.00")).accountId();

        assertThatThrownBy(() -> ledgerService.withdraw(accountId, new BigDecimal("20.01")))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Insufficient funds");

        assertThat(ledgerService.balance(accountId).amount()).isEqualByComparingTo("20.00");
        assertThat(ledgerService.history(accountId)).hasSize(1);
    }

    @Test
    void rejectsOperationsForUnknownAccount() {
        UUID accountId = UUID.randomUUID();

        assertThatThrownBy(() -> ledgerService.deposit(accountId, new BigDecimal("10.00")))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Account not found: " + accountId);
    }

}