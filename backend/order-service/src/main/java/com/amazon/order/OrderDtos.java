package com.amazon.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

record CreateOrderRequest(@NotEmpty List<@Valid CreateOrderItemRequest> items) {
}

record CreateOrderItemRequest(@Positive long productId, @Positive int quantity) {
}

record OrderResponse(
    long id,
    String number,
    String status,
    BigDecimal total,
    Instant placedAt,
    int items) {
}
