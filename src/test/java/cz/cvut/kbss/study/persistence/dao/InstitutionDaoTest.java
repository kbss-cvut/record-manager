package cz.cvut.kbss.study.persistence.dao;

import cz.cvut.kbss.study.environment.generator.Generator;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.persistence.BaseDaoTestRunner;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InstitutionDaoTest extends BaseDaoTestRunner {

    @Autowired
    private InstitutionDao institutionDao;

    @Test
    public void getInstitutionByName() throws Exception {
        Institution institution1 = Generator.generateInstitution();
        Institution institution2 = Generator.generateInstitution();

        institutionDao.persist(institution1);
        institutionDao.persist(institution2);

        assertEquals(institution1.getUri(), institutionDao.findByName(institution1.getName()).getUri());
    }
}