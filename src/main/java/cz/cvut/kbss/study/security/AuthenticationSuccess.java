package cz.cvut.kbss.study.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cvut.kbss.study.exception.RecordManagerException;
import cz.cvut.kbss.study.security.model.LoginStatus;
import cz.cvut.kbss.study.service.ConfigReader;
import cz.cvut.kbss.study.util.ConfigParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 * Writes basic login/logout information into the response.
 */
@Service
public class AuthenticationSuccess implements AuthenticationSuccessHandler, LogoutSuccessHandler {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationSuccess.class);

    private final ObjectMapper mapper;

    private final ConfigReader config;

    public AuthenticationSuccess(ObjectMapper mapper, ConfigReader config) {
        this.mapper = mapper;
        this.config = config;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                        Authentication authentication) throws IOException {
        final String username = getUsername(authentication);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Successfully authenticated user {}", username);
        }
        addSameSiteCookieAttribute(httpServletResponse);
        final LoginStatus loginStatus = new LoginStatus(true, authentication.isAuthenticated(), username, null);
        mapper.writeValue(httpServletResponse.getOutputStream(), loginStatus);
    }

    private String getUsername(Authentication authentication) {
        if (authentication == null) {
            return "";
        }
        return authentication.getName();
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                Authentication authentication) throws IOException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Successfully logged out user {}", getUsername(authentication));
        }
        final LoginStatus loginStatus = new LoginStatus(false, true, null, null);
        mapper.writeValue(httpServletResponse.getOutputStream(), loginStatus);
    }

    enum SameSiteValue {
        STRICT("Strict"),
        LAX("Lax"),
        NONE("None");

        private final String name;

        SameSiteValue(String name) {
            this.name = name;
        }

        public static Optional<SameSiteValue> getValue(String value) {
            return Arrays.stream(SameSiteValue.values())
                    .filter(v -> v.name.equals(value))
                    .findFirst();
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private void addSameSiteCookieAttribute(HttpServletResponse response) {
        String configValue = config.getConfig(ConfigParam.SECURITY_SAME_SITE, "");

        if (configValue.equals("")) {
            LOG.debug("SameSite attribute for set-cookie header not configured.");
            return;
        }

        SameSiteValue sameSiteValue = SameSiteValue.getValue(configValue)
                .orElseThrow(
                        () -> new RecordManagerException(
                                "Could not recognize " + ConfigParam.SECURITY_SAME_SITE + " parameter value '"
                                        + configValue + "', as it is not one of the values "
                                        + Arrays.toString(SameSiteValue.values()) + "."
                        )
                );

        StringBuilder headerValues = new StringBuilder();
        if (sameSiteValue.equals(SameSiteValue.NONE)) {
            headerValues.append("Secure; ");
        }
        headerValues.append("SameSite=").append(sameSiteValue);

        Collection<String> headers = response.getHeaders(HttpHeaders.SET_COOKIE);
        boolean firstHeader = true;
        // there can be multiple Set-Cookie attributes
        for (String header : headers) {
            if (firstHeader) {
                response.setHeader(HttpHeaders.SET_COOKIE, String.format("%s; %s", header, headerValues));
                firstHeader = false;
                continue;
            }
            response.addHeader(HttpHeaders.SET_COOKIE, String.format("%s; %s", header, headerValues));
        }
    }




}
