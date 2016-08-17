package cz.cvut.kbss.study.rest;

import cz.cvut.kbss.study.exception.NotFoundException;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.rest.exception.BadRequestException;
import cz.cvut.kbss.study.rest.util.RestUtils;
import cz.cvut.kbss.study.security.SecurityConstants;
import cz.cvut.kbss.study.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    @RequestMapping(method = RequestMethod.GET, value = "/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    public User getByUsername(@PathVariable("username") String username) {
        final User user = userService.findByUsername(username);
        if (user == null) {
            throw NotFoundException.create("User", username);
        }
        return user;
    }

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

    @PreAuthorize("hasRole('" + SecurityConstants.ROLE_ADMIN + "')")
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<User> getUsers() {
        final List<User> users = userService.findAll();
        users.forEach(User::erasePassword);
        return users;
    }

    @PreAuthorize("hasRole('" + SecurityConstants.ROLE_ADMIN + "')")
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
}
