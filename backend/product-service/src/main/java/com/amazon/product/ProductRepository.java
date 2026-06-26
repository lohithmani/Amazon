package com.amazon.product;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductRepository {

  private final JdbcTemplate jdbcTemplate;

  public ProductRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public List<ProductResponse> findAllActive() {
    return jdbcTemplate.query("""
            SELECT p.id,
                   p.name,
                   COALESCE(c.name, 'Uncategorized') AS category,
                   COALESCE(p.brand, 'Generic') AS brand,
                   p.price,
                   COALESCE(pr.average_rating, 0) AS rating,
                   COALESCE(i.quantity_on_hand - i.reserved_quantity, 0) AS stock,
                   p.description,
                   p.image_url
            FROM products p
            LEFT JOIN categories c ON c.id = p.category_id
            LEFT JOIN product_ratings pr ON pr.product_id = p.id
            LEFT JOIN inventory i ON i.product_id = p.id
            WHERE p.active = TRUE
            ORDER BY p.created_at DESC
            """,
        (rs, rowNum) -> new ProductResponse(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("category"),
            rs.getString("brand"),
            rs.getBigDecimal("price"),
            rs.getBigDecimal("rating"),
            rs.getInt("stock"),
            badge(rs.getBigDecimal("rating").doubleValue(), rs.getInt("stock"), rowNum),
            rs.getString("description"),
            rs.getString("image_url")));
  }

  private String badge(double rating, int stock, int index) {
    if (rating >= 4.8) return "Top rated";
    if (stock <= 10) return "Limited stock";
    if (index < 6) return "New";
    return "Available";
  }
}
