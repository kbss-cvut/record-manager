package cz.cvut.kbss.study.service;

import cz.cvut.kbss.study.config.ServiceConfig;
import cz.cvut.kbss.study.environment.config.TestPersistenceConfig;

import cz.cvut.kbss.study.environment.util.Generator;
import cz.cvut.kbss.study.model.User;
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
    private PasswordEncoder passwordEncoder;

    protected User user;

    public static final String USERNAME = "halsey@unsc.org";
    public static final String PASSWORD = "john117";

    @Before
    public void setUp() throws Exception {
        user = Generator.getUser(USERNAME, PASSWORD, "John", "Johnie", "Johnie@gmail.com", null);
        if (userDao.findByUsername(user.getUsername()) == null) {
            user.encodePassword(passwordEncoder);
            userDao.persist(user);
        }
    }
}
