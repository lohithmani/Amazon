package com.amazon.user;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

  private final UserRepository repository;

  public UserService(UserRepository repository) {
    this.repository = repository;
  }

  public UserProfileResponse getProfile(String email) {
    return toResponse(findUser(email));
  }

  public UserProfileResponse updateProfile(String email, UpdateProfileRequest request) {
    UserProfile profile = findUser(email);
    profile.update(request.fullName(), request.phone());
    repository.save(profile);
    return toResponse(profile);
  }

  public UserProfileResponse assignRole(String email, AssignRoleRequest request) {
    UserProfile profile = findUser(email);
    profile.addRole(request.role().toUpperCase());
    repository.addRole(profile, request.role().toUpperCase());
    return toResponse(profile);
  }

  public AddressResponse addAddress(String email, CreateAddressRequest request) {
    UserProfile profile = findUser(email);
    Address address = repository.addAddress(profile, request);
    profile.addAddress(address);
    return toAddressResponse(address);
  }

  public List<AddressResponse> listAddresses(String email) {
    return findUser(email).addresses().stream().map(this::toAddressResponse).toList();
  }

  private UserProfile findUser(String email) {
    return repository.findByEmail(email.toLowerCase())
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
  }

  private UserProfileResponse toResponse(UserProfile profile) {
    return new UserProfileResponse(
        profile.email(),
        profile.fullName(),
        profile.phone(),
        profile.roles(),
        profile.addresses().size(),
        profile.createdAt(),
        profile.updatedAt());
  }

  private AddressResponse toAddressResponse(Address address) {
    return new AddressResponse(
        address.id(),
        address.label(),
        address.line1(),
        address.line2(),
        address.city(),
        address.state(),
        address.postalCode(),
        address.country(),
        address.defaultAddress(),
        address.createdAt());
  }
}
