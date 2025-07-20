package cz.cvut.kbss.study.security;

import cz.cvut.kbss.study.rest.exception.BadRequestException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Extends default user switching logic by preventing switching to an admin account.
 */
public class CustomSwitchUserFilter extends SwitchUserFilter {

    @Override
    protected Authentication attemptSwitchUser(HttpServletRequest request) throws AuthenticationException {

        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        final Authentication switchTo = super.attemptSwitchUser(request);
        if (currentAuth != null && !hasHigherPrivileges(currentAuth, switchTo)){
            throw new BadRequestException("Cannot switch to a user with higher or equal privileges.");
        }
        return switchTo;
    }

    private boolean hasHigherPrivileges(Authentication a1, Authentication a2) {
        Set<String> a1Authorities = a1.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> !auth.equals(SwitchUserFilter.ROLE_PREVIOUS_ADMINISTRATOR))
                .collect(Collectors.toSet());

        Set<String> a2Authorities = a2.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> !auth.equals(SwitchUserFilter.ROLE_PREVIOUS_ADMINISTRATOR))
                .collect(Collectors.toSet());

        return a1Authorities.containsAll(a2Authorities) && a1Authorities.size() > a2Authorities.size();
    }
}
