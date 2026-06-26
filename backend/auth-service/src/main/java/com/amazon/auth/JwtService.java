package com.amazon.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class JwtService {

  private final SecretKey signingKey;
  private final String issuer;
  private final long accessTokenMinutes;
  private final long refreshTokenDays;

  public JwtService(
      @Value("${auth.jwt.secret}") String secret,
      @Value("${auth.jwt.issuer}") String issuer,
      @Value("${auth.jwt.access-token-minutes}") long accessTokenMinutes,
      @Value("${auth.jwt.refresh-token-days}") long refreshTokenDays) {
    this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.issuer = issuer;
    this.accessTokenMinutes = accessTokenMinutes;
    this.refreshTokenDays = refreshTokenDays;
  }

  public String generateAccessToken(UserAccount account) {
    return buildToken(account.email(), account.roles(), "access", Instant.now().plus(accessTokenMinutes, ChronoUnit.MINUTES));
  }

  public String generateRefreshToken(UserAccount account) {
    return buildToken(account.email(), account.roles(), "refresh", Instant.now().plus(refreshTokenDays, ChronoUnit.DAYS));
  }

  public JwtPrincipal parseAccessToken(String token) {
    Claims claims = Jwts.parser()
        .verifyWith(signingKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();

    if (!"access".equals(claims.get("tokenType", String.class))) {
      throw new IllegalArgumentException("Not an access token");
    }

    List<String> roles = claims.get("roles", List.class);
    return new JwtPrincipal(claims.getSubject(), roles);
  }

  public JwtPrincipal parseRefreshToken(String token) {
    Claims claims = Jwts.parser()
        .verifyWith(signingKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();

    if (!"refresh".equals(claims.get("tokenType", String.class))) {
      throw new IllegalArgumentException("Not a refresh token");
    }

    List<String> roles = claims.get("roles", List.class);
    return new JwtPrincipal(claims.getSubject(), roles);
  }

  private String buildToken(String subject, Set<String> roles, String tokenType, Instant expiresAt) {
    return Jwts.builder()
        .issuer(issuer)
        .subject(subject)
        .claims(Map.of(
            "roles", roles.stream().toList(),
            "tokenType", tokenType))
        .issuedAt(new Date())
        .expiration(Date.from(expiresAt))
        .signWith(signingKey)
        .compact();
  }
}
