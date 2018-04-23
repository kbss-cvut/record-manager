package cz.cvut.kbss.study.persistence.dao;

import cz.cvut.kbss.study.environment.generator.Generator;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.persistence.BaseDaoTestRunner;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class BaseDaoTest extends BaseDaoTestRunner{

    @Autowired
    private InstitutionDao institutionDao; // We're using one of the DAO implementations for the basic tests

    @Test
    public void findAllReturnsAllExistingInstances() {
        final List<Institution> institutions = new ArrayList<>();
        final Institution i1 = Generator.generateInstitution();
        institutions.add(i1);
        final Institution i2 = Generator.generateInstitution();
        institutions.add(i2);
        institutionDao.persist(institutions);

        final List<Institution> result = institutionDao.findAll();
        assertEquals(institutions.size(), result.size());
        for (Institution i : institutions) {
            final Institution matching = result.stream().filter(pr -> i.getUri().equals(pr.getUri())).findFirst().get();
            assertNotNull(matching);
        }
    }

    @Test
    public void removeInstitution() throws Exception {
        final Institution institution = Generator.generateInstitution();
        institutionDao.persist(institution);

        Institution i1 = institutionDao.findByName(institution.getName());
        assertNotNull(i1);

        institutionDao.remove(i1);
        Institution i2 = institutionDao.findByName(institution.getName());
        assertNull(i2);
    }


    @Test
    public void removeListOfInstitutions() {
        final List<Institution> institutions = new ArrayList<>();
        final Institution i1 = Generator.generateInstitution();
        institutions.add(i1);
        final Institution i2 = Generator.generateInstitution();
        institutions.add(i2);
        institutionDao.persist(institutions);

        final List<Institution> foundInstitutions = institutionDao.findAll();
        assertEquals(institutions.size(), foundInstitutions.size());

        institutionDao.remove(institutions);
        final List<Institution> result = institutionDao.findAll();
        assertEquals(0, result.size());
    }

    @Test
    public void updateInstitution() throws Exception {
        final Institution institution = Generator.generateInstitution();
        institutionDao.persist(institution);

        Institution i1 = institutionDao.findByName(institution.getName());
        assertNotNull(i1);
        i1.setName("Random Gynecology");
        institutionDao.update(i1);

        Institution i2 = institutionDao.findByName(i1.getName());
        assertEquals(institution.getUri(),i2.getUri());
    }
}
