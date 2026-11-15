package guru.springframework.spring6reactivemongo.config;

import io.opentelemetry.api.OpenTelemetry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class OpenTelemetryAppenderInitializerTest {

    @Test
    @SuppressWarnings("unchecked")
    void installsAppenderWhenOpenTelemetryIsAvailable() {
        ObjectProvider<OpenTelemetry> openTelemetryProvider = mock(ObjectProvider.class);
        given(openTelemetryProvider.getIfAvailable()).willReturn(OpenTelemetry.noop());

        OpenTelemetryAppenderInitializer initializer = new OpenTelemetryAppenderInitializer(openTelemetryProvider);

        assertThatCode(initializer::afterPropertiesSet).doesNotThrowAnyException();
    }

    @Test
    @SuppressWarnings("unchecked")
    void skipsInstallWhenOpenTelemetryIsNotAvailable() {
        ObjectProvider<OpenTelemetry> openTelemetryProvider = mock(ObjectProvider.class);
        given(openTelemetryProvider.getIfAvailable()).willReturn(null);

        OpenTelemetryAppenderInitializer initializer = new OpenTelemetryAppenderInitializer(openTelemetryProvider);

        assertThatCode(initializer::afterPropertiesSet).doesNotThrowAnyException();
    }

}
