package com.amazon.order;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

  private final OrderRepository orderRepository;

  public OrderController(OrderRepository orderRepository) {
    this.orderRepository = orderRepository;
  }

  @GetMapping
  List<OrderResponse> listOrders(@RequestHeader("X-Auth-Email") String email) {
    return orderRepository.findByEmail(email);
  }

  @PostMapping
  OrderResponse createOrder(@RequestHeader("X-Auth-Email") String email, @Valid @RequestBody CreateOrderRequest request) {
    return orderRepository.create(email, request);
  }
}
