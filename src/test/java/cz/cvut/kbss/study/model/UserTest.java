package cz.cvut.kbss.study.model;

import static org.junit.Assert.*;
import org.junit.Test;

public class UserTest {
    @Test
    public void generateUriEncodesUsersWithComplexName() throws Exception {

        User user = new User();
        user.setFirstName("Mike John");
        user.setLastName("Brave");
        user.generateUri();
        assertNotNull(user.getUri());
        assertTrue(user.getUri().toString().contains("Mike"));
        assertTrue(user.getUri().toString().contains("John"));
        assertTrue(user.getUri().toString().contains("Brave"));
    }

}