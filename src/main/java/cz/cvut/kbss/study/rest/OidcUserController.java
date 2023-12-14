package cz.cvut.kbss.study.rest;

import cz.cvut.kbss.study.exception.NotFoundException;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.model.util.HasUri;
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
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
        Institution newInstitution = user.getInstitution();
        Institution oldInstitution = original.getInstitution();
        original.setInstitution(newInstitution);
        userService.update(original, sendEmail, "profileUpdate");

        if (LOG.isTraceEnabled() && ! equals(newInstitution, oldInstitution)) {
            LOG.trace("Set user institution {} to institution {} successfully.", user, oldInstitution, newInstitution);
        }
    }

    /**
     * Input arguments can be null and getUri can be null.
     * @param entity1
     * @param entity2
     * @return true if the URIs of the input arguments are equal, false otherwise.
     */
    private boolean equals(HasUri entity1, HasUri entity2){
        URI u1 = getURI(entity1);
        URI u2 = getURI(entity2);
        return Objects.equals(u1, u2);
    }

    /**
     * Retrieves the URI of entity argument. entity can be null.
     * @param entity
     * @return the URI of the entity or null if entity is null.
     */
    private URI getURI(HasUri entity){
        return Optional.ofNullable(entity).map(HasUri::getUri).orElse(null);
    }

    private List<User> getByInstitution(String institutionKey) {
        assert institutionKey != null;
        final Institution institution = institutionService.findByKey(institutionKey);
        return userService.findByInstitution(institution);
    }
}
