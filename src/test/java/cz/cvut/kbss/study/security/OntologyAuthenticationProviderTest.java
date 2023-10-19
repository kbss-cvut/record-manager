package cz.cvut.kbss.study.security;

import cz.cvut.kbss.study.environment.config.TestSecurityConfig;
import cz.cvut.kbss.study.service.BaseServiceTestRunner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ContextConfiguration(classes = {TestSecurityConfig.class})
public class OntologyAuthenticationProviderTest extends BaseServiceTestRunner {

    @Autowired
    @Qualifier("ontologyAuthenticationProvider")
    private AuthenticationProvider provider;

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        SecurityContextHolder.setContext(new SecurityContextImpl());
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.setContext(new SecurityContextImpl());
    }

    @Test
    public void successfulAuthenticationSetsSecurityContext() {
        final Authentication auth =
                new UsernamePasswordAuthenticationToken(BaseServiceTestRunner.USERNAME, BaseServiceTestRunner.PASSWORD);
        final SecurityContext context = SecurityContextHolder.getContext();
        assertNull(context.getAuthentication());
        final Authentication result = provider.authenticate(auth);
        assertTrue(result.isAuthenticated());
        assertNotNull(SecurityContextHolder.getContext());
        assertEquals(BaseServiceTestRunner.USERNAME, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @Test
    public void authenticateThrowsUserNotFoundExceptionForUnknownUsername() {
        final Authentication auth =
                new UsernamePasswordAuthenticationToken("unknownUsername", BaseServiceTestRunner.PASSWORD);
        assertThrows(UsernameNotFoundException.class, () -> provider.authenticate(auth));
        final SecurityContext context = SecurityContextHolder.getContext();
        assertNull(context.getAuthentication());
    }

    @Test
    public void authenticateThrowsBadCredentialsForInvalidPassword() {
        final Authentication auth = new UsernamePasswordAuthenticationToken(USERNAME, "unknownPassword");
        assertThrows(BadCredentialsException.class, () -> provider.authenticate(auth));
        final SecurityContext context = SecurityContextHolder.getContext();
        assertNull(context.getAuthentication());
    }
}



