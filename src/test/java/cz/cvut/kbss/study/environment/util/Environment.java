package cz.cvut.kbss.study.environment.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cvut.kbss.study.config.WebAppConfig;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.security.model.UserDetails;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import java.nio.charset.StandardCharsets;

public class Environment {

    private static User currentUser;

    private static ObjectMapper objectMapper;

    public static long MILLIS_PER_DAY = 24 * 3600 * 1000L;

    private Environment() {
        throw new AssertionError();
    }

    /**
     * Initializes security context with the specified user.
     *
     * @param user User to set as currently authenticated
     */
    public static void setCurrentUser(User user) {
        currentUser = user;
        final UserDetails userDetails = new UserDetails(user);
        SecurityContext context = new SecurityContextImpl();
        final UsernamePasswordAuthenticationToken token =
                UsernamePasswordAuthenticationToken.authenticated(userDetails, userDetails.getPassword(),
                                                                  userDetails.getAuthorities());
        token.setDetails(userDetails);
        context.setAuthentication(token);
        SecurityContextHolder.setContext(context);
    }

    /**
     * Returns currently logged user.
     *
     * @return User
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Gets a Jackson object mapper for mapping JSON to Java and vice versa.
     *
     * @return Object mapper
     */
    public static ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            objectMapper = WebAppConfig.createJsonObjectMapper();
        }
        return objectMapper;
    }

    public static HttpMessageConverter<?> createDefaultMessageConverter() {
        return new MappingJackson2HttpMessageConverter(getObjectMapper());
    }

    public static HttpMessageConverter<?> createStringEncodingMessageConverter() {
        return new StringHttpMessageConverter(StandardCharsets.UTF_8);
    }

    public static HttpMessageConverter<?> createResourceMessageConverter() {
        return new ResourceHttpMessageConverter();
    }

}
