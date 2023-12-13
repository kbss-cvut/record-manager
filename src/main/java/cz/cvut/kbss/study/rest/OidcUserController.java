package cz.cvut.kbss.study.rest;

import cz.cvut.kbss.study.exception.NotFoundException;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.rest.exception.BadRequestException;
import cz.cvut.kbss.study.security.SecurityConstants;
import cz.cvut.kbss.study.service.InstitutionService;
import cz.cvut.kbss.study.service.UserService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * API for getting basic user info.
 * <p>
 * Enabled when OIDC security is used.
 */
@ConditionalOnProperty(prefix = "security", name = "provider", havingValue = "oidc")
@RestController
@RequestMapping("/users")
public class OidcUserController extends BaseController {

    private final UserService userService;

    private final InstitutionService institutionService;

    public OidcUserController(UserService userService, InstitutionService institutionService) {
        this.userService = userService;
        this.institutionService = institutionService;
    }

    @PreAuthorize("hasRole('" + SecurityConstants.ROLE_USER + "')")
    @GetMapping(value = "/current", produces = MediaType.APPLICATION_JSON_VALUE)
    public User getCurrent() {
        return userService.getCurrentUser();
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

    @PreAuthorize(
            "hasRole('" + SecurityConstants.ROLE_ADMIN + "') " +
                    "or hasRole('" + SecurityConstants.ROLE_USER + "') and @securityUtils.isMemberOfInstitution(#institutionKey)")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<User> getUsers(@RequestParam(value = "institution", required = false) String institutionKey) {
        return institutionKey != null ? getByInstitution(institutionKey) : userService.findAll();
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

        // validate institution update is valid
        List<URI> institutionURIs = Arrays.asList(user.getInstitution(), original.getInstitution())
                .stream().map(i -> i != null ? i.getUri() : null).collect(Collectors.toList());
        if (Objects.equals(institutionURIs.get(0), institutionURIs.get(1))){
            LOG.warn("Ignoring attempt to add user {} to institution {} because it is already in institution {}.", user, institutionURIs.get(0), institutionURIs.get(1));
            return;
        }

        // make sure only institution is updated
        Institution newInstitution = user.getInstitution();
        user = original.copy();
        user.setInstitution(newInstitution);
        userService.update(user, sendEmail, "profileUpdate");
        if (LOG.isTraceEnabled()) {
            LOG.trace("Added user {} to institution {} successfully.", user, user.getInstitution());
        }
    }

    private List<User> getByInstitution(String institutionKey) {
        assert institutionKey != null;
        final Institution institution = institutionService.findByKey(institutionKey);
        return userService.findByInstitution(institution);
    }
}
