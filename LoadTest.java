import java.net.*;
import java.io.*;
import java.util.concurrent.*;

public class LoadTest {
    public static void main(String[] args) {
        System.out.println("=== Starting Load Test ===\n");
        
        int numRequests = 50;
        int numThreads = 10;
        
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numRequests);
        
        long startTime = System.currentTimeMillis();
        int[] successCount = {0};
        int[] failCount = {0};
        
        for (int i = 0; i < numRequests; i++) {
            final int requestId = i + 1;
            executor.submit(() -> {
                try {
                    URL url = new URL("http://localhost:8080/");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);
                    
                    int responseCode = conn.getResponseCode();
                    
                    synchronized(successCount) {
                        if (responseCode == 200) {
                            successCount[0]++;
                            System.out.println("Request " + requestId + ": SUCCESS (200 OK)");
                        } else {
                            failCount[0]++;
                            System.out.println("Request " + requestId + ": FAILED (" + responseCode + ")");
                        }
                    }
                    
                    conn.disconnect();
                } catch (Exception e) {
                    synchronized(failCount) {
                        failCount[0]++;
                        System.out.println("Request " + requestId + ": ERROR - " + e.getMessage());
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        
        executor.shutdown();
        
        System.out.println("\n==================================================");
        System.out.println("           LOAD TEST RESULTS");
        System.out.println("==================================================");
        System.out.println("Total Requests:     " + numRequests);
        System.out.println("Successful:         " + successCount[0]);
        System.out.println("Failed:             " + failCount[0]);
        System.out.println("Success Rate:       " + String.format("%.1f", successCount[0] * 100.0 / numRequests) + "%");
        System.out.println("Total Time:         " + totalTime + "ms");
        System.out.println("Average Time:       " + (totalTime / numRequests) + "ms per request");
        System.out.println("Throughput:         " + String.format("%.2f", numRequests * 1000.0 / totalTime) + " requests/sec");
        System.out.println("==================================================");
    }
}