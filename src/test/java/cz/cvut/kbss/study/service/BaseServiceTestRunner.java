package cz.cvut.kbss.study.service;

import cz.cvut.kbss.study.config.ServiceConfig;
import cz.cvut.kbss.study.environment.config.TestPersistenceConfig;

import cz.cvut.kbss.study.environment.generator.Generator;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.persistence.dao.InstitutionDao;
import cz.cvut.kbss.study.persistence.dao.UserDao;
import org.junit.Before;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ServiceConfig.class, TestPersistenceConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class BaseServiceTestRunner {

    @Autowired
    private UserDao userDao;

    @Autowired
    private InstitutionDao institutionDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    protected User user;

    public static final String USERNAME = "halsey";
    public static final String PASSWORD = "john117";
    public static final String EMAIL = "john117@gmail.com";

    @Before
    public void setUp() throws Exception {
        Institution institution = Generator.generateInstitution();
        institutionDao.persist(institution);

        user = Generator.getUser(USERNAME, PASSWORD, "John", "Grant", EMAIL, institution);
        if (userDao.findByUsername(user.getUsername()) == null) {
            user.encodePassword(passwordEncoder);
            userDao.persist(user);
        }
    }
}
