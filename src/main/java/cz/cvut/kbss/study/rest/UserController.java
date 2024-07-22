package cz.cvut.kbss.study.rest;

import cz.cvut.kbss.study.exception.NotFoundException;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.rest.exception.BadRequestException;
import cz.cvut.kbss.study.rest.util.RestUtils;
import cz.cvut.kbss.study.security.SecurityConstants;
import cz.cvut.kbss.study.service.InstitutionService;
import cz.cvut.kbss.study.service.UserService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * User management API.
 *
 * Enabled when internal security is used.
 */
@ConditionalOnProperty(prefix = "security", name = "provider", havingValue = "internal", matchIfMissing = true)
@RestController
@RequestMapping("/users")
public class UserController extends BaseController {

    private final UserService userService;

    private final InstitutionService institutionService;

    public UserController(UserService userService, InstitutionService institutionService) {
        this.userService = userService;
        this.institutionService = institutionService;
    }

    @PreAuthorize("hasRole('" + SecurityConstants.ROLE_ADMIN + "') or #username == authentication.name or " +
            "hasRole('" + SecurityConstants.ROLE_USER + "') and @securityUtils.areFromSameInstitution(#username)")
    @GetMapping(value = "/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    public User getByUsername(@PathVariable("username") String username) {
        final User user = userService.findByUsername(username);
        if (user == null) {
            throw NotFoundException.create("User", username);
        }
        return user;
    }

    @PreAuthorize("hasRole('" + SecurityConstants.ROLE_USER + "')")
    @GetMapping(value = "/current", produces = MediaType.APPLICATION_JSON_VALUE)
    public User getCurrent() {
        return userService.getCurrentUser();
    }

    @PreAuthorize("hasRole('" + SecurityConstants.ROLE_ADMIN + "')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> create(@RequestBody User user) {
        userService.persist(user);
        if (LOG.isTraceEnabled()) {
            LOG.trace("User {} successfully registered.", user);
        }
        final HttpHeaders headers = RestUtils
                .createLocationHeaderFromCurrentUri("/{username}", user.getUsername());
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }


    @PreAuthorize(
            "hasRole('" + SecurityConstants.ROLE_ADMIN + "') " +
                    "or hasRole('" + SecurityConstants.ROLE_USER + "') and @securityUtils.isMemberOfInstitution(#institutionKey)")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<User> getUsers(@RequestParam(value = "institution", required = false) String institutionKey) {
        return institutionKey != null ? getByInstitution(institutionKey) : userService.findAll();
    }

    private List<User> getByInstitution(String institutionKey) {
        assert institutionKey != null;
        final Institution institution = institutionService.findByKey(institutionKey);
        return userService.findByInstitution(institution);
    }

    @PreAuthorize("hasRole('" + SecurityConstants.ROLE_ADMIN + "')")
    @DeleteMapping(value = "/{username}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeUser(@PathVariable("username") String username) {
        final User toRemove = getByUsername(username);
        userService.remove(toRemove);
        if (LOG.isTraceEnabled()) {
            LOG.trace("User {} successfully removed.", toRemove);
        }
    }

    @PreAuthorize("hasRole('" + SecurityConstants.ROLE_ADMIN + "') or #username == authentication.name")
    @PutMapping(value = "/{username}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateUser(@PathVariable("username") String username, @RequestBody User user,
                           @RequestParam(value = "email", defaultValue = "true") boolean sendEmail) {
        if (!username.equals(user.getUsername())) {
            throw new BadRequestException("The passed user's username is different from the specified one.");
        }
        final User original = getByUsername(username);
        assert original != null;
        userService.update(user, sendEmail, "profileUpdate");
        if (LOG.isTraceEnabled()) {
            LOG.trace("User {} successfully updated.", user);
        }
    }

    @PreAuthorize("hasRole('" + SecurityConstants.ROLE_ADMIN + "') or #username == authentication.name")
    @PutMapping(value = "/{username}/password-change", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updatePassword(@PathVariable("username") String username, @RequestBody Map<String, String> password,
                               @RequestParam(value = "email", defaultValue = "true") boolean sendEmail) {
        final User original = getByUsername(username);
        assert original != null;
        userService.changePassword(original, password.get("newPassword"), password.get("currentPassword"), sendEmail);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Password for username {} successfully changed.", username);
        }
    }

    private User getByEmail(String email) {
        assert email != null;
        return userService.findByEmail(email);
    }

    @PostMapping(value = "/password-reset", consumes = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@RequestBody String emailAddress) {
        final User original = getByEmail(emailAddress);
        if(original != null) {
            userService.resetPassword(original, emailAddress);
            if (LOG.isTraceEnabled()) {
                LOG.trace("New password successfully sent to {}.", emailAddress);
            }
        }
    }

    @PreAuthorize("hasRole('" + SecurityConstants.ROLE_ADMIN + "')")
    @GetMapping(value = "/generate-username/{usernamePrefix}", produces = MediaType.TEXT_PLAIN_VALUE)
    public String generateUsername(@PathVariable(value = "usernamePrefix") String usernamePrefix) {
        return userService.generateUsername(usernamePrefix);
    }

    @PostMapping(value = "/validate-token", consumes = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void validateToken(@RequestBody String token) {
        User user = getByToken(token);
        if (user == null) {
            throw new BadRequestException("Token does not exists.");
        }
    }

    @PutMapping(value = "/password-change-token", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePasswordByToken(@RequestBody Map<String, String> data) {
        final User original = getByToken(data.get("token"));
        assert original != null;
        userService.changePasswordByToken(original, data.get("password"));
        if (LOG.isTraceEnabled()) {
            LOG.trace("Password for username {} successfully changed by token.", original.getUsername());
        }
    }

    @PreAuthorize("hasRole('" + SecurityConstants.ROLE_ADMIN + "')")
    @PutMapping(value = "/send-invitation/{username}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sendInvitation(@PathVariable(value = "username") String username) {
        final User original = getByUsername(username);
        assert original != null;
        if (original.getIsInvited()) {
            throw new BadRequestException("Cannot invite already invited user.");
        }
        userService.sendInvitation(original);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Invitation has been sent to user {}.", original.getUsername());
        }
    }

    @PreAuthorize("hasRole('" + SecurityConstants.ROLE_ADMIN + "')")
    @PostMapping(value = "/send-invitation/delete", consumes = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteInvitationOption(@RequestBody String username) {
        final User original = getByUsername(username);
        assert original != null;
        original.setIsInvited(true);
        userService.update(original);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Invitation option has been canceled for user {}.", original.getUsername());
        }
    }

    private User getByToken(String token) {
        assert token != null;
        return userService.findByToken(token);
    }
}
