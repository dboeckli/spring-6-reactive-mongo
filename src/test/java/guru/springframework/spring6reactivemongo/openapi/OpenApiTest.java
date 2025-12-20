package guru.springframework.spring6reactivemongo.openapi;

import guru.springframework.spring6reactivemongo.test.config.AuthServerDockerContainer;
import guru.springframework.spring6reactivemongo.test.config.TestMongoDockerContainer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Import({TestMongoDockerContainer.class, AuthServerDockerContainer.class})
@AutoConfigureWebTestClient
@Slf4j
class OpenApiTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    BuildProperties buildProperties;

    @Test
    void openapiGetJsonTest() {
        EntityExchangeResult<byte[]> result = webTestClient.get().uri("/v3/api-docs")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .returnResult();

        String jsonResponse = new String(Objects.requireNonNull(result.getResponseBody()));
        JsonNode jsonNode = objectMapper.readTree(jsonResponse);
        log.info("Response:\n{}", objectMapper.writeValueAsString(jsonNode));

        assertThat(jsonNode.has("info")).isTrue();
        JsonNode infoNode = jsonNode.get("info");
        assertThat(infoNode.has("title")).isTrue();
        assertThat(infoNode.get("title").asString()).isEqualTo(buildProperties.getName());
    }

}
