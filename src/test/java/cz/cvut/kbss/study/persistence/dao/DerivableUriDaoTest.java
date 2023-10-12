package cz.cvut.kbss.study.persistence.dao;

import cz.cvut.kbss.study.environment.generator.Generator;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.persistence.BaseDaoTestRunner;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DerivableUriDaoTest extends BaseDaoTestRunner {

    @Autowired
    private UserDao userDao; // We're using one of the DAO implementations for the basic tests

    @Autowired
    private InstitutionDao institutionDao;

    @Test
    public void persistedInstanceHasGeneratedUri(){
        final Institution institution = Generator.generateInstitution();
        User user = Generator.generateUser(institution);

        institutionDao.persist(institution);
        userDao.persist(user);

        user = userDao.findByUsername(user.getUsername());
        assertNotNull(user.getUri());
    }
}
