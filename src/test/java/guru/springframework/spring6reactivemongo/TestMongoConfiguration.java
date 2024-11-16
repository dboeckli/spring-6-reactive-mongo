package guru.springframework.spring6reactivemongo;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestMongoConfiguration {

    @Primary
    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create("mongodb://localhost:32769");
    }
    
}
