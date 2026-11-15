package guru.springframework.spring6reactivemongo.config;

import com.mongodb.MongoClientSettings;
import io.micrometer.observation.ObservationRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.InvalidMongoDbApiUsageException;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MongoClientConfigTest {

    @Test
    void usesDatabaseNameFromUri() {
        MongoClientConfig config = newConfig("mongodb://localhost:27017/testdb", "", "");

        assertThat(config.getDatabaseName()).isEqualTo("testdb");
    }

    @Test
    void defaultsDatabaseNameWhenUriHasNoPath() {
        MongoClientConfig config = newConfig("mongodb://localhost:27017", "", "");

        assertThat(config.getDatabaseName()).isEqualTo("sfg");
    }

    @Test
    void defaultsDatabaseNameWhenUriPathIsNotAvailable() {
        MongoClientConfig config = newConfig("mongodb:localhost", "", "");

        assertThat(config.getDatabaseName()).isEqualTo("sfg");
    }

    @Test
    void throwsWhenMongoUriIsInvalid() {
        MongoClientConfig config = newConfig("mongodb://invalid host:27017/testdb", "", "");

        assertThatThrownBy(config::getDatabaseName).isInstanceOf(InvalidMongoDbApiUsageException.class)
            .hasMessage("Invalid MongoDB URI");
    }

    @Test
    void configuresCredentialWhenCredentialsAreSet() {
        MongoClientConfig config = newConfig("mongodb://localhost:27017/testdb", "user", "secret");
        MongoClientSettings.Builder builder = MongoClientSettings.builder();

        config.configureClientSettings(builder);

        assertThat(builder.build().getCredential()).isNotNull();
    }

    @Test
    void configuresNoCredentialWhenUsernameIsEmpty() {
        MongoClientConfig config = newConfig("mongodb://localhost:27017/testdb", "", "secret");
        MongoClientSettings.Builder builder = MongoClientSettings.builder();

        config.configureClientSettings(builder);

        assertThat(builder.build().getCredential()).isNull();
    }

    private static MongoClientConfig newConfig(String mongoUri, String username, String password) {
        MongoClientConfig config = new MongoClientConfig(ObservationRegistry.NOOP);
        ReflectionTestUtils.setField(config, "mongoUri", mongoUri);
        ReflectionTestUtils.setField(config, "username", username);
        ReflectionTestUtils.setField(config, "password", password);
        return config;
    }

}
