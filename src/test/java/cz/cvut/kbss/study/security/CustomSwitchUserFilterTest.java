package cz.cvut.kbss.study.security;

import cz.cvut.kbss.study.environment.generator.Generator;
import cz.cvut.kbss.study.environment.util.Environment;
import cz.cvut.kbss.study.model.Role;
import cz.cvut.kbss.study.model.RoleGroup;
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
        final User source = Generator.generateUser(null, null);
        source.setRoleGroup(Generator.generateAdminRoleGroup());
        Environment.setCurrentUser(source);
        final User target = Generator.generateUser(null, null);
        target.setRoleGroup(Generator.generateRoleGroupWithRoles());
        when(userDetailsService.loadUserByUsername(target.getUsername())).thenReturn(new UserDetails(target));
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("username", target.getUsername());
        final Authentication result = sut.attemptSwitchUser(request);
        assertEquals(target.getUsername(), result.getName());
    }

    @Test
    void  attemptSwitchUsersWithEqualPrivilegesSwitchesCurrentUserToTarget() {
        RoleGroup roleGroup = Generator.generateAdminRoleGroup();
        final User source = Generator.generateUser(null, roleGroup);
        Environment.setCurrentUser(source);
        final User target = Generator.generateUser(null, roleGroup);
        when(userDetailsService.loadUserByUsername(target.getUsername())).thenReturn(new UserDetails(target));
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("username", target.getUsername());
        final Authentication result = sut.attemptSwitchUser(request);
        assertEquals(target.getUsername(), result.getName());
    }

    @Test
    void attemptSwitchUserThrowsBadRequestExceptionWhenTargetUserHasHigherPrivilege() {
        RoleGroup sourceRoleGroup = Generator.generateRoleGroupWithRoles(Role.readAllUsers);
        RoleGroup targetRoleGroup = Generator.generateRoleGroupWithRoles(Role.readAllUsers, Role.readAllOrganizations);
        final User source = Generator.generateUser(null, sourceRoleGroup);
        Environment.setCurrentUser(source);
        final User target = Generator.generateUser(null, targetRoleGroup);
        when(userDetailsService.loadUserByUsername(target.getUsername())).thenReturn(new UserDetails(target));
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("username", target.getUsername());
        assertThrows(BadRequestException.class, () -> sut.attemptSwitchUser(request));
    }
}