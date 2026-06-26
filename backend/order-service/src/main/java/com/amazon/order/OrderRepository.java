package com.amazon.order;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public class OrderRepository {

  private final JdbcTemplate jdbcTemplate;

  public OrderRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public List<OrderResponse> findByEmail(String email) {
    return jdbcTemplate.query("""
            SELECT o.id, o.order_number, o.status, o.grand_total, o.placed_at,
                   COALESCE(SUM(oi.quantity), 0) AS item_count
            FROM orders o
            JOIN users u ON u.id = o.user_id
            LEFT JOIN order_items oi ON oi.order_id = o.id
            WHERE lower(u.email) = ?
            GROUP BY o.id
            ORDER BY o.created_at DESC
            """,
        (rs, rowNum) -> new OrderResponse(
            rs.getLong("id"),
            rs.getString("order_number"),
            rs.getString("status"),
            rs.getBigDecimal("grand_total"),
            rs.getTimestamp("placed_at").toInstant(),
            rs.getInt("item_count")),
        email.toLowerCase());
  }

  @Transactional
  public OrderResponse create(String email, CreateOrderRequest request) {
    Long userId = jdbcTemplate.queryForObject("SELECT id FROM users WHERE lower(email) = ?", Long.class, email.toLowerCase());
    BigDecimal subtotal = BigDecimal.ZERO;
    int itemCount = 0;

    for (CreateOrderItemRequest item : request.items()) {
      BigDecimal price = jdbcTemplate.queryForObject("SELECT price FROM products WHERE id = ? AND active = TRUE", BigDecimal.class, item.productId());
      subtotal = subtotal.add(price.multiply(BigDecimal.valueOf(item.quantity())));
      itemCount += item.quantity();
    }

    String orderNumber = "AMZ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    Long orderId = jdbcTemplate.queryForObject("""
            INSERT INTO orders (user_id, order_number, status, subtotal, grand_total, placed_at)
            VALUES (?, ?, 'PLACED', ?, ?, CURRENT_TIMESTAMP)
            RETURNING id
            """,
        Long.class,
        userId,
        orderNumber,
        subtotal,
        subtotal);

    for (CreateOrderItemRequest item : request.items()) {
      jdbcTemplate.update("""
              INSERT INTO order_items (order_id, product_id, product_name, quantity, unit_price, line_total)
              SELECT ?, p.id, p.name, ?, p.price, p.price * ?
              FROM products p
              WHERE p.id = ? AND p.active = TRUE
              """,
          orderId,
          item.quantity(),
          item.quantity(),
          item.productId());
    }

    return new OrderResponse(orderId, orderNumber, "PLACED", subtotal, Instant.now(), itemCount);
  }
}
