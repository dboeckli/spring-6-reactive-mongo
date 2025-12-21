package guru.springframework.spring6reactivemongo.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.core.ConditionTimeoutException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.security.autoconfigure.actuate.web.reactive.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;

import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private static final List<String> ALLOWED_HEADERS = List.of("*");
    private static final List<String> ALLOWED_METHODS = List.of("POST", "GET", "PUT", "OPTIONS", "DELETE", "PATCH");

    private final AllowedOriginConfig allowedOriginConfig;

    private final Environment environment;

    @PostConstruct
    public void init() {
        log.info("### Allowed origins: {}", allowedOriginConfig);
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    SecurityWebFilterChain springSecurityActuator(ServerHttpSecurity http, CorsConfigurationSource corsConfigurationSource) {
        return http
            .securityMatcher(EndpointRequest.toAnyEndpoint())
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
            .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
            .authorizeExchange(exchange -> exchange.anyExchange().permitAll())
            .build();
    }

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    SecurityWebFilterChain springSecurity(ServerHttpSecurity http, CorsConfigurationSource corsConfigurationSource) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .authorizeExchange(exchange -> exchange
                .pathMatchers(
                    "/favicon.ico",
                    "/v3/api-docs",
                    "/v3/api-docs.yaml",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oAuth2 -> oAuth2.jwt(Customizer.withDefaults()))
            .build();
    }

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        String issuer = environment.getProperty("spring.security.oauth2.resourceserver.jwt.issuer-uri");
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalStateException("Property spring.security.oauth2.resourceserver.jwt.issuer-uri must be set");
        }
        log.info("### Wating for Issuer ready: {}", issuer);

        WebClient webClient = WebClient.builder().baseUrl(issuer).build();

        try {
            await()
                .atMost(120, TimeUnit.SECONDS)
                .pollInterval(3, TimeUnit.SECONDS)
                .ignoreExceptions()
                .alias("Auth-Server-Readiness-Check")
                .until(() -> {
                    return webClient.get()
                        .uri("/.well-known/openid-configuration")
                        .exchangeToMono(response -> {
                            if (response.statusCode().is2xxSuccessful()) {
                                return Mono.just(true);
                            } else {
                                log.warn("### Readiness-Check fehlgeschlagen. Status: {}", response.statusCode());
                                return Mono.just(false);
                            }
                        })
                        .onErrorResume(ex -> {
                            // Hier fangen wir das "Connection refused" ab und loggen es
                            log.error("### Readiness-Check fehlgeschlagen: {}", ex.getMessage());
                            return Mono.just(false);
                        })
                        .block(Duration.ofSeconds(2));
                });
        } catch (ConditionTimeoutException e) {
            log.error("Initialisierung fehlgeschlagen: Der Auth-Server hat nicht rechtzeitig geantwortet.");
            throw e; // Den Test/Start explizit abbrechen
        }

        log.info("Authentication Server ist bereit. Initialisiere ReactiveJwtDecoder.");
        
        return ReactiveJwtDecoders.fromIssuerLocation(issuer);
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(allowedOriginConfig.getAllowedOrigins());
        configuration.setAllowedMethods(ALLOWED_METHODS);
        configuration.setAllowedHeaders(ALLOWED_HEADERS);
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Component
    @ConfigurationProperties(prefix = "security.cors")
    @Data
    public static class AllowedOriginConfig {
        private List<String> allowedOrigins;
    }
}
