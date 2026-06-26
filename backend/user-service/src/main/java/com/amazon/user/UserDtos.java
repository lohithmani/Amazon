package com.amazon.user;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.Set;

record UpdateProfileRequest(@NotBlank String fullName, String phone) {
}

record CreateAddressRequest(
    @NotBlank String label,
    @NotBlank String line1,
    String line2,
    @NotBlank String city,
    @NotBlank String state,
    @NotBlank String postalCode,
    String country,
    boolean defaultAddress) {
}

record AssignRoleRequest(@NotBlank String role) {
}

record UserProfileResponse(
    String email,
    String fullName,
    String phone,
    Set<String> roles,
    int addressCount,
    Instant createdAt,
    Instant updatedAt) {
}

record AddressResponse(
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
