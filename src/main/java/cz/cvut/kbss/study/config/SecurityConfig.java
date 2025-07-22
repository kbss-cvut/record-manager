package cz.cvut.kbss.study.config;

import cz.cvut.kbss.study.exception.RecordManagerException;
import cz.cvut.kbss.study.model.Role;
import cz.cvut.kbss.study.security.CsrfHeaderFilter;
import cz.cvut.kbss.study.security.CustomSwitchUserFilter;
import cz.cvut.kbss.study.security.SecurityConstants;
import cz.cvut.kbss.study.service.ConfigReader;
import cz.cvut.kbss.study.service.security.UserDetailsService;
import cz.cvut.kbss.study.util.ConfigParam;
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
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
    public SecurityFilterChain filterChain(HttpSecurity http, ConfigReader config,
                                           UserDetailsService userDetailsService) throws Exception {
        LOG.debug("Using internal security mechanisms.");
        final AuthenticationManager authManager = buildAuthenticationManager(http);
        http.authorizeHttpRequests(
                    (auth) -> auth.requestMatchers(
                                    SecurityConstants.SECURITY_CHECK_URI,
                                    "/login",
                                    "/register"
                            ).permitAll()
                            .anyRequest().authenticated())
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
            .sessionManagement((auth) -> auth.maximumSessions(1))
            .addFilterAfter(switchUserFilter(userDetailsService), AuthorizationFilter.class)
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
        configureAllowedOrigins(corsConfiguration, configReader);
        corsConfiguration.addExposedHeader(HttpHeaders.AUTHORIZATION);
        corsConfiguration.addExposedHeader(HttpHeaders.LOCATION);
        corsConfiguration.addExposedHeader(HttpHeaders.CONTENT_DISPOSITION);
        corsConfiguration.addExposedHeader(HttpHeaders.LINK);

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    private static void configureAllowedOrigins(CorsConfiguration corsConfig, ConfigReader config) {
        final Optional<String> appUrlOrigin = getApplicationUrlOrigin(config);
        final List<String> allowedOrigins = new ArrayList<>();
        appUrlOrigin.ifPresent(allowedOrigins::add);
        final String allowedOriginsConfig = config.getConfig(ConfigParam.CORS_ALLOWED_ORIGINS);
        Arrays.stream(allowedOriginsConfig.split(",")).filter(s -> !s.isBlank()).forEach(allowedOrigins::add);
        if (!allowedOrigins.isEmpty()) {
            corsConfig.setAllowedOrigins(allowedOrigins);
            corsConfig.setAllowCredentials(true);
        } else {
            corsConfig.setAllowedOrigins(null);
        }
        LOG.info(
            "Using response header Access-Control-Allow-Origin with value {}.",
            corsConfig.getAllowedOrigins()
        );
    }

    private static Optional<String> getApplicationUrlOrigin(ConfigReader configReader) {
        String appUrlConfig = configReader.getConfig(ConfigParam.APP_CONTEXT);

        if (appUrlConfig.isBlank()) {
            return Optional.empty();
        }
        try {
            final URL appUrl = new URL(appUrlConfig);
            return Optional.of(appUrl.getProtocol() + "://" + appUrl.getAuthority());
        } catch (MalformedURLException e) {
            throw new RecordManagerException("Invalid configuration parameter " + ConfigParam.APP_CONTEXT + ".", e);
        }
    }

    @Bean
    public SwitchUserFilter switchUserFilter(UserDetailsService userDetailsService) {
        final SwitchUserFilter filter = new CustomSwitchUserFilter();
        filter.setUserDetailsService(userDetailsService);
        filter.setUsernameParameter("username");
        filter.setSwitchUserUrl("/rest/users/impersonate");
        filter.setExitUserUrl("/rest/users/impersonate/logout");
        filter.setSuccessHandler(authenticationSuccessHandler);
        return filter;
    }
}
