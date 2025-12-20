package guru.springframework.spring6reactivemongo.test.config;

import lombok.extern.java.Log;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.MongoDBContainer;

@TestConfiguration
@Log
public class TestMongoDockerContainer {

    @Bean
    @ServiceConnection
    MongoDBContainer mongoDBContainer() {
        return new MongoDBContainer("mongo:8.2.1")
    }

    @Bean
    public DynamicPropertyRegistrar mongoDbProperties(MongoDBContainer mongoDBContainer) {
        return (properties) -> {
            String databaseName = "sfg";
            properties.add("spring.mongodb.database", () -> databaseName);
            // Nutze getReplicaSetUrl für die korrekte URI (beinhaltet bereits Host und dynamischen Port)
            properties.add("spring.mongodb.uri", () -> mongoDBContainer.getReplicaSetUrl(databaseName));

            // Falls explizite Host/Port Properties benötigt werden:
            properties.add("spring.mongodb.host", mongoDBContainer::getHost);
            properties.add("spring.mongodb.port", () -> mongoDBContainer.getMappedPort(27017));
        };
    }
}
