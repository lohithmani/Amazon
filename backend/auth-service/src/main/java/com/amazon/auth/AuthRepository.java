package com.amazon.auth;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class AuthRepository {

  private final JdbcTemplate jdbcTemplate;

  public AuthRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public Optional<UserAccount> findByEmail(String email) {
    String normalized = email.toLowerCase();
    return jdbcTemplate.query("""
            SELECT id, email, full_name, password_hash, created_at, updated_at
            FROM users
            WHERE lower(email) = ?
            """,
        (rs, rowNum) -> new UserAccount(
            rs.getLong("id"),
            rs.getString("email"),
            rs.getString("full_name"),
            rs.getString("password_hash"),
            rolesForUser(rs.getLong("id")),
            rs.getTimestamp("created_at").toInstant(),
            rs.getTimestamp("updated_at").toInstant()),
        normalized).stream().findFirst();
  }

  @Transactional
  public UserAccount save(UserAccount userAccount) {
    Long existingId = userAccount.id();
    if (existingId == null) {
      existingId = jdbcTemplate.queryForObject("""
              INSERT INTO users (email, full_name, password_hash)
              VALUES (?, ?, ?)
              RETURNING id
              """,
          Long.class,
          userAccount.email().toLowerCase(),
          userAccount.fullName(),
          userAccount.passwordHash());
      userAccount.assignId(existingId);
    } else {
      jdbcTemplate.update("""
              UPDATE users
              SET full_name = ?, password_hash = ?, updated_at = CURRENT_TIMESTAMP
              WHERE id = ?
              """,
          userAccount.fullName(),
          userAccount.passwordHash(),
          existingId);
    }

    saveRoles(existingId, userAccount.roles());
    return userAccount;
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

  private void saveRoles(long userId, Set<String> roles) {
    for (String role : roles) {
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
          userId,
          roleId);
    }
  }
}
