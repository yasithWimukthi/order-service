package com.microservices.orderservice.service;

import com.microservices.orderservice.model.LogEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private static final String SERVICE = "order-service";

    private final RestTemplate restTemplate;
    private final LogSender logSender;

    public String createOrder(String failure) {

        String requestId = UUID.randomUUID().toString();

        long start = System.currentTimeMillis();

        log.info("service={} requestId={} event=request_received",
                SERVICE, requestId);

        logSender.send(new LogEvent(
                Instant.now().toString(),
                "order-service",
                "INFO",
                "request_received",
                requestId,
                null,
                null
        ));

        try {

            log.info("service={} requestId={} event=call_user_service",
                    SERVICE, requestId);

            logSender.send(new LogEvent(
                    Instant.now().toString(),
                    "order-service",
                    "INFO",
                    "call_user_service",
                    requestId,
                    null,
                    null
            ));

            restTemplate.getForObject(
                    "http://localhost:8081/users/1",
                    String.class
            );

            log.info("service={} requestId={} event=user_service_success",
                    SERVICE, requestId);

            logSender.send(new LogEvent(
                    Instant.now().toString(),
                    "order-service",
                    "INFO",
                    "user_service_success",
                    requestId,
                    null,
                    null
            ));

            log.info("service={} requestId={} event=call_payment_service",
                    SERVICE, requestId);

            logSender.send(new LogEvent(
                    Instant.now().toString(),
                    "order-service",
                    "INFO",
                    "call_payment_service",
                    requestId,
                    null,
                    null
            ));

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

            logSender.send(new LogEvent(
                    Instant.now().toString(),
                    "order-service",
                    "INFO",
                    "payment_success",
                    requestId,
                    null,
                    null
            ));

        } catch (Exception e) {

            log.error("service={} requestId={} event=order_failed error={}",
                    SERVICE, requestId, e.getMessage());

            logSender.send(new LogEvent(
                    Instant.now().toString(),
                    "order-service",
                    "ERROR",
                    "order_failed",
                    requestId,
                    null,
                    e.getMessage()
            ));

            throw e;
        }

        long latency = System.currentTimeMillis() - start;

        if (latency > 1000) {
            log.warn("service={} requestId={} event=slow_response latency={}",
                    SERVICE, requestId, latency);

            logSender.send(new LogEvent(
                    Instant.now().toString(),
                    "order-service",
                    "WARN",
                    "slow_response",
                    requestId,
                    latency,
                    null
            ));
        }

        log.info("service={} requestId={} event=response_sent latency={}",
                SERVICE, requestId, latency);

        logSender.send(new LogEvent(
                Instant.now().toString(),
                "order-service",
                "INFO",
                "response_sent",
                requestId,
                latency,
                null
        ));

        return "order-created";
    }
}