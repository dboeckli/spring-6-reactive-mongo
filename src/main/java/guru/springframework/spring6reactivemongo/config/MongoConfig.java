package guru.springframework.spring6reactivemongo.config;

import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;

import java.net.URI;
import java.net.URISyntaxException;

import static java.util.Collections.singletonList;

@Configuration
@Log
public class MongoConfig extends AbstractReactiveMongoConfiguration {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create(mongoUri);
    }

    @Override
    protected String getDatabaseName() {
        ConnectionDetails connectionDetails = parseMongoUri();
        log.info("#### Mongo DB Database: " + connectionDetails.databaseName);
        return connectionDetails.databaseName;
    }

    //@Override
    protected void configureClientSettings(MongoClientSettings.Builder builder) {
        ConnectionDetails connectionDetails = parseMongoUri();
        log.info("#### Mongo DB Host: " + connectionDetails.host);
        log.info("#### Mongo DB Port: " + connectionDetails.port);
        log.info("#### Mongo DB Database: " + connectionDetails.databaseName);
        
        builder.applyToClusterSettings(settings -> {
            settings.hosts((singletonList(
                new ServerAddress(connectionDetails.host, connectionDetails.port)
            )));
        });
        /*
        builder.credential(MongoCredential.createCredential("root",
                "admin", "example".toCharArray()))
            .applyToClusterSettings(settings -> {
                settings.hosts((singletonList(
                    new ServerAddress("127.0.0.1", 32769)
                )));
            });*/
    }

    private ConnectionDetails parseMongoUri() {
        try {
            URI uri = new URI(mongoUri);
            String host = uri.getHost();
            int port = uri.getPort();
            String path = uri.getPath();
            String databaseName = path != null && path.length() > 1 ? path.substring(1) : "sfg"; // Default to "sfg" if no database is specified
            return new ConnectionDetails(host, port, databaseName);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid MongoDB URI", e);
        }
    }

    private static class ConnectionDetails {
        String host;
        int port;
        String databaseName;

        ConnectionDetails(String host, int port, String databaseName) {
            this.host = host;
            this.port = port;
            this.databaseName = databaseName;
        }
    } 
    
}
