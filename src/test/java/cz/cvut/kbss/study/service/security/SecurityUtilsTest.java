package cz.cvut.kbss.study.service.security;

import cz.cvut.kbss.study.environment.generator.Generator;
import cz.cvut.kbss.study.environment.util.Environment;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.PatientRecord;
import cz.cvut.kbss.study.model.Role;
import cz.cvut.kbss.study.model.RoleGroup;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.model.Vocabulary;
import cz.cvut.kbss.study.persistence.dao.PatientRecordDao;
import cz.cvut.kbss.study.persistence.dao.UserDao;
import cz.cvut.kbss.study.security.SecurityConstants;
import cz.cvut.kbss.study.security.model.UserDetails;
import cz.cvut.kbss.study.service.ConfigReader;
import cz.cvut.kbss.study.util.ConfigParam;
import cz.cvut.kbss.study.util.IdentificationUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

@ExtendWith(MockitoExtension.class)
public class SecurityUtilsTest {

    @Mock
    private UserDao userDao;

    @Mock
    private PatientRecordDao patientRecordDao;

    @Mock
    private ConfigReader config;

    @InjectMocks
    private SecurityUtils sut;

    private User user;

    public static final String USERNAME = "username";
    public static final String PASSWORD = "pass" + Generator.randomInt(0, 1000);

    @BeforeEach
    public void setUp() {
        Institution institution = Generator.generateInstitution();
        institution.setKey(IdentificationUtils.generateKey());
        RoleGroup roleGroup = new RoleGroup();
        this.user = Generator.getUser(USERNAME, PASSWORD, "John", "Johnie", "Johnie@gmail.com", institution, roleGroup);
        user.generateUri();
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void getCurrentUserReturnsCurrentlyLoggedInUser() {
        Environment.setCurrentUser(user);
        when(userDao.findByUsername(user.getUsername())).thenReturn(user);
        final User result = sut.getCurrentUser();
        assertEquals(user, result);
    }

    @Test
    void getCurrentUserRetrievesCurrentUserForOauthJwtAccessToken() {
        when(config.getConfig(ConfigParam.OIDC_ROLE_CLAIM)).thenReturn("roles");
        final Jwt token = Jwt.withTokenValue("abcdef12345")
                             .header("alg", "RS256")
                             .header("typ", "JWT")
                             .claim("roles", List.of(SecurityConstants.ROLE_USER))
                             .issuer("http://localhost:8080/termit")
                             .subject(USERNAME)
                             .claim("preferred_username", USERNAME)
                             .expiresAt(Instant.now().truncatedTo(ChronoUnit.SECONDS).plusSeconds(300))
                             .build();
        SecurityContext context = new SecurityContextImpl();
        context.setAuthentication(new JwtAuthenticationToken(token));
        SecurityContextHolder.setContext(context);
        when(userDao.findByUsername(user.getUsername())).thenReturn(user);

        final User result = sut.getCurrentUser();
        assertEquals(user, result);
    }

    @Test
    public void isMemberOfInstitutionReturnsTrueForUserFromSpecifiedInstitution() {
        Environment.setCurrentUser(user);
        when(userDao.findByUsername(user.getUsername())).thenReturn(user);
        assertTrue(sut.isMemberOfInstitution(user.getInstitution().getKey()));
    }

    @Test
    public void isMemberOfInstitutionReturnsFalseForDifferentInstitutionKey() {
        Environment.setCurrentUser(user);
        when(userDao.findByUsername(user.getUsername())).thenReturn(user);
        assertFalse(sut.isMemberOfInstitution("nonExistingInstitutionKey"));
    }

    @Test
    public void areFromSameInstitutionReturnsTrueForUserFromSameInstitution() {
        Environment.setCurrentUser(user);
        when(userDao.findByUsername(user.getUsername())).thenReturn(user);

        User userFromSameInstitution = Generator.generateUser(user.getInstitution());
        when(userDao.findByInstitution(user.getInstitution())).thenReturn(List.of(user, userFromSameInstitution));

        assertTrue(sut.areFromSameInstitution(userFromSameInstitution.getUsername()));
    }

    @Test
    public void areFromSameInstitutionReturnsFalseForUserFromDifferentInstitution() {
        Environment.setCurrentUser(user);
        when(userDao.findByUsername(user.getUsername())).thenReturn(user);

        Institution institutionAnother = Generator.generateInstitution();

        User userFromAnotherInstitution = Generator.generateUser(institutionAnother);
        when(userDao.findByInstitution(user.getInstitution())).thenReturn(List.of(user));

        assertFalse(sut.areFromSameInstitution(userFromAnotherInstitution.getUsername()));
    }

    @Test
    public void isRecordInUsersInstitutionReturnsTrueWhenRecordBelongsToCurrentUsersInstitution() {
        Environment.setCurrentUser(user);
        when(userDao.findByUsername(user.getUsername())).thenReturn(user);

        PatientRecord record = Generator.generatePatientRecord(user);
        record.setKey(IdentificationUtils.generateKey());
        when(patientRecordDao.findByKey(record.getKey())).thenReturn(record);

        assertTrue(sut.isRecordInUsersInstitution(record.getKey()));
    }

    @Test
    public void isRecordInUsersInstitutionReturnsFalseWhenRecordBelongsToInstitutionDifferentFromCurrentUsers() {
        Environment.setCurrentUser(user);
        when(userDao.findByUsername(user.getUsername())).thenReturn(user);
        Institution institutionAnother = Generator.generateInstitution();
        institutionAnother.setKey(IdentificationUtils.generateKey());
        User userFromAnotherInstitution = Generator.generateUser(institutionAnother);

        PatientRecord record = Generator.generatePatientRecord(userFromAnotherInstitution);
        record.setKey(IdentificationUtils.generateKey());
        when(patientRecordDao.findByKey(record.getKey())).thenReturn(record);

        assertFalse(sut.isRecordInUsersInstitution(record.getKey()));
    }

    @Test
    void getCurrentUserEnhancesRetrievedUserWithTypesCorrespondingToRolesSpecifiedInJwtClaim() {
        when(config.getConfig(ConfigParam.OIDC_ROLE_CLAIM)).thenReturn("roles");
        final Jwt token = Jwt.withTokenValue("abcdef12345")
                             .header("alg", "RS256")
                             .header("typ", "JWT")
                             .claim("roles", List.of(SecurityConstants.ROLE_ADMIN))
                             .issuer("http://localhost:8080/termit")
                             .subject(USERNAME)
                             .claim("preferred_username", USERNAME)
                             .expiresAt(Instant.now().truncatedTo(ChronoUnit.SECONDS).plusSeconds(300))
                             .build();
        SecurityContext context = new SecurityContextImpl();
        context.setAuthentication(new JwtAuthenticationToken(token));
        SecurityContextHolder.setContext(context);
        when(userDao.findByUsername(user.getUsername())).thenReturn(user);

        final User result = sut.getCurrentUser();
        assertThat(result.getRoleGroup().getRoles(), hasItem(Role.administrator));
    }

    @Test
    void getCurrentUserEnhancesRetrievedUserWithImpersonatorTypeWhenItHasSwitchAuthorityRole() {
        final UserDetails userDetails =
                new UserDetails(user, Set.of(new SimpleGrantedAuthority(SwitchUserFilter.ROLE_PREVIOUS_ADMINISTRATOR)));
        SecurityUtils.setCurrentUser(userDetails);
        when(userDao.findByUsername(user.getUsername())).thenReturn(user);
        final User result = sut.getCurrentUser();
        assertEquals(user, result);
        assertThat(result.getRoleGroup().getRoles(), hasItem(Role.impersonate));
    }

    @Test
    void getCurrentUserReturnsCopyOfInstanceRetrievedFromRepository() {
        final UserDetails userDetails =
                new UserDetails(user, Set.of(new SimpleGrantedAuthority(SwitchUserFilter.ROLE_PREVIOUS_ADMINISTRATOR)));
        SecurityUtils.setCurrentUser(userDetails);
        when(userDao.findByUsername(user.getUsername())).thenReturn(user);
        final User result = sut.getCurrentUser();

        assertNotSame(user, result);
        assertEquals(user, result);
    }
}
