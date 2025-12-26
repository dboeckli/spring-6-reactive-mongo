package guru.springframework.spring6reactivemongo.web.fn;

import guru.springframework.spring6reactivemongo.test.config.AuthServerDockerContainer;
import guru.springframework.spring6reactivemongo.test.config.MongoExtension;
import guru.springframework.spring6reactivemongo.test.config.TestMongoDockerContainer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureWebTestClient
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
@Import({TestMongoDockerContainer.class, AuthServerDockerContainer.class})
@ExtendWith(MongoExtension.class)
class CustomerHandlerWithAuthServerIT {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ObjectMapper objectMapper;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    String issuerUri;

    @Test
    void testListCustomers() {
        String accessToken = AuthTokenUtil.fetchClientCredentialsAccessToken(
            objectMapper,
            issuerUri,
            "messaging-client",
            "secret",
            "message.read message.write"
        );

        webTestClient
            .get().uri(CustomerRouterConfig.CUSTOMER_PATH)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().valueEquals("Content-type", "application/json")
            .expectBody().jsonPath("$.size()").value(size -> assertEquals(3, size));
    }
}
