package com.microservices.orderservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogEvent {

    private String timestamp;
    private String service;
    private String level;
    private String event;
    private String requestId;
    private Long latency;
    private String errorMessage;
}