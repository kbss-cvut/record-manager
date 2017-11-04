package cz.cvut.kbss.study.service;

import cz.cvut.kbss.study.environment.util.Generator;
import cz.cvut.kbss.study.service.security.UserDetailsService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static junit.framework.TestCase.assertEquals;

public class UserDetailsServiceTest extends BaseServiceTestRunner {
    @Autowired
    private UserService userService;

    @Autowired
    private UserDetailsService userDetailsService;

    public static final String USERNAME1 = "Pepa";
    public static final String PASSWORD1 = "house123";
    public static final String FIRST_NAME1 = "Josef";
    public static final String LAST_NAME1 = "Barák";
    public static final String EMAIL1 = "pepa@barak.pro";

    @Test
    public void loadUserByUsername() throws Exception {
        userService.persist(Generator.getUser(USERNAME1, PASSWORD1, FIRST_NAME1, LAST_NAME1, EMAIL1, null));
        UserDetails user = userDetailsService.loadUserByUsername(USERNAME1);
        assertEquals(USERNAME1, user.getUsername());
    }

    @Test(expected = UsernameNotFoundException.class)
    public void loadUserByUsernameExpectException() throws Exception {
        userDetailsService.loadUserByUsername(USERNAME1);
    }
}
