package cz.cvut.kbss.study.security.model;

import cz.cvut.kbss.study.model.Vocabulary;
import cz.cvut.kbss.study.security.SecurityConstants;

import java.util.Optional;
import java.util.stream.Stream;

public enum Role {
    USER(SecurityConstants.ROLE_USER, Vocabulary.s_i_user),
    ADMIN(SecurityConstants.ROLE_ADMIN, Vocabulary.s_i_administrator);

    private final String name;
    private final String type;

    Role(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public static Optional<Role> forType(String type) {
        return Stream.of(Role.values()).filter(r -> r.type.equals(type)).findAny();
    }

    public static Optional<Role> forName(String name) {
        return Stream.of(Role.values()).filter(r -> r.name.equals(name)).findAny();
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}
