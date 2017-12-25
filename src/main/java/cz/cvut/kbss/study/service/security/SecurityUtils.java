package cz.cvut.kbss.study.service.security;

import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.security.model.UserDetails;
import cz.cvut.kbss.study.service.InstitutionService;
import cz.cvut.kbss.study.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SecurityUtils {

    @Autowired
    private UserService userService;
    /**
     * Gets the currently authenticated user.
     *
     * @return Current user
     */
    public User getCurrentUser() {
        final SecurityContext context = SecurityContextHolder.getContext();
        assert context != null;
        final UserDetails userDetails = (UserDetails) context.getAuthentication().getDetails();
        return userDetails.getUser();
    }

    /**
     * Gets details of the currently authenticated user.
     *
     * @return Currently authenticated user details or null, if no one is currently authenticated
     */
    public UserDetails getCurrentUserDetails() {
        final SecurityContext context = SecurityContextHolder.getContext();
        if (context.getAuthentication() != null && context.getAuthentication().getDetails() instanceof UserDetails) {
            return (UserDetails) context.getAuthentication().getDetails();
        } else {
            return null;
        }
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
     * Checks whether the current user is in same institution as user we are asking for.
     *
     * @param username String identifier
     * @return Membership status of the current user and another user
     */
    public boolean areFromSameInstitution(String username) {
        final User user = getCurrentUser();
        final List<User> users = userService.findByInstitution(user.getInstitution());
        return users.stream().anyMatch(o -> o.getUsername().equals(username));
    }
}
