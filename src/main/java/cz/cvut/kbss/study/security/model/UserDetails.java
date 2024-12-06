package cz.cvut.kbss.study.security.model;

import cz.cvut.kbss.study.model.Role;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.security.SecurityConstants;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class UserDetails implements org.springframework.security.core.userdetails.UserDetails {

    private final User user;

    private final Set<GrantedAuthority> authorities;

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
        authorities.addAll(
                user.getRoleGroup().getRoles().stream()
                    .map(r -> new SimpleGrantedAuthority(r.getRoleName()))
                    .toList());
        authorities.add(new SimpleGrantedAuthority(Role.user.name()));
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
        return true;
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

    @Override
    public String toString() {
        return "UserDetails{" +
                "user=" + user +
                ", authorities=" + authorities +
                '}';
    }
}
