package cz.cvut.kbss.study.service.security;

import cz.cvut.kbss.study.environment.generator.Generator;
import cz.cvut.kbss.study.environment.util.Environment;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.security.model.UserDetails;
import cz.cvut.kbss.study.service.BaseServiceTestRunner;
import cz.cvut.kbss.study.service.InstitutionService;
import cz.cvut.kbss.study.service.UserService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.Assert.*;

public class SecurityUtilsTest extends BaseServiceTestRunner {

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private UserService userService;

    @Autowired
    private InstitutionService institutionService;

    private User user;
    public static final String USERNAME = "halsey";
    public static final String PASSWORD = "john117";

    @Before
    public void setUp() {
        Institution institution = Generator.generateInstitution();
        institutionService.persist(institution);
        this.user = Generator.getUser(USERNAME, PASSWORD, "John", "Johnie", "Johnie@gmail.com", institutionService.findByName(institution.getName()));
        user.generateUri();
        userService.persist(user);
    }

    @After
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void getCurrentUserReturnsCurrentlyLoggedInUser() {
        Environment.setCurrentUser(user);
        final User result = securityUtils.getCurrentUser();
        assertEquals(user, result);
    }

    @Test
    public void getCurrentUserDetailsReturnsUserDetailsOfCurrentlyLoggedInUser() {
        Environment.setCurrentUser(user);
        final UserDetails result = securityUtils.getCurrentUserDetails();
        assertNotNull(result);
        assertTrue(result.isEnabled());
        assertEquals(user, result.getUser());
    }

    @Test
    public void getCurrentUserDetailsReturnsNullIfNoUserIsLoggedIn() {
        assertNull(securityUtils.getCurrentUserDetails());
    }
}
*/