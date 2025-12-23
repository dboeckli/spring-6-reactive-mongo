package guru.springframework.spring6reactivemongo.config;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.InvalidMongoDbApiUsageException;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.singletonList;

@Configuration
@Log
public class MongoClientConfig extends AbstractReactiveMongoConfiguration {

    @Value("${spring.mongodb.uri}")
    private String mongoUri;

    @Value("${spring.mongodb.username:}")
    private String username;

    @Value("${spring.mongodb.password:}")
    private String password;

    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create(mongoUri);
    }

    @Override
    protected String getDatabaseName() {
        ConnectionDetails connectionDetails = parseMongoUri();
        return connectionDetails.databaseName;
    }

    @Override
    protected void configureClientSettings(MongoClientSettings.Builder builder) {
        log.info("#### Mongo DB URI: " + mongoUri);
        ConnectionDetails connectionDetails = parseMongoUri();
        log.info("#### Mongo DB Host: " + connectionDetails.host);
        log.info("#### Mongo DB Port: " + connectionDetails.port);
        log.info("#### Mongo DB Database: " + connectionDetails.databaseName);
        builder
            .retryWrites(true)
            .applyToConnectionPoolSettings(poolSettings -> poolSettings
                .minSize(5)
                .maxSize(300)
                .maxConnectionIdleTime(0, TimeUnit.MILLISECONDS))
            .applyToSocketSettings(socketSettings -> socketSettings
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES))
            .applyToClusterSettings(settings -> settings.hosts((singletonList(
                new ServerAddress(connectionDetails.host, connectionDetails.port)
            ))));
        // this is only used when we start the application with the docker-compose profile. in that case we connect to the Docker MongoDB instance.
        if (username!= null &&!username.isEmpty()) {
            log.info("#### Mongo DB authentication enabled");
            log.info("#### Mongo DB username: " + username);
            log.info("#### Mongo DB password: " + password);
            // make sure to specify the authSource=admin query parameter in the connection string if you are using the root account for authentication.
            builder.credential(MongoCredential.createCredential(username, "admin", password.toCharArray()));
        } else {
            log.info("#### Mongo DB authentication disabled");
        }
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
            throw new InvalidMongoDbApiUsageException("Invalid MongoDB URI", e);
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
