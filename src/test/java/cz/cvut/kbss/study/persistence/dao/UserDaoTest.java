package cz.cvut.kbss.study.persistence.dao;

import cz.cvut.kbss.study.environment.generator.Generator;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.model.Vocabulary;
import cz.cvut.kbss.study.persistence.BaseDaoTestRunner;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserDaoTest extends BaseDaoTestRunner {

    @Autowired
    private UserDao userDao;

    @Autowired
    private InstitutionDao institutionDao;

    @Test
    public void getUserByUsername() throws Exception {
        Institution institution = Generator.generateInstitution();
        institutionDao.persist(institution);

        User user1 = Generator.generateUser(institution);
        userDao.persist(user1);

        User user2 = userDao.findByUsername(user1.getUsername());

        assertEquals(user1.getUsername(), user2.getUsername());
        assertEquals(user1.getEmailAddress(), user2.getEmailAddress());
    }

    @Test
    public void getUserByEmail() throws Exception {
        Institution institution = Generator.generateInstitution();
        institutionDao.persist(institution);

        User user1 = Generator.generateUser(institution);
        userDao.persist(user1);

        User user2 = userDao.findByEmail(user1.getEmailAddress());

        assertEquals(user1.getUsername(), user2.getUsername());
        assertEquals(user1.getEmailAddress(), user2.getEmailAddress());
    }

    @Test
    public void getUsersByToken() throws Exception {
        Institution institution = Generator.generateInstitution();

        institutionDao.persist(institution);

        User user = Generator.generateUser(institution);
        user.setToken("Token");

        userDao.persist(user);

        user = userDao.findByToken("Token");
        User userNull = userDao.findByUsername("NotExistingToken");

        assertNotNull(user);
        assertNull(userNull);
    }

    @Test
    public void getUsersByInstitution() throws Exception {
        Institution institution1 = Generator.generateInstitution();
        Institution institution2 = Generator.generateInstitution();

        institutionDao.persist(institution1);
        institutionDao.persist(institution2);

        User user1 = Generator.generateUser(institution1);
        User user2 = Generator.generateUser(institution1);

        userDao.persist(user1);
        userDao.persist(user2);

        List<User> usersList = userDao.findByInstitution(institution1);
        List<User> emptyList = userDao.findByInstitution(institution2);

        assertEquals(2, usersList.size());
        assertEquals(0, emptyList.size());

        userDao.remove(user1);

        usersList = userDao.findByInstitution(institution1);
        assertEquals(1, usersList.size());
    }

    @Test
    public void getNumberOfInvestigators() throws Exception {
        Institution institution = Generator.generateInstitution();

        institutionDao.persist(institution);

        User user1 = Generator.generateUser(institution);
        User user2 = Generator.generateUser(institution);
        User user3 = Generator.generateUser(institution);

        Set<String> types = new HashSet<>();
        types.add(Vocabulary.s_c_administrator);
        types.add(Vocabulary.s_c_doctor);

        user3.setTypes(types);

        userDao.persist(user1);
        userDao.persist(user2);
        userDao.persist(user3);

        int numberOfInvestigators = userDao.getNumberOfInvestigators();

        assertEquals(2, numberOfInvestigators);
    }
}