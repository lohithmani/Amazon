package com.amazon.auth;

import java.util.List;

public record JwtPrincipal(String email, List<String> roles) {
}
