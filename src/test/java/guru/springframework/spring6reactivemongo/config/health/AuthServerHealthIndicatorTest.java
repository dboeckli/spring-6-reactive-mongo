package guru.springframework.spring6reactivemongo.config.health;

import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;

class AuthServerHealthIndicatorTest {

    private static final String AUTH_SERVER_URL = "http://localhost:9000";

    @Test
    void healthIsUpWhenAuthServerReportsUp() {
        ExchangeFunction exchangeFunction = request -> {
            assertThat(request.url().toString()).isEqualTo(AUTH_SERVER_URL + "/actuator/health");
            return Mono.just(responseWithBody("{\"status\":\"UP\"}"));
        };

        Health health = newIndicator(exchangeFunction).health().block();

        assertThat(health).isNotNull();
        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }

    @Test
    void healthIsDownWhenAuthServerReportsDown() {
        ExchangeFunction exchangeFunction = request -> Mono.just(responseWithBody("{\"status\":\"DOWN\"}"));

        Health health = newIndicator(exchangeFunction).health().block();

        assertThat(health).isNotNull();
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }

    @Test
    void healthIsDownWhenAuthServerIsNotReachable() {
        ExchangeFunction exchangeFunction = request -> Mono.error(new IllegalStateException("connection refused"));

        Health health = newIndicator(exchangeFunction).health().block();

        assertThat(health).isNotNull();
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }

    private static AuthServerHealthIndicator newIndicator(ExchangeFunction exchangeFunction) {
        WebClient.Builder webClientBuilder = WebClient.builder().exchangeFunction(exchangeFunction);
        return new AuthServerHealthIndicator(webClientBuilder, AUTH_SERVER_URL);
    }

    private static ClientResponse responseWithBody(String body) {
        return ClientResponse.create(HttpStatus.OK)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(body)
            .build();
    }

}
