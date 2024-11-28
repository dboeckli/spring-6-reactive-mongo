package guru.springframework.spring6reactivemongo.config;

import lombok.extern.java.Log;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.MongoDBContainer;

@Configuration
@Profile("docker-with-testcontainer")
@Log
public class MongoServerDockerContainer {

    @Bean
    @ServiceConnection
    MongoDBContainer mongoDBContainer() {
        MongoDBContainer container = new MongoDBContainer("mongo:8.0.3")
            .withExposedPorts(27017)
            .withReuse(false);
        
        container.start();
        log.info("### Set spring.data.mongodb.uri to: " + container.getConnectionString());
        log.info("### MongoDB Container started Run on port: " + container.getMappedPort(27017));
        // this will replace the default MongoDB URI, because we are using Docker and the port is changing every time the container is started
        System.setProperty("spring.data.mongodb.uri", container.getConnectionString());
        return container;
    }
}
