package cz.cvut.kbss.study.config;

import cz.cvut.kbss.study.exception.RecordManagerException;
import cz.cvut.kbss.study.security.CsrfHeaderFilter;
import cz.cvut.kbss.study.security.SecurityConstants;
import cz.cvut.kbss.study.service.ConfigReader;
import cz.cvut.kbss.study.util.ConfigParam;
import java.net.MalformedURLException;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;
import java.util.List;

@ConditionalOnProperty(prefix = "security", name = "provider", havingValue = "internal", matchIfMissing = true)
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityConfig.class);

    private static final String[] COOKIES_TO_DESTROY = {
            SecurityConstants.SESSION_COOKIE_NAME,
            SecurityConstants.REMEMBER_ME_COOKIE_NAME,
            SecurityConstants.CSRF_COOKIE_NAME
    };

    private final AuthenticationFailureHandler authenticationFailureHandler;

    private final AuthenticationSuccessHandler authenticationSuccessHandler;

    private final LogoutSuccessHandler logoutSuccessHandler;

    private final AuthenticationProvider ontologyAuthenticationProvider;

    public SecurityConfig(AuthenticationFailureHandler authenticationFailureHandler,
                          AuthenticationSuccessHandler authenticationSuccessHandler,
                          LogoutSuccessHandler logoutSuccessHandler,
                          AuthenticationProvider ontologyAuthenticationProvider) {
        this.authenticationFailureHandler = authenticationFailureHandler;
        this.authenticationSuccessHandler = authenticationSuccessHandler;
        this.logoutSuccessHandler = logoutSuccessHandler;
        this.ontologyAuthenticationProvider = ontologyAuthenticationProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, ConfigReader config) throws Exception {
        LOG.debug("Using internal security mechanisms.");
        final AuthenticationManager authManager = buildAuthenticationManager(http);
        http.authorizeHttpRequests((auth) -> auth.anyRequest().permitAll())
            .cors((auth) -> auth.configurationSource(corsConfigurationSource(config)))
            .csrf(AbstractHttpConfigurer::disable)
            .addFilterAfter(new CsrfHeaderFilter(), CsrfFilter.class)
            .exceptionHandling(ehc -> ehc.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .formLogin((form) -> form.loginProcessingUrl(SecurityConstants.SECURITY_CHECK_URI)
                                     .successHandler(authenticationSuccessHandler)
                                     .failureHandler(authenticationFailureHandler))
            .logout((auth) -> auth.logoutUrl(SecurityConstants.LOGOUT_URI)
                                  .logoutSuccessHandler(logoutSuccessHandler)
                                  .invalidateHttpSession(true).deleteCookies(COOKIES_TO_DESTROY))
            .authenticationManager(authManager);
        return http.build();
    }

    private AuthenticationManager buildAuthenticationManager(HttpSecurity http) throws Exception {
        final AuthenticationManagerBuilder ab = http.getSharedObject(AuthenticationManagerBuilder.class);
        ab.authenticationProvider(ontologyAuthenticationProvider);
        return ab.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(ConfigReader config) {
        return createCorsConfiguration(config);
    }

    static CorsConfigurationSource createCorsConfiguration(ConfigReader configReader) {
        final CorsConfiguration corsConfiguration = new CorsConfiguration().applyPermitDefaultValues();
        corsConfiguration.setAllowedMethods(Collections.singletonList("*"));
        URL appUrl = getApplicationContext(configReader);
        if (appUrl != null) {
            corsConfiguration.setAllowedOrigins(List.of(parseOrigin(appUrl)));
            corsConfiguration.setAllowCredentials(true);
        }
        corsConfiguration.addExposedHeader(HttpHeaders.AUTHORIZATION);
        corsConfiguration.addExposedHeader(HttpHeaders.LOCATION);
        corsConfiguration.addExposedHeader(HttpHeaders.CONTENT_DISPOSITION);

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    private static URL getApplicationContext(ConfigReader configReader) {
        String appUrl = configReader.getConfig(ConfigParam.APP_CONTEXT);

        if (appUrl.isBlank()) {
            return null;
        }
        try {
            return new URL(appUrl);
        } catch (MalformedURLException e) {
            throw new RecordManagerException(
                "Invalid configuration parameter " + ConfigParam.APP_CONTEXT + ".",
                e);
        }
    }

    private static String parseOrigin(URL url) {
        return url.getProtocol() + "://" + url.getHost()
            + (url.getPort() != -1 ? ":" + url.getPort() : "");
    }
}
