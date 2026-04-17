package com.microservices.orderservice.service;

import com.microservices.orderservice.model.LogEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class LogSender {

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String COLLECTOR_URL = "http://localhost:8090/logs";

    public void send(LogEvent logEvent) {

        try {
            restTemplate.postForObject(
                    COLLECTOR_URL,
                    logEvent,
                    Void.class
            );
        } catch (Exception e) {
            // do not break main flow
        }
    }
}