package com.amazon.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class SimpleRateLimitFilter implements GlobalFilter, Ordered {

  private static final int LIMIT = 100;
  private static final long WINDOW_SECONDS = 60;
  private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    String key = exchange.getRequest().getRemoteAddress() != null
        ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
        : "unknown";

    Bucket bucket = buckets.computeIfAbsent(key, ignored -> new Bucket());
    if (!bucket.tryConsume()) {
      exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
      return exchange.getResponse().setComplete();
    }

    return chain.filter(exchange);
  }

  @Override
  public int getOrder() {
    return -2;
  }

  private static final class Bucket {
    private final AtomicInteger count = new AtomicInteger();
    private volatile long windowStart = Instant.now().getEpochSecond();

    synchronized boolean tryConsume() {
      long now = Instant.now().getEpochSecond();
      if (now - windowStart >= WINDOW_SECONDS) {
        windowStart = now;
        count.set(0);
      }
      return count.incrementAndGet() <= LIMIT;
    }
  }
}
