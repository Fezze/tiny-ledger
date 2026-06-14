package com.teya.tinyledger.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record Money(BigDecimal amount) implements Comparable<Money> {

    private static final int SCALE = 2;

    public Money {
        if (amount == null) {
            throw new RuntimeException("Amount is required");
        }
        if (amount.signum() < 0) {
            throw new RuntimeException("Amount must not be negative");
        }
        if (amount.stripTrailingZeros().scale() > SCALE) {
            throw new RuntimeException("Amount must not have more than two decimal places");
        }
        amount = amount.setScale(SCALE, RoundingMode.UNNECESSARY);
    }

    public static Money of(BigDecimal amount) {
        return new Money(amount);
    }

    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }

    public Money subtract(Money other) {
        return new Money(this.amount.subtract(other.amount));
    }

    public boolean isPositive() {
        return amount.signum() > 0;
    }

    public boolean isLessThan(Money other) {
        return amount.compareTo(other.amount) < 0;
    }


    @Override
    public int compareTo(Money other) {
        return amount.compareTo(other.amount);
    }
}
