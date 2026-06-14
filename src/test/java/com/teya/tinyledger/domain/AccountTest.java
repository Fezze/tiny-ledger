package com.teya.tinyledger.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountTest {

    @Test
    void openingAccountCreatesAccount() {
        Account account = Account.open(Money.of(new BigDecimal("100.00")));

        assertThat(account.balance().amount()).isEqualByComparingTo("100.00");
        assertThat(account.id()).isNotNull();
    }

    @Test
    void snapshotOfAccountReturnsAccountSnapshot() {
        Account account = Account.open(Money.of(new BigDecimal("100.00")));
        AccountSnapshot snapshot = account.snapshot();

        assertThat(snapshot.balance().amount()).isEqualByComparingTo("100.00");
        assertThat(snapshot.id()).isNotNull();
    }

}
