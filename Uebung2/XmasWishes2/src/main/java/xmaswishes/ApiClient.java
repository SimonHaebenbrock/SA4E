package xmaswishes;

import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ApiClient {

    private static final String BASE_URL = "http://localhost:8080/wishes";
    private static final RestTemplate restTemplate = new RestTemplate();

    public static void main(String[] args) throws InterruptedException {
        int numberOfRequests = 10000; // Anzahl der parallelen Anfragen
        int threadPoolSize = 50;    // Anzahl paralleler Threads
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger errorCount = new AtomicInteger();
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < numberOfRequests; i++) {
            final int requestId = i;
            executor.submit(() -> {
                try {
                    createWish(new Wish("Name" + requestId, "Description" + requestId, 1));
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.MINUTES);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        double requestsPerSecond = (numberOfRequests / (duration / 1000.0));

        System.out.println("======================================");
        System.out.println("API Performance Test Results");
        System.out.println("Total Requests: " + numberOfRequests);
        System.out.println("Successful Requests: " + successCount.get());
        System.out.println("Failed Requests: " + errorCount.get());
        System.out.println("Total Time (ms): " + duration);
        System.out.println("Requests per second: " + requestsPerSecond);
        System.out.println("======================================");
    }

    private static void createWish(Wish wish) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Wish> request = new HttpEntity<>(wish, headers);
        restTemplate.postForEntity(BASE_URL, request, Wish.class);
    }
}