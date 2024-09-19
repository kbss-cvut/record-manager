package cz.cvut.kbss.study.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserTest {

    private User user;

    @BeforeEach
    public void setUp() {
        this.user = new User();
    }

    @Test
    public void newInstanceHasAgentInTypes() {
        assertTrue(user.getRoleGroup().getRoles().contains(Role.user));
    }

    @Test
    public void encodePasswordThrowsIllegalStateForNullPassword() {
        final IllegalStateException ex = assertThrows(IllegalStateException.class, () -> user.encodePassword(new BCryptPasswordEncoder()));
        assertEquals("Cannot encode an empty password.", ex.getMessage());
    }

    @Test
    public void encodePasswordChangesPassword() {
        user.setPassword("password");
        user.encodePassword(new BCryptPasswordEncoder());
        assertFalse(user.getPassword().contains("password"));
    }

    @Test
    public void generateUriCreatesUriFromFirstNameAndLastName() {
        user.setFirstName("Josh");
        user.setLastName("Ulk");
        user.generateUri();

        assertNotNull(user.getUri());
        assertTrue(user.getUri().toString().contains("Josh"));
        assertTrue(user.getUri().toString().contains("Ulk"));
    }

    @Test
    public void generateUriThrowsIllegalStateForMissingFirstName() {
        user.setLastName("b");
        final IllegalStateException ex = assertThrows(IllegalStateException.class, () -> user.generateUri());
        assertEquals("Cannot generate Person URI without first name.", ex.getMessage());
    }

    @Test
    public void generateUriThrowsIllegalStateForEmptyFirstName() {
        user.setFirstName("");
        user.setLastName("b");
        final IllegalStateException ex = assertThrows(IllegalStateException.class, () -> user.generateUri());
        assertEquals("Cannot generate Person URI without first name.", ex.getMessage());
    }

    @Test
    public void generateUriThrowsIllegalStateForMissingLastName() {
        user.setFirstName("John");
        final IllegalStateException ex = assertThrows(IllegalStateException.class, () -> user.generateUri());
        assertEquals("Cannot generate Person URI without last name.", ex.getMessage());
    }

    @Test
    public void generateUriThrowsIllegalStateForEmptyLastName() {
        user.setFirstName("John");
        user.setLastName("");
        final IllegalStateException ex = assertThrows(IllegalStateException.class, () -> user.generateUri());
        assertEquals("Cannot generate Person URI without last name.", ex.getMessage());
    }

    @Test
    public void generateUriDoesNothingIfTheUriIsAlreadySet() {
        final String uri = Vocabulary.ONTOLOGY_IRI_RECORD_MANAGER + "/test";
        user.setUri(URI.create(uri));
        user.generateUri();
        assertEquals(uri, user.getUri().toString());
    }

    @Test
    public void generateUriEncodesUsersWithComplexName() {
        User user = new User();

        user.setFirstName("Mike John");
        user.setLastName("Brave");
        user.generateUri();

        assertNotNull(user.getUri());
        assertTrue(user.getUri().toString().contains("John"));
        assertTrue(user.getUri().toString().contains("Mike"));
        assertTrue(user.getUri().toString().contains("Brave"));
    }

    @Test
    public void newUserHasRoleDoctor() {
        User user = new User();
        assertTrue(user.getRoleGroup().getRoles().contains(Role.user));
    }
}