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
import cz.cvut.kbss.study.util.ConfigParam;
import cz.cvut.kbss.study.util.oidc.OidcGrantedAuthoritiesExtractor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.server.authentication.SwitchUserWebFilter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SecurityUtils {

    private final UserDao userDao;

    private final PatientRecordDao patientRecordDao;

    private final ConfigReader config;
    private final RestTemplate restTemplate;

    public SecurityUtils(UserDao userDao, PatientRecordDao patientRecordDao, ConfigReader config, RestTemplate restTemplate) {
        this.userDao = userDao;
        this.patientRecordDao = patientRecordDao;
        this.config = config;
        this.restTemplate = restTemplate;
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
            final User user = userDao.findByUsername(username).copy();
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

    public String getCurrentToken(){
        // Retrieve token from security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Check if token is Jwt
        if(! (authentication.getPrincipal() instanceof Jwt))
            throw new IllegalArgumentException("Cannot process request, authentication principal type \"%s\" is not supported.".formatted(authentication.getPrincipal().getClass()));

        // This is only for Jwt type tokens
        return ((Jwt)authentication.getPrincipal()).getTokenValue();
    }

    public String getPublishToken(){
        // TODO - The exchanged token does not contain an IDP field, so the supplier won't know the original identity provider
        String accessToken = getCurrentToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange");
        map.add("subject_token", accessToken);
        map.add("subject_token_type", "urn:ietf:params:oauth:token-type:jwt");
        map.add("client_id", "record-manager-server"); // The client_id of the publishing service, i.e. the suppliers record-manager-server
        map.add("client_secret", config.getConfig(ConfigParam.PUBLISH_RECORDS_SERVICE_SECRET)); // the client secret for the client_id

        //TODO consider adding other parameters to the `map`
        //     as described in https://github.com/kbss-cvut/23ava-distribution/issues/147#issuecomment-2356420098

        try {
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(config.getConfig(ConfigParam.EXCHANGE_TOKEN_SERVICE_URL), request, Map.class);

            return response.getBody().get("access_token").toString();

        } catch (Exception e) {
            throw new SecurityException("Error exchanging token", e);
        }
    }
}
