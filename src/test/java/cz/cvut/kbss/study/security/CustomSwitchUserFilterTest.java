package cz.cvut.kbss.study.security;

import cz.cvut.kbss.study.environment.generator.Generator;
import cz.cvut.kbss.study.environment.util.Environment;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.model.Vocabulary;
import cz.cvut.kbss.study.rest.exception.BadRequestException;
import cz.cvut.kbss.study.security.model.UserDetails;
import cz.cvut.kbss.study.service.security.UserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomSwitchUserFilterTest {

    @Mock
    private UserDetailsService userDetailsService;

    private CustomSwitchUserFilter sut;

    @BeforeEach
    void setUp() {
        this.sut = new CustomSwitchUserFilter();
        sut.setUserDetailsService(userDetailsService);
    }

    @Test
    void attemptSwitchUserSwitchesCurrentUserToTarget() {
        final User source = Generator.generateUser(null);
        source.addType(Vocabulary.s_c_administrator);
        Environment.setCurrentUser(source);
        final User target = Generator.generateUser(null);
        when(userDetailsService.loadUserByUsername(target.getUsername())).thenReturn(new UserDetails(target));
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("username", target.getUsername());
        final Authentication result = sut.attemptSwitchUser(request);
        assertEquals(target.getUsername(), result.getName());
    }

    @Test
    void attemptSwitchUserThrowsBadRequestExceptionWhenTargetUserIsAdmin() {
        final User source = Generator.generateUser(null);
        source.addType(Vocabulary.s_c_administrator);
        Environment.setCurrentUser(source);
        final User target = Generator.generateUser(null);
        target.addType(Vocabulary.s_c_administrator);
        when(userDetailsService.loadUserByUsername(target.getUsername())).thenReturn(new UserDetails(target));
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("username", target.getUsername());
        assertThrows(BadRequestException.class, () -> sut.attemptSwitchUser(request));
    }
}