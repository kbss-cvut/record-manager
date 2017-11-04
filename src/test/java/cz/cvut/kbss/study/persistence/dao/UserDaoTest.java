package cz.cvut.kbss.study.persistence.dao;

import cz.cvut.kbss.study.environment.util.Generator;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.persistence.BaseDaoTestRunner;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static junit.framework.TestCase.assertEquals;

public class UserDaoTest extends BaseDaoTestRunner {
    @Autowired
    private UserDao userDao;

    @Autowired
    private InstitutionDao institutionDao;

    public static final String USERNAME1 = "Pepa";
    public static final String PASSWORD1 = "house123";
    public static final String FIRST_NAME1 = "Josef";
    public static final String LAST_NAME1 = "Barák";
    public static final String EMAIL1 = "pepa@barak.pro";

    public static final String USERNAME2 = "Franta";
    public static final String PASSWORD2 = "tree123";
    public static final String FIRST_NAME2 = "Franta";
    public static final String LAST_NAME2 = "Strom";
    public static final String EMAIL2 = "franta@strom.pro";

    @Test
    public void getUserByUsername() throws Exception {

        userDao.persist(Generator.getUser(USERNAME1, PASSWORD1, FIRST_NAME1, LAST_NAME1, EMAIL1, null));
        User user = userDao.findByUsername(USERNAME1);

        assertEquals(USERNAME1, user.getUsername());
        assertEquals(EMAIL1, user.getEmailAddress());
    }

    @Test
    public void getUserByEmail() throws Exception {
        userDao.persist(Generator.getUser(USERNAME1, PASSWORD1, FIRST_NAME1, LAST_NAME1, EMAIL1, null));
        User user = userDao.findByEmail(EMAIL1);

        assertEquals(USERNAME1, user.getUsername());
        assertEquals(EMAIL1, user.getEmailAddress());
    }

    @Test
    public void getUsersByInstitution() throws Exception {
        Institution institution1 = Generator.generateInstitution();
        Institution institution2 = Generator.generateInstitution();

        institutionDao.persist(institution1);
        institutionDao.persist(institution2);

        User user1 = Generator.getUser(USERNAME1, PASSWORD1, FIRST_NAME1, LAST_NAME1, EMAIL1, institution1);
        User user2 = Generator.getUser(USERNAME2, PASSWORD2, FIRST_NAME2, LAST_NAME2, EMAIL2, institution1);

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
}