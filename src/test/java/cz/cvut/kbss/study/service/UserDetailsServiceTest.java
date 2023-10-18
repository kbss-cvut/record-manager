package cz.cvut.kbss.study.service;

import cz.cvut.kbss.study.environment.generator.Generator;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.service.security.UserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class UserDetailsServiceTest extends BaseServiceTestRunner {
    @Autowired
    private UserService userService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private InstitutionService institutionService;

    @Test
    public void loadUserByUsername() {
        Institution institution = Generator.generateInstitution();
        institutionService.persist(institution);

        User user = Generator.generateUser(institution);
        userService.persist(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        assertEquals(userDetails.getUsername(), user.getUsername());
    }

    @Test
    public void loadUserByUsernameExpectException() {
        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername("CarolansRoyal12"));
    }
}
