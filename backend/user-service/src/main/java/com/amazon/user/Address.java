package com.amazon.user;

import java.time.Instant;

public record Address(
    long id,
    String label,
    String line1,
    String line2,
    String city,
    String state,
    String postalCode,
    String country,
    boolean defaultAddress,
    Instant createdAt) {
}
