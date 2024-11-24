package guru.springframework.spring6reactivemongo.test.helper;

import lombok.extern.java.Log;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Log
public class AbstractBaseMongoTestUtil {

    @Container
    public static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:8.0.3")
        .withExposedPorts(27017)
        .withReuse(false);

    @BeforeAll
    static void setup() {
        log.info("### Starting container on port: " + mongoDBContainer.getMappedPort(mongoDBContainer.getExposedPorts().getFirst()));
        mongoDBContainer.start();
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            assertTrue(mongoDBContainer.isRunning());
            log.info("Container started.");
        });
        log.info("### ConnectionString: " + mongoDBContainer.getConnectionString());
    }

    @AfterAll
    static void tearDown() {
        log.info("Stopping container");
        mongoDBContainer.close();
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            assertFalse(mongoDBContainer.isRunning());
            log.info("Container stopped.");
        });
    }
    
    
}
