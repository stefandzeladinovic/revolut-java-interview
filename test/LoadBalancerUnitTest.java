import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

@DisplayName("LoadBalancer Unit Tests")
public class LoadBalancerUnitTest {
    
    private LoadBalancer loadBalancer;
    
    @BeforeEach
    public void setUp() {
        loadBalancer = new LoadBalancer();
    }
    
    @Test
    @DisplayName("Should initialize LoadBalancer successfully")
    public void testLoadBalancerInitialization() {
        assertNotNull(loadBalancer, "LoadBalancer should be initialized");
    }
    
    @Test
    @DisplayName("Should handle single request")
    public void testHandleSingleRequest() {
        assertDoesNotThrow(() -> {
            loadBalancer.distribute("request1");
        }, "LoadBalancer should handle single request without throwing exception");
    }
    
    @Test
    @DisplayName("Should handle multiple requests sequentially")
    public void testHandleMultipleRequests() {
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 10; i++) {
                loadBalancer.distribute("request" + i);
            }
        }, "LoadBalancer should handle multiple requests");
    }
    
    @Test
    @DisplayName("Should not accept null requests")
    public void testNullRequestHandling() {
        assertThrows(IllegalArgumentException.class, () -> {
            loadBalancer.distribute(null);
        }, "LoadBalancer should throw IllegalArgumentException for null requests");
    }
    
    @Test
    @DisplayName("Should not accept empty string requests")
    public void testEmptyStringRequestHandling() {
        assertThrows(IllegalArgumentException.class, () -> {
            loadBalancer.distribute("");
        }, "LoadBalancer should throw IllegalArgumentException for empty requests");
    }
}