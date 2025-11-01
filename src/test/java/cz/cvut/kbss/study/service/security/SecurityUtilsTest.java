package cz.cvut.kbss.study.service.security;

import cz.cvut.kbss.study.environment.generator.Generator;
import cz.cvut.kbss.study.environment.util.Environment;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.Record;
import cz.cvut.kbss.study.model.Role;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.persistence.dao.RecordDao;
import cz.cvut.kbss.study.persistence.dao.UserDao;
import cz.cvut.kbss.study.security.SecurityConstants;
import cz.cvut.kbss.study.security.model.UserDetails;
import cz.cvut.kbss.study.service.BaseServiceTestRunner;
import cz.cvut.kbss.study.service.ConfigReader;
import cz.cvut.kbss.study.util.ConfigParam;
import cz.cvut.kbss.study.util.IdentificationUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;


public class SecurityUtilsTest extends BaseServiceTestRunner {

    @Mock
    private UserDao userDao;

    @Mock
    private RecordDao recordDao;

    @Mock
    private ConfigReader config;

    @InjectMocks
    private SecurityUtils sut;

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void getCurrentUserReturnsCurrentlyLoggedInUser() {
        Environment.setCurrentUser(admin);
        when(userDao.findByUsername(admin.getUsername())).thenReturn(admin);
        final User result = sut.getCurrentUser();
        assertEquals(admin, result);
    }

    @Test
    void getCurrentUserRetrievesCurrentUserForOauthJwtAccessToken() {
        when(config.getConfig(ConfigParam.OIDC_ROLE_CLAIM)).thenReturn("roles");
        final Jwt token = Jwt.withTokenValue("abcdef12345")
                             .header("alg", "RS256")
                             .header("typ", "JWT")
                             .issuer("http://localhost:8080/termit")
                             .subject(admin.getUsername())
                             .claim("preferred_username", admin.getUsername())
                             .expiresAt(Instant.now().truncatedTo(ChronoUnit.SECONDS).plusSeconds(300))
                             .build();
        SecurityContext context = new SecurityContextImpl();
        context.setAuthentication(new JwtAuthenticationToken(token));
        SecurityContextHolder.setContext(context);
        when(userDao.findByUsername(admin.getUsername())).thenReturn(admin);

        final User result = sut.getCurrentUser();
        assertEquals(admin, result);
    }

    @Test
    public void isMemberOfInstitutionReturnsTrueForUserFromSpecifiedInstitution() {
        Environment.setCurrentUser(admin);
        when(userDao.findByUsername(admin.getUsername())).thenReturn(admin);
        assertTrue(sut.isMemberOfInstitution(admin.getInstitution().getKey()));
    }

    @Test
    public void isMemberOfInstitutionReturnsFalseForDifferentInstitutionKey() {
        Environment.setCurrentUser(admin);
        when(userDao.findByUsername(admin.getUsername())).thenReturn(admin);
        assertFalse(sut.isMemberOfInstitution("nonExistingInstitutionKey"));
    }

    @Test
    public void areFromSameInstitutionReturnsTrueForUserFromSameInstitution() {
        Environment.setCurrentUser(admin);
        when(userDao.findByUsername(admin.getUsername())).thenReturn(admin);

        User userFromSameInstitution = Generator.generateUser(admin.getInstitution(), this.adminRoleGroup);
        when(userDao.findByInstitution(admin.getInstitution())).thenReturn(List.of(admin, userFromSameInstitution));

        assertTrue(sut.areFromSameInstitution(userFromSameInstitution.getUsername()));
    }

    @Test
    public void areFromSameInstitutionReturnsFalseForUserFromDifferentInstitution() {
        Environment.setCurrentUser(admin);
        when(userDao.findByUsername(admin.getUsername())).thenReturn(admin);

        Institution institutionAnother = Generator.generateInstitution();

        User userFromAnotherInstitution = Generator.generateUser(institutionAnother, this.adminRoleGroup);
        when(userDao.findByInstitution(admin.getInstitution())).thenReturn(List.of(admin));

        assertFalse(sut.areFromSameInstitution(userFromAnotherInstitution.getUsername()));
    }

    @Test
    public void isRecordInUsersInstitutionReturnsTrueWhenRecordBelongsToCurrentUsersInstitution() {
        Environment.setCurrentUser(admin);
        when(userDao.findByUsername(admin.getUsername())).thenReturn(admin);

        Record record = Generator.generateRecord(admin);
        record.setKey(IdentificationUtils.generateKey());
        when(recordDao.findByKey(record.getKey())).thenReturn(record);

        assertTrue(sut.isRecordInUsersInstitution(record.getKey()));
    }

    @Test
    public void isRecordInUsersInstitutionReturnsFalseWhenRecordBelongsToInstitutionDifferentFromCurrentUsers() {
        Environment.setCurrentUser(admin);
        when(userDao.findByUsername(admin.getUsername())).thenReturn(admin);
        Institution institutionAnother = Generator.generateInstitution();
        institutionAnother.setKey(IdentificationUtils.generateKey());
        User userFromAnotherInstitution = Generator.generateUser(institutionAnother, this.adminRoleGroup);

        Record record = Generator.generateRecord(userFromAnotherInstitution);
        record.setKey(IdentificationUtils.generateKey());
        when(recordDao.findByKey(record.getKey())).thenReturn(record);

        assertFalse(sut.isRecordInUsersInstitution(record.getKey()));
    }

    @Test
    void getCurrentUserEnhancesRetrievedUserWithTypesCorrespondingToRolesSpecifiedInJwtClaim() {
        when(config.getConfig(ConfigParam.OIDC_ROLE_CLAIM)).thenReturn("roles");
        final Jwt token = Jwt.withTokenValue("abcdef12345")
                             .header("alg", "RS256")
                             .header("typ", "JWT")
                             .claim("roles", List.of(SecurityConstants.writeAllUsers))
                             .issuer("http://localhost:8080/termit")
                             .subject(admin.getUsername())
                             .claim("preferred_username", admin.getUsername())
                             .expiresAt(Instant.now().truncatedTo(ChronoUnit.SECONDS).plusSeconds(300))
                             .build();
        SecurityContext context = new SecurityContextImpl();
        context.setAuthentication(new JwtAuthenticationToken(token));
        SecurityContextHolder.setContext(context);
        when(userDao.findByUsername(admin.getUsername())).thenReturn(admin);

        final User result = sut.getCurrentUser();
        assertThat(result.getRoleGroup().getRoles(), hasItem(Role.writeAllUsers));
    }

    @Test
    void getCurrentUserEnhancesRetrievedUserWithImpersonatorTypeWhenItHasSwitchAuthorityRole() {
        final UserDetails userDetails =
                new UserDetails(admin, Set.of(new SimpleGrantedAuthority(SwitchUserFilter.ROLE_PREVIOUS_ADMINISTRATOR)));
        SecurityUtils.setCurrentUser(userDetails);
        when(userDao.findByUsername(admin.getUsername())).thenReturn(admin);
        final User result = sut.getCurrentUser();
        assertEquals(admin, result);
        assertTrue(result.isImpersonated());
    }

    @Test
    void getCurrentUserReturnsCopyOfInstanceRetrievedFromRepository() {
        final UserDetails userDetails =
                new UserDetails(admin, Set.of(new SimpleGrantedAuthority(SwitchUserFilter.ROLE_PREVIOUS_ADMINISTRATOR)));
        SecurityUtils.setCurrentUser(userDetails);
        when(userDao.findByUsername(admin.getUsername())).thenReturn(admin);
        final User result = sut.getCurrentUser();

        assertNotSame(admin, result);
        assertEquals(admin, result);
    }
}
