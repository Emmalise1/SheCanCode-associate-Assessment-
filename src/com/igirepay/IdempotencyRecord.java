package com.igirepay;

public class IdempotencyRecord {
    private final int statusCode;
    private final String responseBody;
    private final String bodyHash;
    private final long createdAt;

    public IdempotencyRecord(int statusCode, String responseBody, String bodyHash) {
        this.statusCode = statusCode;
        this.responseBody = responseBody;
        this.bodyHash = bodyHash;
        this.createdAt = System.currentTimeMillis();
    }

    public int getStatusCode() { return statusCode; }
    public String getResponseBody() { return responseBody; }
    public String getBodyHash() { return bodyHash; }
    public long getCreatedAt() { return createdAt; }
}