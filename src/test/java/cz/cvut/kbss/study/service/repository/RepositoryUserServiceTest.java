package cz.cvut.kbss.study.service.repository;

import cz.cvut.kbss.study.environment.generator.Generator;
import cz.cvut.kbss.study.environment.util.Environment;
import cz.cvut.kbss.study.exception.NotFoundException;
import cz.cvut.kbss.study.model.RoleGroup;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.persistence.dao.UserDao;
import cz.cvut.kbss.study.service.BaseServiceTestRunner;
import cz.cvut.kbss.study.service.EmailService;
import cz.cvut.kbss.study.service.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class RepositoryUserServiceTest extends BaseServiceTestRunner {

    @InjectMocks
    private RepositoryUserService userService;

    @Mock
    private UserDao userDao;;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private EmailService emailService;

    @Test
    void findByUsernameReturnsUserWhenUserExists() {
        when(userDao.findByUsername(admin.getUsername())).thenReturn(admin);
        User result = userService.findByUsername(admin.getUsername());
        assertEquals(admin, result);
        verify(userDao).findByUsername(admin.getUsername());
    }

    @Test
    void findByUsernameReturnsNullWhenUserDoesNotExist() {
        when(userDao.findByUsername(anyString())).thenReturn(null);
        User result = userService.findByUsername("nonexistent");
        assertNull(result);
        verify(userDao).findByUsername("nonexistent");
    }

    @Test
    void getCurrentUserReturnsUserFromSecurityUtils() {
        Environment.setCurrentUser(admin);
        when(securityUtils.getCurrentUser()).thenReturn(admin);
        User result = userService.getCurrentUser();
        assertEquals(admin, result);
        verify(securityUtils).getCurrentUser();
    }

    @Test
    void findByInstitutionReturnsUsersForInstitution() {
        List<User> users = List.of(admin, user);
        when(userDao.findByInstitution(institution)).thenReturn(users);
        List<User> result = userService.findByInstitution(institution);
        assertEquals(users, result);
        verify(userDao).findByInstitution(institution);
    }

    @Test
    void findByEmailReturnsUserWhenEmailExists() {
        when(userDao.findByEmail(admin.getEmailAddress())).thenReturn(admin);
        User result = userService.findByEmail(admin.getEmailAddress());
        assertEquals(admin, result);
        verify(userDao).findByEmail(admin.getEmailAddress());
    }

    @Test
    void findByEmailThrowsNotFoundExceptionWhenEmailDoesNotExist() {
        when(userDao.findByEmail(anyString())).thenReturn(null);
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.findByEmail("nonexistent@example.com"));
        assertEquals("Could not find user by email 'nonexistent@example.com'.", exception.getMessage());
        verify(userDao).findByEmail("nonexistent@example.com");
    }

    @Test
    void findByTokenReturnsUserWhenTokenExists() {
        when(userDao.findByToken("token123")).thenReturn(admin);
        User result = userService.findByToken("token123");
        assertEquals(admin, result);
        verify(userDao).findByToken("token123");
    }



}
