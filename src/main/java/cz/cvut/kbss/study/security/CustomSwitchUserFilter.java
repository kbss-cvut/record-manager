package cz.cvut.kbss.study.security;

import cz.cvut.kbss.study.rest.exception.BadRequestException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;

import static cz.cvut.kbss.study.service.security.SecurityUtils.hasSupersetOfRoles;

/**
 * Extends default user switching logic by preventing switching to an admin account.
 */
public class CustomSwitchUserFilter extends SwitchUserFilter {

    @Override
    protected Authentication attemptSwitchUser(HttpServletRequest request) throws AuthenticationException {

        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        final Authentication switchTo = super.attemptSwitchUser(request);
        if (currentAuth != null && !hasSupersetOfRoles(currentAuth, switchTo)) {
            throw new BadRequestException("Cannot switch to a user with higher privileges.");
        }
        return switchTo;
    }

}
