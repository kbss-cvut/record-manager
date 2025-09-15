package cz.cvut.kbss.study.service.security;

import cz.cvut.kbss.study.persistence.dao.UserDao;
import cz.cvut.kbss.study.service.BaseServiceTestRunner;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class UserDetailsServiceTest extends BaseServiceTestRunner {

    @InjectMocks
    private UserDetailsService userDetailsService;

    @Mock
    private UserDao userDao;

    @Test
    void loadUserByUsernameReturnsUserDetailsWhenUserExists() {
        when(userDao.findByUsername(this.admin.getUsername())).thenReturn(this.admin);
        UserDetails result = userDetailsService.loadUserByUsername(this.admin.getUsername());
        assertNotNull(result);
        assertEquals(admin.getUsername(), result.getUsername());
        assertEquals(admin.getPassword(), result.getPassword());
        assertInstanceOf(UserDetails.class, result);
        verify(userDao).findByUsername(admin.getUsername());
    }

    @Test
    void loadUserByUsername_ThrowsUsernameNotFoundException_WhenUserDoesNotExist() {
        when(userDao.findByUsername("nonexistent")).thenReturn(null);
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () ->
                userDetailsService.loadUserByUsername("nonexistent"));
        assertEquals("User with username 'nonexistent' does not exist.", exception.getMessage());
        verify(userDao).findByUsername("nonexistent");
    }
}
