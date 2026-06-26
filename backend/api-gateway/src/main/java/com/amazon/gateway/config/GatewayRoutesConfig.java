package com.amazon.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

  @Bean
  RouteLocator routeLocator(RouteLocatorBuilder builder) {
    return builder.routes()
        .route("auth-service", route -> route.path("/api/auth/**").uri("lb://auth-service"))
        .route("user-service", route -> route.path("/api/users/**").uri("lb://user-service"))
        .route("product-service", route -> route.path("/api/products/**").uri("lb://product-service"))
        .route("inventory-service", route -> route.path("/api/inventory/**").uri("lb://inventory-service"))
        .route("cart-service", route -> route.path("/api/cart/**").uri("lb://cart-service"))
        .route("order-service", route -> route.path("/api/orders/**").uri("lb://order-service"))
        .route("payment-service", route -> route.path("/api/payments/**").uri("lb://payment-service"))
        .route("notification-service", route -> route.path("/api/notifications/**").uri("lb://notification-service"))
        .build();
  }
}
