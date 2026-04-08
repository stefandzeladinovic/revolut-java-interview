import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@DisplayName("LoadBalancer Integration Tests")
public class LoadBalancerIntegrationTest {
    
    private LoadBalancer loadBalancer;
    private static final int NUM_THREADS = 5;
    private static final int REQUESTS_PER_THREAD = 20;
    
    @BeforeEach
    public void setUp() {
        loadBalancer = new LoadBalancer();
    }
    
    @Test
    @DisplayName("Should handle concurrent requests from multiple threads")
    public void testConcurrentRequestHandling() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS);
        CountDownLatch latch = new CountDownLatch(NUM_THREADS * REQUESTS_PER_THREAD);
        
        try {
            for (int thread = 0; thread < NUM_THREADS; thread++) {
                final int threadId = thread;
                executorService.submit(() -> {
                    for (int req = 0; req < REQUESTS_PER_THREAD; req++) {
                        try {
                            loadBalancer.distribute("thread-" + threadId + "-request-" + req);
                        } catch (Exception e) {
                            fail("Failed to distribute request: " + e.getMessage());
                        } finally {
                            latch.countDown();
                        }
                    }
                });
            }
            
            boolean completed = latch.await(30, TimeUnit.SECONDS);
            assertTrue(completed, "All requests should complete within 30 seconds");
        } finally {
            executorService.shutdown();
        }
    }
    
    @Test
    @DisplayName("Should maintain stability under sustained load")
    public void testSustainedLoadHandling() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        CountDownLatch latch = new CountDownLatch(300);
        
        try {
            for (int batch = 0; batch < 3; batch++) {
                final int batchId = batch;
                executorService.submit(() -> {
                    for (int i = 0; i < 100; i++) {
                        try {
                            loadBalancer.distribute("batch-" + batchId + "-item-" + i);
                        } catch (Exception e) {
                            fail("LoadBalancer failed under sustained load: " + e.getMessage());
                        } finally {
                            latch.countDown();
                        }
                    }
                });
            }
            
            boolean completed = latch.await(60, TimeUnit.SECONDS);
            assertTrue(completed, "LoadBalancer should handle sustained load");
        } finally {
            executorService.shutdown();
        }
    }
    
    @Test
    @DisplayName("Should handle rapid sequential requests")
    public void testRapidSequentialRequests() {
        int requestCount = 1000;
        long startTime = System.currentTimeMillis();
        
        assertDoesNotThrow(() -> {
            for (int i = 0; i < requestCount; i++) {
                loadBalancer.distribute("rapid-request-" + i);
            }
        }, "LoadBalancer should handle rapid sequential requests");
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        assertTrue(duration < 10000, "1000 requests should complete in less than 10 seconds");
    }
    
    @Test
    @DisplayName("Should recover from intermittent failures")
    public void testRecoveryFromFailures() {
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 5; i++) {
                try {
                    loadBalancer.distribute("recovery-test-" + i);
                } catch (Exception e) {
                    // Continue and try again
                    loadBalancer.distribute("recovery-retry-" + i);
                }
            }
        }, "LoadBalancer should recover and continue handling requests");
    }
}