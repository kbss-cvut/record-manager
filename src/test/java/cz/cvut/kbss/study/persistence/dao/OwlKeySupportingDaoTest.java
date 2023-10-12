package cz.cvut.kbss.study.persistence.dao;

import cz.cvut.kbss.study.environment.generator.Generator;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.persistence.BaseDaoTestRunner;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class OwlKeySupportingDaoTest extends BaseDaoTestRunner {
    @Autowired
    private InstitutionDao institutionDao; // We're using one of the DAO implementations for the basic tests

    @Test
    public void persistedInstanceHasGeneratedKey() {
        final Institution institution = Generator.generateInstitution();
        transactional(() -> institutionDao.persist(institution));
        assertNotNull(institution.getKey());
    }

    @Test
    public void getInstitutionByKey() {
        final Institution institution = Generator.generateInstitution();
        transactional(() -> institutionDao.persist(institution));

        assertNotNull(institutionDao.findByKey(institution.getKey()));
    }
}
