package guru.springframework.spring6reactivemongo.web.fn;

import lombok.experimental.UtilityClass;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@UtilityClass
public class AuthTokenUtil {

    public static String fetchClientCredentialsAccessToken(ObjectMapper objectMapper,
                                                           String issuerBaseUrl,
                                                           String clientId,
                                                           String clientSecret,
                                                           String scope) {

        WebTestClient authClient = WebTestClient.bindToServer()
            .baseUrl(issuerBaseUrl)
            .build();

        // 1) OIDC discovery -> token_endpoint
        byte[] discoveryBody = authClient.get()
            .uri("/.well-known/openid-configuration")
            .exchange()
            .expectStatus().is2xxSuccessful()
            .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            .expectBody()
            .returnResult()
            .getResponseBodyContent();

        if (discoveryBody == null) {
            throw new IllegalStateException("OIDC discovery response body is null");
        }

        Map<String, Object> cfg = objectMapper.readValue(discoveryBody, Map.class);
        String tokenEndpoint = String.valueOf(cfg.get("token_endpoint"));
        if (tokenEndpoint == null || tokenEndpoint.isBlank() || "null".equals(tokenEndpoint)) {
            throw new IllegalStateException("token_endpoint missing in discovery: " + cfg);
        }

        // 2) Token holen (client_credentials)
        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "client_credentials");
        form.add("scope", scope);

        byte[] tokenBody = authClient.post()
            .uri(tokenEndpoint) // absolute URL ist ok
            .headers(h -> h.setBasicAuth(clientId, clientSecret))
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue(form)
            .exchange()
            .expectStatus().is2xxSuccessful()
            .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            .expectBody()
            .returnResult()
            .getResponseBodyContent();

        if (tokenBody == null) {
            throw new IllegalStateException("Token response body is null");
        }

        JsonNode json = objectMapper.readTree(tokenBody);
        JsonNode token = json.get("access_token");
        if (token == null || token.asString().isBlank()) {
            throw new IllegalStateException("No access_token in response: " + json);
        }
        return token.asString();
    }

}
