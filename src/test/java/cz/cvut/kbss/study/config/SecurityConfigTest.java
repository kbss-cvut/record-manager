package cz.cvut.kbss.study.config;

import cz.cvut.kbss.study.service.ConfigReader;
import cz.cvut.kbss.study.util.ConfigParam;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SecurityConfigTest {

    private static final String UI_ORIGIN = "http://localhost:3000";
    private static final String UI_APP_URL = UI_ORIGIN + "/record-manager";

    private final MockEnvironment environment = new MockEnvironment();

    private final ConfigReader config = new ConfigReader(environment);

    @Test
    void createCorsConfigurationUsesAppContextToConfigureAllowedOrigins() {
        environment.setProperty(ConfigParam.APP_CONTEXT.toString(), UI_APP_URL);

        final CorsConfigurationSource result = SecurityConfig.createCorsConfiguration(config);
        assertNotNull(result.getCorsConfiguration(new MockHttpServletRequest()));
        assertThat(result.getCorsConfiguration(new MockHttpServletRequest()).getAllowedOrigins(), hasItem(UI_ORIGIN));
    }

    @Test
    void createCorsConfigurationCombinesAppContextWithConfiguredAllowedOrigins() {
        environment.setProperty(ConfigParam.APP_CONTEXT.toString(), UI_APP_URL);
        final String configuredOrigin = "https://example.org";
        environment.setProperty(ConfigParam.CORS_ALLOWED_ORIGINS.toString(), configuredOrigin);

        final CorsConfigurationSource result = SecurityConfig.createCorsConfiguration(config);
        assertNotNull(result.getCorsConfiguration(new MockHttpServletRequest()));
        assertThat(result.getCorsConfiguration(new MockHttpServletRequest()).getAllowedOrigins(),
                   hasItems(UI_ORIGIN, configuredOrigin));
    }

    @Test
    void createCorsConfigurationSupportsMultipleConfiguredAllowedOrigins() {
        final String originOne = "https://example.org";
        final String originTwo = "http://localhost:8081";
        final String originThree = "http://192.168.1.25";
        environment.setProperty(ConfigParam.CORS_ALLOWED_ORIGINS.toString(),
                                String.join(",", originOne, originTwo, originThree));

        final CorsConfigurationSource result = SecurityConfig.createCorsConfiguration(config);
        assertNotNull(result.getCorsConfiguration(new MockHttpServletRequest()));
        assertThat(result.getCorsConfiguration(new MockHttpServletRequest()).getAllowedOrigins(),
                   hasItems(originOne, originTwo, originThree));
    }
}