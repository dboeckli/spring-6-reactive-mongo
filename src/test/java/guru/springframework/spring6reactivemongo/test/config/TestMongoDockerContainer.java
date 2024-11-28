package guru.springframework.spring6reactivemongo.test.config;

import lombok.extern.java.Log;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestConfiguration
@Log
public class TestMongoDockerContainer {

    @Bean
    @ServiceConnection
    MongoDBContainer mongoDBContainer() {
        MongoDBContainer container = new MongoDBContainer("mongo:8.0.3")
            .withExposedPorts(27017)
            .withReuse(false);

        container.start();
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            assertTrue(container.isRunning());
            log.info("Container started.");
        });
        // this will replace the default MongoDB URI, because we are using Docker and the port is changing every time the container is started
        log.info("### Set spring.data.mongodb.uri to: " + container.getConnectionString());
        log.info("### MongoDB Container started Run on port: " + container.getMappedPort(27017));
        System.setProperty("spring.data.mongodb.uri", container.getConnectionString());
        return container;
    }

    @Bean
    public DynamicPropertyRegistrar mongoDbProperties(MongoDBContainer mongoDBContainer) {
        return (properties) -> {
            String databaseName = RandomStringUtils.randomAlphabetic(10); 
            properties.add("spring.data.mongodb.database", () -> databaseName); // Replace with your desired database name
            properties.add("spring.data.mongodb.uri", () -> mongoDBContainer.getReplicaSetUrl(databaseName));
        };
    }



}
