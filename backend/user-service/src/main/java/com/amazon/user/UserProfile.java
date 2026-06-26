package com.amazon.user;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class UserProfile {

  private final long id;
  private final String email;
  private String fullName;
  private String phone;
  private final Set<String> roles = new LinkedHashSet<>();
  private final List<Address> addresses = new ArrayList<>();
  private Instant createdAt = Instant.now();
  private Instant updatedAt = Instant.now();

  public UserProfile(long id, String email, String fullName, String phone, Set<String> roles, List<Address> addresses, Instant createdAt, Instant updatedAt) {
    this.id = id;
    this.email = email;
    this.fullName = fullName;
    this.phone = phone;
    this.roles.addAll(roles);
    this.addresses.addAll(addresses);
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public long id() {
    return id;
  }

  public String email() {
    return email;
  }

  public String fullName() {
    return fullName;
  }

  public String phone() {
    return phone;
  }

  public Set<String> roles() {
    return roles;
  }

  public List<Address> addresses() {
    return addresses;
  }

  public Instant createdAt() {
    return createdAt;
  }

  public Instant updatedAt() {
    return updatedAt;
  }

  public void update(String fullName, String phone) {
    this.fullName = fullName;
    this.phone = phone;
    this.updatedAt = Instant.now();
  }

  public void addRole(String role) {
    this.roles.add(role);
    this.updatedAt = Instant.now();
  }

  public void addAddress(Address address) {
    this.addresses.add(address);
    this.updatedAt = Instant.now();
  }
}
