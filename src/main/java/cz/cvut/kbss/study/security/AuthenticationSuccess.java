package cz.cvut.kbss.study.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cvut.kbss.study.security.model.LoginStatus;
import cz.cvut.kbss.study.security.model.UserDetails;
import cz.cvut.kbss.study.service.ConfigReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Writes basic login/logout information into the response.
 */
@Service
public class AuthenticationSuccess implements AuthenticationSuccessHandler, LogoutSuccessHandler {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationSuccess.class);

    private final ObjectMapper mapper;

    private final ConfigReader config;

    public AuthenticationSuccess(ObjectMapper mapper,
                                 ConfigReader config) {
        this.mapper = mapper;
        this.config = config;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                        Authentication authentication) throws IOException, ServletException {
        final String username = getUsername(authentication);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Successfully authenticated user {}", username);
        }
        final LoginStatus loginStatus = new LoginStatus(true, authentication.isAuthenticated(), username, null);
        mapper.writeValue(httpServletResponse.getOutputStream(), loginStatus);
    }

    private String getUsername(Authentication authentication) {
        if (authentication == null) {
            return "";
        }
        return ((UserDetails) authentication.getPrincipal()).getUsername();
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                Authentication authentication) throws IOException, ServletException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Successfully logged out user {}", getUsername(authentication));
        }
        final LoginStatus loginStatus = new LoginStatus(false, true, null, null);
        mapper.writeValue(httpServletResponse.getOutputStream(), loginStatus);
    }
}
