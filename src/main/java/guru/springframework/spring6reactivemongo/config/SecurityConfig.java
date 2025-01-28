package guru.springframework.spring6reactivemongo.config;

import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    @Order(1)
    public SecurityWebFilterChain actuatorSecurityFilterChain(ServerHttpSecurity http) {
        http.securityMatcher(EndpointRequest.toAnyEndpoint())
            .authorizeExchange(authorize -> authorize.anyExchange().permitAll());
        return http.build();
    }

    @Bean
    @Order(2)
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(authorizeExchange -> authorizeExchange
                .pathMatchers(
                    "/favicon.ico",
                    "/v3/api-docs",
                    "/v3/api-docs.yaml",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html").permitAll() // allow Swagger/Openapi endpoints to be accessed without authentication
                //.pathMatchers("/api/v3/customer/**", "/api/v3/beer/**").hasRole("ADMIN") // FOR FUTURE USE ONLY
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oAuth2ResourceServerSpec -> oAuth2ResourceServerSpec.jwt(Customizer.withDefaults()));
        return http.build();
    }
    
}
