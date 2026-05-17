package com.igirepay;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        IdempotencyService service = new IdempotencyService();
        
        server.createContext("/process-payment", new PaymentHandler(service));
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        
        System.out.println("Idempotency Gateway running on http://localhost:8080");
        System.out.println("POST /process-payment");
        System.out.println("Required header: Idempotency-Key");
        
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(3600000);
                    service.cleanupExpired();
                } catch (InterruptedException e) {
                    break;
                }
            }
        }).start();
    }
}