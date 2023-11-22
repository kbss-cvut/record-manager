package cz.cvut.kbss.study.service.security;

import cz.cvut.kbss.study.exception.NotFoundException;
import cz.cvut.kbss.study.model.PatientRecord;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.model.Vocabulary;
import cz.cvut.kbss.study.persistence.dao.PatientRecordDao;
import cz.cvut.kbss.study.persistence.dao.UserDao;
import cz.cvut.kbss.study.security.model.Role;
import cz.cvut.kbss.study.security.model.UserDetails;
import cz.cvut.kbss.study.service.ConfigReader;
import cz.cvut.kbss.study.util.oidc.OidcGrantedAuthoritiesExtractor;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.server.authentication.SwitchUserWebFilter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SecurityUtils {

    private final UserDao userDao;

    private final PatientRecordDao patientRecordDao;

    private final ConfigReader config;

    public SecurityUtils(UserDao userDao, PatientRecordDao patientRecordDao, ConfigReader config) {
        this.userDao = userDao;
        this.patientRecordDao = patientRecordDao;
        this.config = config;
    }

    /**
     * Sets the current security context to the user represented by the provided user details.
     * <p>
     * Note that this method erases credentials from the provided user details for security reasons.
     * <p>
     * This method should be used only when internal authentication is used.
     *
     * @param userDetails User details
     */
    public static AbstractAuthenticationToken setCurrentUser(UserDetails userDetails) {
        final UsernamePasswordAuthenticationToken token =
                UsernamePasswordAuthenticationToken.authenticated(userDetails, userDetails.getPassword(),
                                                                  userDetails.getAuthorities());
        token.setDetails(userDetails);
        token.eraseCredentials();   // Do not pass credentials around

        final SecurityContext context = new SecurityContextImpl();
        context.setAuthentication(token);
        SecurityContextHolder.setContext(context);
        return token;
    }

    /**
     * Gets the currently authenticated user.
     *
     * @return Current user
     */
    public User getCurrentUser() {
        final SecurityContext context = SecurityContextHolder.getContext();
        assert context != null;
        final Object principal = context.getAuthentication().getPrincipal();
        if (principal instanceof Jwt) {
            return resolveAccountFromOAuthPrincipal((Jwt) principal);
        } else {
            final String username = context.getAuthentication().getName();
            final User user = userDao.findByUsername(username);
            if (context.getAuthentication().getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(
                    SwitchUserWebFilter.ROLE_PREVIOUS_ADMINISTRATOR))) {
                user.addType(Vocabulary.s_c_impersonator);
            }
            return user;
        }
    }

    private User resolveAccountFromOAuthPrincipal(Jwt principal) {
        final OidcUserInfo userInfo = new OidcUserInfo(principal.getClaims());
        final List<String> roles = new OidcGrantedAuthoritiesExtractor(config).extractRoles(principal);
        final User user = userDao.findByUsername(userInfo.getPreferredUsername());
        if (user == null) {
            throw new NotFoundException(
                    "User with username '" + userInfo.getPreferredUsername() + "' not found in repository.");
        }
        roles.stream().map(Role::forName).filter(Optional::isPresent).forEach(r -> user.addType(r.get().getType()));
        return user;
    }

    /**
     * Checks whether the current user is a member of a institution with the specified key.
     *
     * @param institutionKey Institution identifier
     * @return Membership status of the current user
     */
    public boolean isMemberOfInstitution(String institutionKey) {
        final User user = getCurrentUser();
        return user.getInstitution() != null && user.getInstitution().getKey().equals(institutionKey);
    }

    /**
     * Checks whether the current user is in same institution as the patient record was created.
     *
     * @param recordKey PatientRecord identifier
     * @return Membership status of the current user and patient record
     */
    public boolean isRecordInUsersInstitution(String recordKey) {
        final User user = getCurrentUser();
        final PatientRecord record = patientRecordDao.findByKey(recordKey);
        return user.getInstitution().getKey().equals(record.getInstitution().getKey());
    }

    /**
     * Checks whether the current user is in same institution as user we are asking for.
     *
     * @param username String identifier
     * @return Membership status of the current user and another user
     */
    public boolean areFromSameInstitution(String username) {
        final User user = getCurrentUser();
        final List<User> users = userDao.findByInstitution(user.getInstitution());
        return users.stream().anyMatch(o -> o.getUsername().equals(username));
    }
}
