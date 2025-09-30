package cz.cvut.kbss.study.util.oidc;

import cz.cvut.kbss.study.model.Role;
import cz.cvut.kbss.study.service.ConfigReader;
import cz.cvut.kbss.study.util.ConfigParam;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.ClaimAccessor;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;

public class OidcGrantedAuthoritiesExtractor implements Converter<Jwt, Collection<SimpleGrantedAuthority>> {

    private final ConfigReader config;

    public OidcGrantedAuthoritiesExtractor(ConfigReader config) {
        this.config = config;
    }

    @Override
    public Collection<SimpleGrantedAuthority> convert(Jwt source) {
        return extractRoles(source).stream()
                .map(Role::fromName)                        
                .flatMap(Optional::stream)
                .map(Role::getRoleName)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    public List<String> extractRoles(ClaimAccessor source) {
        final String rolesClaim = config.getConfig(ConfigParam.OIDC_ROLE_CLAIM);
        final String[] parts = rolesClaim.split("\\.");
        assert parts.length > 0;
        final List<String> roles;
        if (parts.length == 1) {
            roles = source.getClaimAsStringList(rolesClaim);
        } else {
            Map<String, Object> map = source.getClaimAsMap(parts[0]);
            for (int i = 1; i < parts.length - 1; i++) {
                if (map.containsKey(parts[i]) && !(map.get(parts[i]) instanceof Map)) {
                    throw new IllegalArgumentException("Access token does not contain roles under the expected claim '" + rolesClaim + "'.");
                }
                map = (Map<String, Object>) map.getOrDefault(parts[i], Collections.emptyMap());
            }
            if (map.containsKey(parts[parts.length - 1]) && !(map.get(parts[parts.length - 1]) instanceof List)) {
                throw new IllegalArgumentException("Roles claim does not contain a list.");
            }
            roles = (List<String>) map.getOrDefault(parts[parts.length - 1], Collections.emptyList());
        }
        return roles;
    }
}
