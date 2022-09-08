package cz.cvut.kbss.study.model;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.security.crypto.password.StandardPasswordEncoder;

import java.net.URI;

import static org.junit.Assert.*;

public class UserTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private User user;

    @Before
    public void setUp() {
        this.user = new User();
    }

    @Test
    public void newInstanceHasAgentInTypes() {
        assertTrue(user.getTypes().contains(Vocabulary.s_c_doctor));
    }

    @Test
    public void encodePasswordThrowsIllegalStateForNullPassword() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Cannot encode an empty password.");
        user.encodePassword(new StandardPasswordEncoder());
    }

    @Test
    public void encodePasswordChangesPassword() {
        user.setPassword("password");
        user.encodePassword(new StandardPasswordEncoder());
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
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Cannot generate Person URI without first name.");
        user.setLastName("b");
        user.generateUri();
    }

    @Test
    public void generateUriThrowsIllegalStateForEmptyFirstName() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Cannot generate Person URI without first name.");
        user.setFirstName("");
        user.setLastName("b");
        user.generateUri();
    }

    @Test
    public void generateUriThrowsIllegalStateForMissingLastName() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Cannot generate Person URI without last name.");
        user.setFirstName("John");
        user.generateUri();
    }

    @Test
    public void generateUriThrowsIllegalStateForEmptyLastName() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Cannot generate Person URI without last name.");
        user.setFirstName("John");
        user.setLastName("");
        user.generateUri();
    }

    @Test
    public void generateUriDoesNothingIfTheUriIsAlreadySet() {
        final String uri = Vocabulary.ONTOLOGY_IRI_record_manager + "/test";
        user.setUri(URI.create(uri));
        user.generateUri();
        assertEquals(uri, user.getUri().toString());
    }

    @Test
    public void generateUriEncodesUsersWithComplexName() throws Exception {
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
    public void newUserHasRoleDoctor() throws Exception {
        User user = new User();
        assertTrue(user.getTypes().toString().contains(Vocabulary.s_c_doctor));
    }
}