package cz.cvut.kbss.study.security.model;

import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.model.Vocabulary;
import cz.cvut.kbss.study.security.SecurityConstants;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class UserDetails implements org.springframework.security.core.userdetails.UserDetails {

    private static final Map<String, String> ROLE_MAPPING = initRoleMapping();

    private final User user;

    private final Set<GrantedAuthority> authorities;

    private static Map<String, String> initRoleMapping() {
        final Map<String, String> result = new HashMap<>();
        result.put(Vocabulary.s_c_administrator, SecurityConstants.ROLE_ADMIN);
        result.put(Vocabulary.s_c_doctor, SecurityConstants.ROLE_USER);
        return result;
    }

    public UserDetails(User user) {
        Objects.requireNonNull(user);
        this.user = user;
        this.authorities = new HashSet<>();
        resolveRoles();
    }

    public UserDetails(User user, Collection<GrantedAuthority> authorities) {
        Objects.requireNonNull(user);
        Objects.requireNonNull(authorities);
        this.user = user;
        this.authorities = new HashSet<>();
        resolveRoles();
        this.authorities.addAll(authorities);
    }

    private void resolveRoles() {
        authorities.addAll(ROLE_MAPPING.entrySet().stream().filter(e -> user.getTypes().contains(e.getKey()))
                                       .map(e -> new SimpleGrantedAuthority(e.getValue())).toList());
        authorities.add(new SimpleGrantedAuthority(SecurityConstants.ROLE_USER));
    }

    public void eraseCredentials() {
        user.erasePassword();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.unmodifiableCollection(authorities);
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public User getUser() {
        return user;
    }
}
