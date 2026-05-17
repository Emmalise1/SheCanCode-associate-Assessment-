package com.igirepay;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class IdempotencyService {
    private final ConcurrentHashMap<String, IdempotencyRecord> storage = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();
    private static final long TTL_MILLIS = 24 * 60 * 60 * 1000;

    public ProcessResult process(String idempotencyKey, PaymentRequest request) {
        IdempotencyRecord existing = storage.get(idempotencyKey);
        if (existing != null && !isExpired(existing)) {
            if (!existing.getBodyHash().equals(request.computeHash())) {
                return new ProcessResult(409, "Idempotency key already used for a different request body.", true);
            }
            return new ProcessResult(existing.getStatusCode(), existing.getResponseBody(), true);
        }
        
        if (existing != null && isExpired(existing)) {
            storage.remove(idempotencyKey);
        }
        
        ReentrantLock lock = locks.computeIfAbsent(idempotencyKey, k -> new ReentrantLock());
        lock.lock();
        try {
            existing = storage.get(idempotencyKey);
            if (existing != null && !isExpired(existing)) {
                if (!existing.getBodyHash().equals(request.computeHash())) {
                    return new ProcessResult(409, "Idempotency key already used for a different request body.", true);
                }
                return new ProcessResult(existing.getStatusCode(), existing.getResponseBody(), true);
            }
            
            Thread.sleep(2000);
            String responseBody = "Charged " + request.getAmount() + " " + request.getCurrency();
            IdempotencyRecord record = new IdempotencyRecord(200, responseBody, request.computeHash());
            storage.put(idempotencyKey, record);
            return new ProcessResult(200, responseBody, false);
        } catch (InterruptedException e) {
            return new ProcessResult(500, "Processing interrupted", true);
        } finally {
            lock.unlock();
            locks.remove(idempotencyKey);
        }
    }
    
    private boolean isExpired(IdempotencyRecord record) {
        return System.currentTimeMillis() - record.getCreatedAt() > TTL_MILLIS;
    }
    
    public void cleanupExpired() {
        storage.entrySet().removeIf(entry -> isExpired(entry.getValue()));
    }
    
    public static class ProcessResult {
        public final int statusCode;
        public final String responseBody;
        public final boolean isCached;
        
        public ProcessResult(int statusCode, String responseBody, boolean isCached) {
            this.statusCode = statusCode;
            this.responseBody = responseBody;
            this.isCached = isCached;
        }
    }
}