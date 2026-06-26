package com.amazon.auth;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  AuthResponse register(@Valid @RequestBody RegisterRequest request) {
    return authService.register(request);
  }

  @PostMapping("/login")
  AuthResponse login(@Valid @RequestBody LoginRequest request) {
    return authService.login(request);
  }

  @PostMapping("/refresh")
  AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
    return authService.refresh(request);
  }

  @PostMapping("/change-password")
  AuthResponse changePassword(Authentication authentication, @Valid @RequestBody ChangePasswordRequest request) {
    return authService.changePassword(authentication.getName(), request);
  }

  @PostMapping("/password/reset-request")
  ResetPasswordResponse requestReset(@Valid @RequestBody PasswordResetRequest request) {
    return authService.requestReset(request);
  }

  @PostMapping("/password/reset")
  AuthResponse resetPassword(@Valid @RequestBody PasswordResetConfirmRequest request) {
    return authService.resetPassword(request);
  }

  @GetMapping("/me")
  AuthProfileResponse me(Authentication authentication) {
    return authService.me(authentication.getName());
  }

  @GetMapping("/admin/ping")
  String adminPing() {
    return authService.adminPing();
  }
}
