package guru.springframework.spring6reactivemongo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class AuthServerHealthIndicator implements ReactiveHealthIndicator {

    private final WebClient webClient;
    private final String authServerUrl;

    public AuthServerHealthIndicator(WebClient.Builder webClientBuilder,
                                     @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String authServerUrl) {
        this.webClient = webClientBuilder.build();
        this.authServerUrl = authServerUrl;
    }

    @Override
    public Mono<Health> health() {
        return checkAuthServerHealth().map(status -> status ? Health.up().build() : Health.down().build());
    }

    private Mono<Boolean> checkAuthServerHealth() {
        return webClient.get()
            .uri(authServerUrl + "/actuator/health")
            .retrieve()
            .bodyToMono(String.class)
            .map(response -> response.contains("\"status\":\"UP\""))
            .onErrorResume(e -> Mono.just(false));
    }
}
