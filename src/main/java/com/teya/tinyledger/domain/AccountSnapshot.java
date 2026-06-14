package com.teya.tinyledger.domain;

import java.util.UUID;

public record AccountSnapshot(UUID id, Money balance) {
}
