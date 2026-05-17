package com.igirepay;

public class PaymentRequest {
    private final int amount;
    private final String currency;

    public PaymentRequest(int amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }

    public int getAmount() { return amount; }
    public String getCurrency() { return currency; }

    public String computeHash() {
        return amount + ":" + currency;
    }

    public static PaymentRequest fromJson(String json) throws Exception {
        int amount = 100;
        String currency = "GHS";
        
        if (json.contains("\"amount\"")) {
            String search = "\"amount\":";
            int start = json.indexOf(search);
            if (start != -1) {
                start += search.length();
                while (start < json.length() && !Character.isDigit(json.charAt(start))) start++;
                int end = start;
                while (end < json.length() && Character.isDigit(json.charAt(end))) end++;
                amount = Integer.parseInt(json.substring(start, end));
            }
        }
        
        if (json.contains("\"currency\"")) {
            String search = "\"currency\":";
            int start = json.indexOf(search);
            if (start != -1) {
                start += search.length();
                while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '"')) start++;
                int end = start;
                while (end < json.length() && json.charAt(end) != '"') end++;
                currency = json.substring(start, end);
            }
        }
        
        return new PaymentRequest(amount, currency);
    }
}