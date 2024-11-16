package guru.springframework.spring6reactivemongo;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;

@TestConfiguration
@Primary
public class TestMongoConfiguration extends AbstractReactiveMongoConfiguration {

    @Primary
    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create("mongodb://localhost:32769");
    }

    @Override
    protected String getDatabaseName() {
        return "gaga";
    }
}
