package com.igirepay;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class PaymentHandler implements HttpHandler {
    private final IdempotencyService service;

    public PaymentHandler(IdempotencyService service) {
        this.service = service;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "Method Not Allowed", false);
            return;
        }
        
        String idempotencyKey = exchange.getRequestHeaders().getFirst("Idempotency-Key");
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            sendResponse(exchange, 400, "Missing Idempotency-Key header", false);
            return;
        }
        
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        
        PaymentRequest request;
        try {
            request = PaymentRequest.fromJson(body);
        } catch (Exception e) {
            sendResponse(exchange, 422, "Invalid JSON body", false);
            return;
        }
        
        IdempotencyService.ProcessResult result = service.process(idempotencyKey, request);
        sendResponse(exchange, result.statusCode, result.responseBody, result.isCached);
    }
    
    private void sendResponse(HttpExchange exchange, int statusCode, String responseBody, boolean isCached) throws IOException {
        if (isCached && statusCode == 200) {
            exchange.getResponseHeaders().set("X-Cache-Hit", "true");
        }
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] response = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }
}