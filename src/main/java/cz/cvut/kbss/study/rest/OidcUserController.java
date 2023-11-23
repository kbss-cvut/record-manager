package cz.cvut.kbss.study.rest;

import cz.cvut.kbss.study.exception.NotFoundException;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.security.SecurityConstants;
import cz.cvut.kbss.study.service.InstitutionService;
import cz.cvut.kbss.study.service.UserService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    private List<User> getByInstitution(String institutionKey) {
        assert institutionKey != null;
        final Institution institution = institutionService.findByKey(institutionKey);
        return userService.findByInstitution(institution);
    }
}
