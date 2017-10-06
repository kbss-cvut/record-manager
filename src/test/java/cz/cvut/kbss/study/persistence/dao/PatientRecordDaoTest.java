package cz.cvut.kbss.study.persistence.dao;

import cz.cvut.kbss.study.dto.PatientRecordSummaryDto;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.environment.generator.Generator;
import cz.cvut.kbss.study.model.PatientRecord;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.persistence.BaseDaoTestRunner;
import java.util.List;
import java.util.UUID;
import static junit.framework.TestCase.assertEquals;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class PatientRecordDaoTest extends BaseDaoTestRunner {

    @Autowired
    private PatientRecordDao patienRecordDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private InstitutionDao institutionDao;

    public static final String USERNAME = "robert@plant.org";
    public static final String PASSWORD = "plant48";


    @Test
    @Ignore
    public void getRecordSummariesByInstitutionReturnsNullWhenNoMatchedFound() throws Exception {
    }

    @Test
    public void getRecordSummariesByInstitutionReturnsMatchingSummaries() throws Exception {

        userDao.persist(getUser());
        User user = userDao.findByUsername(USERNAME);

        Institution institution = generateInstitution();
        Institution institutionOther = generateInstitution();

        institutionDao.persist(institution);
        institutionDao.persist(institutionOther);


        PatientRecord record1 = generatePatientRecord(user, institution);
        PatientRecord record2 = generatePatientRecord(user, institution);
        PatientRecord recordOther = generatePatientRecord(user, institutionOther);

        patienRecordDao.persist(record1);
        patienRecordDao.persist(record2);
        patienRecordDao.persist(recordOther);

        List<PatientRecordSummaryDto> summaries = patienRecordDao.getRecordSummaries(institution);


        assertEquals(2, summaries.size());
        assertEquals(1, summaries.stream().filter(rs -> record1.getUri().equals(rs.getRecordUri())).count());
        assertEquals(1, summaries.stream().filter(rs -> record2.getUri().equals(rs.getRecordUri())).count());

    }

    private Institution generateInstitution() {
        final Institution org = new Institution();
        org.setName(UUID.randomUUID().toString());
        org.setUri(Generator.generateUri());
        return org;
    }

    public static User getUser() {
        final User person = new User();
        person.setFirstName("Robert");
        person.setLastName("Plant");
        person.setUsername(USERNAME);
        person.setPassword(PASSWORD);
        return person;
    }

    private PatientRecord generatePatientRecord(User author, Institution institutionWhereTreated) {
        final PatientRecord rec = new PatientRecord();
        rec.setAuthor(author);
        rec.setLocalName("local name -- " + Integer.toString(Generator.randomInt()));
        rec.setUri(Generator.generateUri());
        rec.setInstitution(institutionWhereTreated);
        return rec;
    }

}