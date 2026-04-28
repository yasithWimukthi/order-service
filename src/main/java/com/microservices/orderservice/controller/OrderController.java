package com.microservices.orderservice.controller;

import com.microservices.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<String> createOrder(
            @RequestParam(required = false) String failure,
            @RequestParam(required = false, defaultValue = "100") int duration
    ) {
        return ResponseEntity.ok(orderService.createOrder(failure, duration));
    }
}