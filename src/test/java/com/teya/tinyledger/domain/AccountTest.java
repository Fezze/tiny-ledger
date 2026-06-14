package com.teya.tinyledger.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AccountTest {

    public static final Money ZERO = new Money(BigDecimal.ZERO);

    @Test
    void openingAccountCreatesOpeningBalanceTransaction() {
        AccountOpening opening = Account.open(Money.of(new BigDecimal("100.00")));

        assertThat(opening.account().balance().amount()).isEqualByComparingTo("100.00");
        assertThat(opening.openingTransaction().accountId()).isEqualTo(opening.account().id());
        assertThat(opening.openingTransaction().type()).isEqualTo(TransactionType.OPENING_BALANCE);
        assertThat(opening.openingTransaction().amount().amount()).isEqualByComparingTo("100.00");
    }

    @Test
    void depositIncreasesBalanceAndReturnsDepositTransaction() {
        Account account = Account.open(Money.of(new BigDecimal("100.00"))).account();

        LedgerTransaction transaction = account.deposit(Money.of(new BigDecimal("25.50")));

        assertThat(account.balance().amount()).isEqualByComparingTo("125.50");
        assertThat(transaction.accountId()).isEqualTo(account.id());
        assertThat(transaction.type()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(transaction.amount().amount()).isEqualByComparingTo("25.50");
    }

    @Test
    void withdrawalDecreasesBalanceAndReturnsWithdrawalTransaction() {
        Account account = Account.open(Money.of(new BigDecimal("100.00"))).account();

        LedgerTransaction transaction = account.withdraw(Money.of(new BigDecimal("40.25")));

        assertThat(account.balance().amount()).isEqualByComparingTo("59.75");
        assertThat(transaction.accountId()).isEqualTo(account.id());
        assertThat(transaction.type()).isEqualTo(TransactionType.WITHDRAWAL);
        assertThat(transaction.amount().amount()).isEqualByComparingTo("40.25");
    }

    @Test
    void rejectsZeroDeposit() {
        Account account = Account.open(Money.of(new BigDecimal("100.00"))).account();

        assertThatThrownBy(() -> account.deposit(ZERO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Amount must be greater than zero");
    }

    @Test
    void rejectsZeroWithdrawal() {
        Account account = Account.open(Money.of(new BigDecimal("100.00"))).account();

        assertThatThrownBy(() -> account.withdraw(ZERO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Amount must be greater than zero");
    }

    @Test
    void rejectsWithdrawalWhenBalanceIsInsufficient() {
        Account account = Account.open(Money.of(new BigDecimal("20.00"))).account();

        assertThatThrownBy(() -> account.withdraw(Money.of(new BigDecimal("20.01"))))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Insufficient funds");

        assertThat(account.balance().amount()).isEqualByComparingTo("20.00");
    }

}
