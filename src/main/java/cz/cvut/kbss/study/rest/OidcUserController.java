package cz.cvut.kbss.study.rest;

import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.security.SecurityConstants;
import cz.cvut.kbss.study.service.UserService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    public OidcUserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasRole('" + SecurityConstants.ROLE_USER + "')")
    @GetMapping(value = "/current", produces = MediaType.APPLICATION_JSON_VALUE)
    public User getCurrent() {
        return userService.getCurrentUser();
    }
}
