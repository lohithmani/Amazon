package com.amazon.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class UserRepository {

  private final JdbcTemplate jdbcTemplate;

  public UserRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public Optional<UserProfile> findByEmail(String email) {
    String normalized = email.toLowerCase();
    return jdbcTemplate.query("""
            SELECT id, email, full_name, phone, created_at, updated_at
            FROM users
            WHERE lower(email) = ?
            """,
        (rs, rowNum) -> new UserProfile(
            rs.getLong("id"),
            rs.getString("email"),
            rs.getString("full_name"),
            rs.getString("phone"),
            rolesForUser(rs.getLong("id")),
            addressesForUser(rs.getLong("id")),
            rs.getTimestamp("created_at").toInstant(),
            rs.getTimestamp("updated_at").toInstant()),
        normalized).stream().findFirst();
  }

  @Transactional
  public UserProfile save(UserProfile profile) {
    jdbcTemplate.update("""
            UPDATE users
            SET full_name = ?, phone = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """,
        profile.fullName(),
        profile.phone(),
        profile.id());
    return profile;
  }

  @Transactional
  public void addRole(UserProfile profile, String role) {
    Long roleId = jdbcTemplate.queryForObject("""
            INSERT INTO roles (name)
            VALUES (?)
            ON CONFLICT (name) DO UPDATE SET name = EXCLUDED.name
            RETURNING id
            """,
        Long.class,
        role.toUpperCase());
    jdbcTemplate.update("""
            INSERT INTO user_roles (user_id, role_id)
            VALUES (?, ?)
            ON CONFLICT DO NOTHING
            """,
        profile.id(),
        roleId);
  }

  public Address addAddress(UserProfile profile, CreateAddressRequest request) {
    return jdbcTemplate.queryForObject("""
            INSERT INTO addresses (user_id, label, line1, line2, city, state, postal_code, country, is_default)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id, label, line1, line2, city, state, postal_code, country, is_default, created_at
            """,
        (rs, rowNum) -> new Address(
            rs.getLong("id"),
            rs.getString("label"),
            rs.getString("line1"),
            rs.getString("line2"),
            rs.getString("city"),
            rs.getString("state"),
            rs.getString("postal_code"),
            rs.getString("country"),
            rs.getBoolean("is_default"),
            rs.getTimestamp("created_at").toInstant()),
        profile.id(),
        request.label(),
        request.line1(),
        request.line2(),
        request.city(),
        request.state(),
        request.postalCode(),
        request.country() == null || request.country().isBlank() ? "India" : request.country(),
        request.defaultAddress());
  }

  private Set<String> rolesForUser(long userId) {
    return jdbcTemplate.queryForList("""
            SELECT r.name
            FROM roles r
            JOIN user_roles ur ON ur.role_id = r.id
            WHERE ur.user_id = ?
            ORDER BY r.name
            """,
        String.class,
        userId).stream().collect(Collectors.toUnmodifiableSet());
  }

  private java.util.List<Address> addressesForUser(long userId) {
    return jdbcTemplate.query("""
            SELECT id, label, line1, line2, city, state, postal_code, country, is_default, created_at
            FROM addresses
            WHERE user_id = ?
            ORDER BY created_at DESC
            """,
        (rs, rowNum) -> new Address(
            rs.getLong("id"),
            rs.getString("label"),
            rs.getString("line1"),
            rs.getString("line2"),
            rs.getString("city"),
            rs.getString("state"),
            rs.getString("postal_code"),
            rs.getString("country"),
            rs.getBoolean("is_default"),
            rs.getTimestamp("created_at").toInstant()),
        userId);
  }
}
