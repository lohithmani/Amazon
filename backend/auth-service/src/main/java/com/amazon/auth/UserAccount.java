package com.amazon.auth;

import java.time.Instant;
import java.util.Set;

public class UserAccount {

  private final String email;
  private Long id;
  private String fullName;
  private String passwordHash;
  private Set<String> roles;
  private Instant createdAt = Instant.now();
  private Instant updatedAt = Instant.now();

  public UserAccount(String email, String fullName, String passwordHash, Set<String> roles) {
    this.email = email;
    this.fullName = fullName;
    this.passwordHash = passwordHash;
    this.roles = roles;
  }

  public UserAccount(Long id, String email, String fullName, String passwordHash, Set<String> roles, Instant createdAt, Instant updatedAt) {
    this(email, fullName, passwordHash, roles);
    this.id = id;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public Long id() {
    return id;
  }

  public void assignId(Long id) {
    this.id = id;
  }

  public String email() {
    return email;
  }

  public String fullName() {
    return fullName;
  }

  public String passwordHash() {
    return passwordHash;
  }

  public Set<String> roles() {
    return roles;
  }

  public Instant createdAt() {
    return createdAt;
  }

  public Instant updatedAt() {
    return updatedAt;
  }

  public void updateProfile(String fullName) {
    this.fullName = fullName;
    this.updatedAt = Instant.now();
  }

  public void updatePassword(String passwordHash) {
    this.passwordHash = passwordHash;
    this.updatedAt = Instant.now();
  }

  public void updateRoles(Set<String> roles) {
    this.roles = roles;
    this.updatedAt = Instant.now();
  }
}
