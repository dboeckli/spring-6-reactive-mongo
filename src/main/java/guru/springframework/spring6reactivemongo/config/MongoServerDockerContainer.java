package guru.springframework.spring6reactivemongo.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.testcontainers.containers.MongoDBContainer;

@Configuration
@Profile("docker")
@Log
@RequiredArgsConstructor
public class MongoServerDockerContainer implements BeanPostProcessor {

    @Bean
    @ServiceConnection
    MongoDBContainer mongoDBContainer() {
        MongoDBContainer container = new MongoDBContainer("mongo:8.0.3")
            .withExposedPorts(27017)
            .withReuse(false);
        
        container.start();
        log.info("### Set spring.data.mongodb.uri to: " + container.getConnectionString());
        log.info("### MongoDB Container started Run on port: " + container.getMappedPort(27017));
        System.setProperty("spring.data.mongodb.uri", container.getConnectionString());
        return container;
    }   
}
