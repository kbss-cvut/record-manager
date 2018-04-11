package cz.cvut.kbss.study.rest;

import cz.cvut.kbss.study.exception.NotFoundException;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.rest.exception.BadRequestException;
import cz.cvut.kbss.study.rest.util.RestUtils;
import cz.cvut.kbss.study.security.SecurityConstants;
import cz.cvut.kbss.study.service.UserService;
import org.eclipse.rdf4j.http.protocol.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    @Autowired
    private InstitutionController institutionController;

    @PreAuthorize("hasRole('" + SecurityConstants.ROLE_ADMIN + "') or #username == authentication.name or " +
            "hasRole('" + SecurityConstants.ROLE_USER + "') and @securityUtils.areFromSameInstitution(#username)")
    @RequestMapping(method = RequestMethod.GET, value = "/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    public User getByUsername(@PathVariable("username") String username) {
        final User user = userService.findByUsername(username);
        if (user == null) {
            throw NotFoundException.create("User", username);
        }
        return user;
    }

    @PreAuthorize("hasRole('" + SecurityConstants.ROLE_USER + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/current", produces = MediaType.APPLICATION_JSON_VALUE)
    public User getCurrent(Principal principal) {
        final String username = principal.getName();
        return getByUsername(username);
    }

    @PreAuthorize("hasRole('" + SecurityConstants.ROLE_ADMIN + "')")
    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
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
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<User> getUsers(@RequestParam(value = "institution", required = false) String institutionKey) {
        final List<User> users = institutionKey != null ? getByInstitution(institutionKey) : userService.findAll();
        return users;
    }

    private List<User> getByInstitution(String institutionKey) {
        assert institutionKey != null;
        final Institution institution = institutionController.findByKey(institutionKey);
        return userService.findByInstitution(institution);
    }

    @PreAuthorize("hasRole('" + SecurityConstants.ROLE_ADMIN + "') or #username == authentication.name")
    @RequestMapping(value = "/{username}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateUser(@PathVariable("username") String username, @RequestBody User user) {
        if (!username.equals(user.getUsername())) {
            throw new BadRequestException("The passed user's username is different from the specified one.");
        }
        final User original = getByUsername(username);
        assert original != null;
        userService.update(user);
        if (LOG.isTraceEnabled()) {
            LOG.trace("User {} successfully updated.", user);
        }
    }

    @PreAuthorize("hasRole('" + SecurityConstants.ROLE_ADMIN + "')")
    @RequestMapping(value = "/{username}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeUser(@PathVariable("username") String username) {
        final User toRemove = getByUsername(username);
        userService.remove(toRemove);
        if (LOG.isTraceEnabled()) {
            LOG.trace("User {} successfully removed.", toRemove);
        }
    }

    @PreAuthorize("hasRole('" + SecurityConstants.ROLE_ADMIN + "') or #username == authentication.name")
    @RequestMapping(value = "/{username}/password-change", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updatePassword(@PathVariable("username") String username, @RequestBody Map<String, String> password) {
        final User original = getByUsername(username);
        assert original != null;
        userService.changePassword(original, password.get("newPassword"), password.get("currentPassword"));
        if (LOG.isTraceEnabled()) {
            LOG.trace("User's password successfully changed.", username);
        }
    }

    private User getByEmail(String email) {
        assert email != null;
        return userService.findByEmail(email);
    }

    @RequestMapping(value = "/password-reset", method = RequestMethod.POST, consumes = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@RequestBody String emailAddress) {
        final User original = getByEmail(emailAddress);
        if(original != null) {
            userService.resetPassword(original, emailAddress);
            if (LOG.isTraceEnabled()) {
                LOG.trace("New password successfully sent.", emailAddress);
            }
        }
    }

    @PreAuthorize("hasRole('" + SecurityConstants.ROLE_ADMIN + "')")
    @RequestMapping(value = "/generate-username/{usernamePrefix}", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public String generateUsername(@PathVariable(value = "usernamePrefix") String usernamePrefix) {
        return userService.generateUsername(usernamePrefix);
    }

    @RequestMapping(value = "/validate-token", method = RequestMethod.POST, consumes = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void validateToken(@RequestBody String token) {
        User user = getByToken(token);
        if (user == null) {
            throw new AccessDeniedException("Token does not exist.");
        }
    }

    @RequestMapping(value = "/password-change-token", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePasswordByToken(@RequestBody Map<String, String> data) {
        final User original = getByToken(data.get("token"));
        assert original != null;
        userService.changePasswordByToken(original, data.get("password"));
        if (LOG.isTraceEnabled()) {
            LOG.trace("User's password successfully changed by token.", original.getUsername());
        }
    }

    @RequestMapping(value = "/send-invitation/{username}", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sendInvitation(@PathVariable(value = "username") String username) {
        final User original = getByUsername(username);
        assert original != null;
        if (!original.getIsInvited().equals("false")) {
            throw new UnauthorizedException("Cannot invite already invited user.");
        }
        userService.sendInvitation(original);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Invitation has been sent to user", original.getUsername());
        }
    }

    private User getByToken(String token) {
        assert token != null;
        return userService.findByToken(token);
    }
}
