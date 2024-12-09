package cz.cvut.kbss.study.service;

import cz.cvut.kbss.study.environment.TransactionalTestRunner;
import cz.cvut.kbss.study.environment.config.TestPersistenceConfig;
import cz.cvut.kbss.study.environment.config.TestServiceConfig;
import cz.cvut.kbss.study.environment.generator.Generator;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.persistence.ConfigDataApplicationContextInitializer;
import cz.cvut.kbss.study.persistence.dao.InstitutionDao;
import cz.cvut.kbss.study.persistence.dao.UserDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestServiceConfig.class, TestPersistenceConfig.class}, initializers = {ConfigDataApplicationContextInitializer.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public abstract class BaseServiceTestRunner extends TransactionalTestRunner {

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

    @BeforeEach
    public void setUp() throws Exception {
        Institution institution = Generator.generateInstitution();
        user = Generator.getUser(USERNAME, PASSWORD, "John", "Grant", EMAIL, institution);
        transactional(() -> {
            institutionDao.persist(institution);
            if (userDao.findByUsername(user.getUsername()) == null) {
                user.encodePassword(passwordEncoder);
                userDao.persist(user);
            }
        });
    }
}
