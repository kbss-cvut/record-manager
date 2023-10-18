package cz.cvut.kbss.study.persistence.dao;

import cz.cvut.kbss.study.environment.generator.Generator;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.model.Vocabulary;
import cz.cvut.kbss.study.persistence.BaseDaoTestRunner;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class UserDaoTest extends BaseDaoTestRunner {

    @Autowired
    private UserDao userDao;

    @Autowired
    private InstitutionDao institutionDao;

    @Test
    public void getUserByUsername() {
        User user1 = getPersistedUser();

        User user2 = userDao.findByUsername(user1.getUsername());

        assertSameUser(user1, user2);
    }

    @Test
    public void getUserByEmailWithExistingEmailSucceeds() {
        User user1 = getPersistedUser();

        User user2 = userDao.findByEmail(user1.getEmailAddress());

        assertSameUser(user1, user2);
    }

    @Test
    public void getUserByEmailIsCaseInsensitive() {
        User user1 = getPersistedUser();

        User user2 = userDao.findByEmail(user1.getEmailAddress().toLowerCase());
        User user3 = userDao.findByEmail(user1.getEmailAddress().toUpperCase());

        assertSameUser(user1, user2);
        assertSameUser(user1, user3);

    }

    @Test
    public void getUserByEmailWithoutTrimmingSucceeds() {
        User user1 = getPersistedUser();

        User user2 = userDao.findByEmail("  \n" + user1.getEmailAddress() + " \n\t");

        assertSameUser(user1, user2);
    }

    @Test
    public void getUserByEmailWithNonExistingEmailReturnsNull() {

        User user = userDao.findByEmail("non-existing@email.com");
        assertNull(user);
    }

    @Test
    public void getUsersByToken() {
        Institution institution = Generator.generateInstitution();
        User user = Generator.generateUser(institution);
        user.setToken("Token");

        transactional(() -> {
            institutionDao.persist(institution);
            userDao.persist(user);
        });

        final User result =  userDao.findByToken("Token");
        User userNull = userDao.findByUsername("NotExistingToken");

        assertNotNull(result);
        assertNull(userNull);
    }

    @Test
    public void getUsersByInstitution() {
        Institution institution1 = Generator.generateInstitution();
        Institution institution2 = Generator.generateInstitution();
        User user1 = Generator.generateUser(institution1);
        User user2 = Generator.generateUser(institution1);

        transactional(() -> {
            institutionDao.persist(institution1);
            institutionDao.persist(institution2);
            userDao.persist(user1);
            userDao.persist(user2);
        });

        List<User> usersList = userDao.findByInstitution(institution1);
        List<User> emptyList = userDao.findByInstitution(institution2);

        assertEquals(2, usersList.size());
        assertEquals(0, emptyList.size());

        transactional(() -> userDao.remove(user1));

        usersList = userDao.findByInstitution(institution1);
        assertEquals(1, usersList.size());
    }

    @Test
    public void getNumberOfInvestigators() {
        Institution institution = Generator.generateInstitution();
        User user1 = Generator.generateUser(institution);
        User user2 = Generator.generateUser(institution);
        User user3 = Generator.generateUser(institution);

        Set<String> types = new HashSet<>();
        types.add(Vocabulary.s_c_administrator);
        types.add(Vocabulary.s_c_doctor);

        user3.setTypes(types);

        transactional(() -> {
            institutionDao.persist(institution);
            userDao.persist(List.of(user1, user2, user3));
        });

        int numberOfInvestigators = userDao.getNumberOfInvestigators();

        assertEquals(2, numberOfInvestigators);
    }


    private User getPersistedUser() {
        Institution institution = Generator.generateInstitution();
        User user = Generator.generateUser(institution);
        transactional(() -> {
            institutionDao.persist(institution);
            userDao.persist(user);
        });
        return user;
    }

    private void assertSameUser(User expectedUser, User actualUser) {
        assertEquals(expectedUser.getUsername(), actualUser.getUsername());
        assertEquals(expectedUser.getEmailAddress(), actualUser.getEmailAddress());
    }

}