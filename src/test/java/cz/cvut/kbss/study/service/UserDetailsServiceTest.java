package cz.cvut.kbss.study.service;

import cz.cvut.kbss.study.environment.generator.Generator;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.User;
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

    @Autowired
    private InstitutionService institutionService;

    @Test
    public void loadUserByUsername() throws Exception {
        Institution institution = Generator.generateInstitution();
        institutionService.persist(institution);

        User user = Generator.generateUser(institution);
        userService.persist(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        assertEquals(userDetails.getUsername(), user.getUsername());
    }

    @Test(expected = UsernameNotFoundException.class)
    public void loadUserByUsernameExpectException() throws Exception {
        userDetailsService.loadUserByUsername("CarolansRoyal12");
    }
}

@Test(expected = UsernameNotFoundException.class)
public void loadUserByUsernameExpectException() throws Exception {
    userDetailsService.loadUserByUsername(USERNAME1);
}
}
*/