package com.microservices.orderservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private static final String SERVICE = "order-service";

    private final RestTemplate restTemplate;

    public String createOrder(String failure) {

        String requestId = UUID.randomUUID().toString();

        long start = System.currentTimeMillis();

        log.info("service={} requestId={} event=request_received",
                SERVICE, requestId);

        try {

            log.info("service={} requestId={} event=call_user_service",
                    SERVICE, requestId);

            restTemplate.getForObject(
                    "http://localhost:8081/users/1",
                    String.class
            );

            log.info("service={} requestId={} event=user_service_success",
                    SERVICE, requestId);

            log.info("service={} requestId={} event=call_payment_service",
                    SERVICE, requestId);

            // pass failure to payment service
            String url = "http://localhost:8083/payments";

            if (failure != null) {
                url += "?failure=" + failure;
            }

            restTemplate.postForObject(
                    url,
                    null,
                    String.class
            );

            log.info("service={} requestId={} event=payment_success",
                    SERVICE, requestId);

        } catch (Exception e) {

            log.error("service={} requestId={} event=order_failed error={}",
                    SERVICE, requestId, e.getMessage());

            throw e;
        }

        long latency = System.currentTimeMillis() - start;

        if (latency > 1000) {
            log.warn("service={} requestId={} event=slow_response latency={}",
                    SERVICE, requestId, latency);
        }

        log.info("service={} requestId={} event=response_sent latency={}",
                SERVICE, requestId, latency);

        return "order-created";
    }
}