package com.teya.tinyledger.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    void normalisesAmountToTwoDecimalPlaces(){
        Money money = Money.of(new BigDecimal("10"));

        assertThat(money.amount()).isEqualByComparingTo("10.00");
        assertThat(money.amount().scale()).isEqualTo(2);
    }

    @Test
    void addsMoney() {
        Money originalBalance = Money.of(new BigDecimal("100.00"));
        Money addMoney = Money.of(new BigDecimal("25.50"));

        assertThat(originalBalance.add(addMoney).amount())
                .isEqualByComparingTo("125.50");
    }

    @Test
    void subtractsMoney() {
        Money originalBalance = Money.of(new BigDecimal("100.00"));
        Money subtractsMoney = Money.of(new BigDecimal("25.50"));

        assertThat(originalBalance.subtract(subtractsMoney).amount())
                .isEqualByComparingTo("74.50");
    }

    @Test
    void rejectNegativeAmount() {
        assertThatThrownBy( () -> Money.of(new BigDecimal("-1.00")))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Amount must not be negative");
    }

    @Test
    void rejectsMoreThanTwoDecimalPlaces() {
        assertThatThrownBy(() -> Money.of(new BigDecimal("1.234")))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Amount must not have more than two decimal places");
    }
}