package guru.springframework.spring6reactivemongo.test.config;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import lombok.extern.java.Log;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.springframework.test.util.TestSocketUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

@TestConfiguration
@Log
public class AuthServerDockerContainer {

    private static final String DOCKER_REPO = "domboeckli";

    private static final String AUTH_SERVER_VERSION = "0.0.5-SNAPSHOT";

    static final int AUTH_SERVER_CONTAINER_PORT = 9000;
    static final int AUTH_SERVER_HOST_PORT = findPortInRange(49152, 65535);

    static final Network sharedNetwork = Network.newNetwork();

    private static int findPortInRange(int min, int max) {
        for (int i = 0; i < 50; i++) { // 50 Versuche
            int port = ThreadLocalRandom.current().nextInt(min, max + 1);
            try (ServerSocket ignored = new ServerSocket(port)) {
                return port;
            } catch (IOException e) {
                // Port belegt, nächster Versuch
            }
        }
        throw new IllegalStateException("Kein freier Port im Bereich " + min + "-" + max + " gefunden.");
    }

    @Container
    static GenericContainer<?> authServer = new GenericContainer<>(DOCKER_REPO + "/spring-6-auth-server:" + AUTH_SERVER_VERSION)
        .withNetworkAliases("auth-server")
        .withNetwork(sharedNetwork)
        // Der Container selbst kann intern auf 9000 laufen...
        .withEnv("SERVER_PORT", String.valueOf(AUTH_SERVER_CONTAINER_PORT))
        // ...aber wir sagen ihm, dass er von außen über den Host-Port erreichbar ist!
        .withEnv("SPRING_SECURITY_OAUTH2_AUTHORIZATION_SERVER_ISSUER", "http://localhost:" + AUTH_SERVER_HOST_PORT)
        // Jetzt binden wir den fixen Host-Zufallsport an den Container-Port
        .withCreateContainerCmdModifier(cmd -> cmd.getHostConfig().withPortBindings(
            new PortBinding(Ports.Binding.bindPort(AUTH_SERVER_HOST_PORT), new ExposedPort(AUTH_SERVER_CONTAINER_PORT))))
        .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("auth-server")))
        .waitingFor(Wait.forHttp("/actuator/health/readiness")
            .forStatusCode(200)
            .forResponsePredicate(response -> {
                log.info("### Readiness Response: " + response);
                return response.contains("\"status\":\"UP\"");
            })

        ).waitingFor(Wait.forHttp("/.well-known/openid-configuration")
            .forStatusCode(200)
            .forResponsePredicate(response -> {
                log.info("### OIDC Config Response: " + response);
                return !response.isEmpty();
            })
        )
        .withStartupTimeout(Duration.ofMinutes(3)
        );


    @Bean
    public DynamicPropertyRegistrar authServerProperties() {
        if (!authServer.isRunning()) {
            authServer.start();
        }

        return (properties) -> {
            String issuerUri = "http://localhost:" + AUTH_SERVER_HOST_PORT;
            log.info("### Setting Resource Server Issuer URI (Static Random Port): " + issuerUri);
            properties.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> issuerUri);
        };
    }

}
