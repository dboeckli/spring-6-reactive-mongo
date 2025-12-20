package guru.springframework.spring6reactivemongo.test.config;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import lombok.extern.java.Log;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;

@TestConfiguration
@Log
public class AuthServerDockerContainer {

    private static final String DOCKER_REPO = "domboeckli";

    private static final String AUTH_SERVER_VERSION = "0.0.5-SNAPSHOT";

    static final int AUTH_SERVER_PORT = 9000;

    static final Network sharedNetwork = Network.newNetwork();

    @Container
    static GenericContainer<?> authServer = new GenericContainer<>(DOCKER_REPO + "/spring-6-auth-server:" + AUTH_SERVER_VERSION)
        .withNetworkAliases("auth-server")
        .withNetwork(sharedNetwork)
        .withEnv("SERVER_PORT", String.valueOf(AUTH_SERVER_PORT))
        // Da wir den Port fixieren, passt dieser Issuer immer
        .withEnv("SPRING_SECURITY_OAUTH2_AUTHORIZATION_SERVER_ISSUER", "http://localhost:" + AUTH_SERVER_PORT)
        .withExposedPorts(AUTH_SERVER_PORT)
        // Hier erzwingen wir das Mapping von Host 9000 auf Container 9000
        .withCreateContainerCmdModifier(cmd -> cmd.getHostConfig().withPortBindings(
            new PortBinding(Ports.Binding.bindPort(AUTH_SERVER_PORT), new ExposedPort(AUTH_SERVER_PORT))))
        .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("auth-server")))
        .waitingFor(Wait.forHttp("/actuator/health/readiness")
            .forStatusCode(200)
            .forResponsePredicate(response ->
                response.contains("\"status\":\"UP\"")
            )
        );

    @Bean
    public DynamicPropertyRegistrar authServerProperties() {
        if (!authServer.isRunning()) {
            authServer.start();
        }

        return (properties) -> {
            String issuerUri = "http://localhost:" + AUTH_SERVER_PORT;
            log.info("### Setting Resource Server Issuer URI (Fixed Port): " + issuerUri);
            properties.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> issuerUri);
        };
    }

}
