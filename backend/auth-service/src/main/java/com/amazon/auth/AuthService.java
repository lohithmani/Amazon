package com.amazon.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

  private final AuthRepository repository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final Map<String, ResetToken> resetTokens = new ConcurrentHashMap<>();
  private final SecureRandom secureRandom = new SecureRandom();

  public AuthService(AuthRepository repository, PasswordEncoder passwordEncoder, JwtService jwtService) {
    this.repository = repository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
  }

  public AuthResponse register(RegisterRequest request) {
    String email = normalize(request.email());
    if (repository.findByEmail(email).isPresent()) {
      throw new IllegalArgumentException("Email already registered");
    }

    UserAccount account = new UserAccount(
        email,
        request.fullName(),
        passwordEncoder.encode(request.password()),
        Set.of("CUSTOMER"));
    repository.save(account);
    return createResponse(account);
  }

  public AuthResponse login(LoginRequest request) {
    UserAccount account = repository.findByEmail(normalize(request.email()))
        .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

    if (!passwordEncoder.matches(request.password(), account.passwordHash())) {
      throw new IllegalArgumentException("Invalid credentials");
    }

    return createResponse(account);
  }

  public AuthResponse refresh(RefreshRequest request) {
    JwtPrincipal principal = jwtService.parseRefreshToken(request.refreshToken());
    UserAccount account = repository.findByEmail(normalize(principal.email()))
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
    return createResponse(account);
  }

  public AuthResponse changePassword(String email, ChangePasswordRequest request) {
    UserAccount account = repository.findByEmail(normalize(email))
        .orElseThrow(() -> new IllegalArgumentException("User not found"));

    if (!passwordEncoder.matches(request.currentPassword(), account.passwordHash())) {
      throw new IllegalArgumentException("Current password is invalid");
    }

    account.updatePassword(passwordEncoder.encode(request.newPassword()));
    repository.save(account);
    return createResponse(account);
  }

  public ResetPasswordResponse requestReset(PasswordResetRequest request) {
    String email = normalize(request.email());
    if (repository.findByEmail(email).isEmpty()) {
      throw new IllegalArgumentException("User not found");
    }

    String code = generateResetCode();
    resetTokens.put(email, new ResetToken(code, Instant.now().plusSeconds(15 * 60)));
    return new ResetPasswordResponse(email, code, "Password reset code generated");
  }

  public AuthResponse resetPassword(PasswordResetConfirmRequest request) {
    String email = normalize(request.email());
    ResetToken token = resetTokens.get(email);
    if (token == null || token.expiresAt().isBefore(Instant.now()) || !token.code().equals(request.resetCode())) {
      throw new IllegalArgumentException("Invalid reset code");
    }

    UserAccount account = repository.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
    account.updatePassword(passwordEncoder.encode(request.newPassword()));
    repository.save(account);
    resetTokens.remove(email);
    return createResponse(account);
  }

  public AuthProfileResponse me(String email) {
    UserAccount account = repository.findByEmail(normalize(email))
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
    return new AuthProfileResponse(account.email(), account.fullName(), account.roles());
  }

  public String adminPing() {
    return "admin access granted";
  }

  private AuthResponse createResponse(UserAccount account) {
    return new AuthResponse(
        jwtService.generateAccessToken(account),
        jwtService.generateRefreshToken(account),
        "Bearer",
        account.email(),
        account.fullName(),
        account.roles());
  }

  private String generateResetCode() {
    byte[] bytes = new byte[6];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes).substring(0, 8);
  }

  private String normalize(String email) {
    return email.toLowerCase().trim();
  }
  private record ResetToken(String code, Instant expiresAt) {
  }
}
