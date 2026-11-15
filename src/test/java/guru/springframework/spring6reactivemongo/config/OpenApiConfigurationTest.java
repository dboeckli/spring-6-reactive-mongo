package guru.springframework.spring6reactivemongo.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.boot.info.BuildProperties;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class OpenApiConfigurationTest {

    @Test
    void updatesInfoAndOAuthUrlsAndIgnoresOtherSecuritySchemes() {
        BuildProperties buildProperties = mock(BuildProperties.class);
        given(buildProperties.getName()).willReturn("test-app");
        given(buildProperties.getVersion()).willReturn("1.0.0");
        OpenApiConfiguration configuration = new OpenApiConfiguration(buildProperties);
        ReflectionTestUtils.setField(configuration, "authorizationUrl", "https://auth.example.com/authorize");
        ReflectionTestUtils.setField(configuration, "tokenUrl", "https://auth.example.com/token");
        ReflectionTestUtils.setField(configuration, "refreshUrl", "https://auth.example.com/refresh");

        SecurityScheme oauthScheme = new SecurityScheme().type(SecurityScheme.Type.OAUTH2)
            .flows(new OAuthFlows().clientCredentials(new OAuthFlow()));
        SecurityScheme httpScheme = new SecurityScheme().type(SecurityScheme.Type.HTTP);
        SecurityScheme oauthSchemeWithoutClientCredentials = new SecurityScheme().type(SecurityScheme.Type.OAUTH2)
            .flows(new OAuthFlows());
        OpenAPI openApi = new OpenAPI().info(new Info())
            .components(new Components().addSecuritySchemes("oauth", oauthScheme)
                .addSecuritySchemes("http", httpScheme)
                .addSecuritySchemes("oauthWithoutClientCredentials", oauthSchemeWithoutClientCredentials));

        OpenApiCustomizer customizer = configuration.customerGlobalHeaderOpenApiCustomizer();
        customizer.customise(openApi);

        assertThat(openApi.getInfo().getTitle()).isEqualTo("test-app");
        assertThat(openApi.getInfo().getVersion()).isEqualTo("1.0.0");
        assertThat(oauthScheme.getFlows().getClientCredentials().getAuthorizationUrl())
            .isEqualTo("https://auth.example.com/authorize");
        assertThat(oauthScheme.getFlows().getClientCredentials().getTokenUrl())
            .isEqualTo("https://auth.example.com/token");
        assertThat(oauthScheme.getFlows().getClientCredentials().getRefreshUrl())
            .isEqualTo("https://auth.example.com/refresh");
        assertThat(httpScheme.getFlows()).isNull();
        assertThat(oauthSchemeWithoutClientCredentials.getFlows().getClientCredentials()).isNull();
    }

}
