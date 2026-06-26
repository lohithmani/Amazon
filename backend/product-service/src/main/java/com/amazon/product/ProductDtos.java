package com.amazon.product;

import java.math.BigDecimal;

record ProductResponse(
    long id,
    String name,
    String category,
    String brand,
    BigDecimal price,
    BigDecimal rating,
    int stock,
    String badge,
    String description,
    String image) {
}
