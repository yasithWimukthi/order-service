package com.microservices.orderservice.service;

import com.microservices.orderservice.model.LogEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.UUID;


@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final RestTemplate restTemplate;
    private final LogSender logSender;

    private static final String SERVICE = "order-service";

    public String createOrder(String failure, int duration) {

        String requestId = UUID.randomUUID().toString();
        long start = System.currentTimeMillis();

        sendLog("INFO", "request_received", requestId, null, null);

        boolean userSuccess = false;
        boolean paymentSuccess = false;

        try {

            // =========================
            // USER SERVICE CALL
            // =========================
            long userStart = System.currentTimeMillis();

            sendLog("INFO", "call_user_service", requestId, null, null);

            restTemplate.getForObject(
                    "http://localhost:8081/users/1",
                    String.class
            );

            long userLatency = System.currentTimeMillis() - userStart;

            sendLog("INFO", "user_service_success", requestId, userLatency, null);

            userSuccess = true;

        } catch (Exception e) {

            sendLog("ERROR", "user_service_failure",
                    requestId, null, e.getMessage());
        }

        try {

            // =========================
            // PAYMENT SERVICE CALL
            // =========================
            long paymentStart = System.currentTimeMillis();

            sendLog("INFO", "call_payment_service", requestId, null, null);

            String url = "http://localhost:8083/payments";

            if (failure != null) {
                url += "?failure=" + failure + "&duration=" + duration;
            }

            // Retry logic
            int retries = 0;
            while (retries < 2) {

                try {
                    restTemplate.postForObject(url, null, String.class);
                    paymentSuccess = true;
                    break;

                } catch (Exception ex) {

                    retries++;

                    sendLog("WARN", "payment_retry_attempt",
                            requestId, null,
                            "Retry attempt " + retries);

                    Thread.sleep(200);
                }
            }

            long paymentLatency = System.currentTimeMillis() - paymentStart;

            if (paymentSuccess) {
                sendLog("INFO", "payment_success", requestId, paymentLatency, null);
            } else {
                sendLog("ERROR", "payment_failure", requestId, paymentLatency,
                        "Failed after retries");
            }

        } catch (Exception e) {

            sendLog("ERROR", "payment_service_exception",
                    requestId, null, e.getMessage());
        }

        // =========================
        // CASCADING FAILURE LOGIC
        // =========================
        if (!userSuccess || !paymentSuccess) {

            sendLog("WARN", "partial_failure_detected",
                    requestId, null,
                    "userSuccess=" + userSuccess + ", paymentSuccess=" + paymentSuccess);
        }

        long latency = System.currentTimeMillis() - start;

        if (latency > 1000) {

            sendLog("WARN", "slow_response",
                    requestId, latency,
                    "Order processing is slow");
        }

        // =========================
        // FINAL STATUS
        // =========================
        if (userSuccess && paymentSuccess) {

            sendLog("INFO", "request_completed_success",
                    requestId, latency, null);

        } else {

            sendLog("ERROR", "request_completed_failure",
                    requestId, latency,
                    "Order failed due to dependency issues");
        }

        sendLog("INFO", "response_sent", requestId, latency, null);

        return userSuccess && paymentSuccess ? "order-success" : "order-failed";
    }

    private void sendLog(String level, String event,
                         String requestId, Long latency, String error) {

        logSender.send(new LogEvent(
                Instant.now().toString(),
                SERVICE,
                level,
                event,
                requestId,
                latency,
                error
        ));
    }
}