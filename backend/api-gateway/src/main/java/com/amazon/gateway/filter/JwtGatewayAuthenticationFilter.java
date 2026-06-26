package com.amazon.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtGatewayAuthenticationFilter implements GlobalFilter, Ordered {

  private final SecretKey signingKey;
  private final List<String> publicPaths;

  public JwtGatewayAuthenticationFilter(
      @Value("${auth.jwt.secret}") String secret,
      @Value("${auth.public-paths}") String publicPaths) {
    this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.publicPaths = Arrays.stream(publicPaths.split(",")).map(String::trim).toList();
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    String path = exchange.getRequest().getPath().value();
    if (isPublic(path)) {
      return chain.filter(exchange);
    }

    String header = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    if (header == null || !header.startsWith("Bearer ")) {
      exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
      return exchange.getResponse().setComplete();
    }

    try {
      Claims claims = Jwts.parser()
          .verifyWith(signingKey)
          .build()
          .parseSignedClaims(header.substring(7))
          .getPayload();

      if (!"access".equals(claims.get("tokenType", String.class))) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
      }

      ServerHttpRequest request = exchange.getRequest().mutate()
          .header("X-Auth-Email", claims.getSubject())
          .header("X-Auth-Roles", String.join(",", claims.get("roles", List.class)))
          .build();
      return chain.filter(exchange.mutate().request(request).build());
    } catch (RuntimeException ex) {
      exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
      return exchange.getResponse().setComplete();
    }
  }

  @Override
  public int getOrder() {
    return -10;
  }

  private boolean isPublic(String path) {
    return publicPaths.stream().anyMatch(path::equals) || path.startsWith("/actuator/");
  }
}
