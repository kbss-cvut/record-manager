package cz.cvut.kbss.study.security.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    static Stream<Arguments> generator() {
        return Stream.of(Role.values()).map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("generator")
    void forTypeReturnsRoleMatchingSpecifiedType(Role r) {
        final Optional<Role> result = Role.forType(r.getType());
        assertTrue(result.isPresent());
        assertEquals(r, result.get());
    }

    @Test
    void forTypeReturnsEmptyOptionalForUnknownRoleType() {
        assertTrue(Role.forType("unknownType").isEmpty());
    }

    @ParameterizedTest
    @MethodSource("generator")
    void forNameReturnsRoleMatchingSpecifiedRoleName(Role r) {
        final Optional<Role> result = Role.forName(r.getName());
        assertTrue(result.isPresent());
        assertEquals(r, result.get());
    }

    @Test
    void forNameReturnsEmptyOptionalForUnknownRoleName() {
        assertTrue(Role.forName("unknownName").isEmpty());
    }
}