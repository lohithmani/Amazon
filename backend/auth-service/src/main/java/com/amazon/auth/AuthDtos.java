package com.amazon.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

record RegisterRequest(@Email String email, @NotBlank String fullName, @NotBlank String password) {
}

record LoginRequest(@Email String email, @NotBlank String password) {
}

record RefreshRequest(@NotBlank String refreshToken) {
}

record ChangePasswordRequest(@NotBlank String currentPassword, @NotBlank String newPassword) {
}

record PasswordResetRequest(@Email String email) {
}

record PasswordResetConfirmRequest(@Email String email, @NotBlank String resetCode, @NotBlank String newPassword) {
}

record AuthResponse(String accessToken, String refreshToken, String tokenType, String email, String fullName, Set<String> roles) {
}

record ResetPasswordResponse(String email, String resetCode, String message) {
}

record AuthProfileResponse(String email, String fullName, Set<String> roles) {
}
