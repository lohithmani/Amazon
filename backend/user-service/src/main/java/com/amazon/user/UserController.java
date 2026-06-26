package com.amazon.user;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/{email}")
  UserProfileResponse getProfile(@PathVariable String email) {
    return userService.getProfile(email);
  }

  @PutMapping("/{email}")
  UserProfileResponse updateProfile(@PathVariable String email, @Valid @RequestBody UpdateProfileRequest request) {
    return userService.updateProfile(email, request);
  }

  @PostMapping("/{email}/addresses")
  AddressResponse addAddress(@PathVariable String email, @Valid @RequestBody CreateAddressRequest request) {
    return userService.addAddress(email, request);
  }

  @GetMapping("/{email}/addresses")
  List<AddressResponse> listAddresses(@PathVariable String email) {
    return userService.listAddresses(email);
  }

  @PostMapping("/{email}/roles")
  UserProfileResponse assignRole(@PathVariable String email, @Valid @RequestBody AssignRoleRequest request) {
    return userService.assignRole(email, request);
  }
}
