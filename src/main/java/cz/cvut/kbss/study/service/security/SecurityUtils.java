package cz.cvut.kbss.study.service.security;

import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.security.model.UserDetails;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SecurityUtils {

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
     * Checks whether the current user is a member of a clinic with the specified key.
     *
     * @param clinicKey Clinic identifier
     * @return Membership status of the current user
     */
    public boolean isMemberOfClinic(String clinicKey) {
        final User user = getCurrentUser();
        return user.getClinic() != null && user.getClinic().getKey().equals(clinicKey);
    }
}
