package cz.cvut.kbss.study.persistence.dao;

import cz.cvut.kbss.study.dto.PatientRecordDto;
import cz.cvut.kbss.study.environment.generator.Generator;
import cz.cvut.kbss.study.model.Institution;
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

    @Test
    public void findByInstitutionReturnsMatchingRecords() throws Exception {
        Institution institution = Generator.generateInstitution();
        Institution institutionOther = Generator.generateInstitution();
        institutionDao.persist(institution);
        institutionDao.persist(institutionOther);

        User user1 = Generator.generateUser(institution);
        userDao.persist(user1);
        user1 = userDao.findByUsername(user1.getUsername());
        User user2 = Generator.generateUser(institutionOther);
        userDao.persist(user2);
        user2 = userDao.findByUsername(user2.getUsername());

        PatientRecord record1 = Generator.generatePatientRecord(user1);
        PatientRecord record2 = Generator.generatePatientRecord(user1);
        PatientRecord recordOther = Generator.generatePatientRecord(user2);

        patienRecordDao.persist(record1);
        patienRecordDao.persist(record2);
        patienRecordDao.persist(recordOther);

        List<PatientRecordDto> records = patienRecordDao.findByInstitution(institution);

        assertEquals(2, records.size());
        assertEquals(1, records.stream().filter(rs -> record1.getUri().equals(rs.getUri())).count());
        assertEquals(1, records.stream().filter(rs -> record2.getUri().equals(rs.getUri())).count());
    }

    @Test
    public void findAllRecordsReturnAllRecords() throws Exception {
        Institution institution1 = Generator.generateInstitution();
        Institution institution2 = Generator.generateInstitution();
        institutionDao.persist(institution1);
        institutionDao.persist(institution2);

        User user1 = Generator.generateUser(institution1);
        User user2 = Generator.generateUser(institution2);
        userDao.persist(user1);
        userDao.persist(user2);
        user1 = userDao.findByUsername(user1.getUsername());
        user2 = userDao.findByUsername(user2.getUsername());

        PatientRecord record1 = Generator.generatePatientRecord(user1);
        PatientRecord record2 = Generator.generatePatientRecord(user1);
        PatientRecord record3 = Generator.generatePatientRecord(user2);

        patienRecordDao.persist(record1);
        patienRecordDao.persist(record2);
        patienRecordDao.persist(record3);

        List<PatientRecordDto> records = patienRecordDao.findAllRecords();

        assertEquals(3, records.size());
    }

    @Test
    public void getNumberOfProcessedRecords() throws Exception {
        Institution institution = Generator.generateInstitution();
        institutionDao.persist(institution);

        User user = Generator.generateUser(institution);
        userDao.persist(user);
        user = userDao.findByUsername(user.getUsername());

        PatientRecord record1 = Generator.generatePatientRecord(user);
        PatientRecord record2 = Generator.generatePatientRecord(user);

        patienRecordDao.persist(record1);
        patienRecordDao.persist(record2);

        int numberOfProcessedRecords = patienRecordDao.getNumberOfProcessedRecords();

        assertEquals(2, numberOfProcessedRecords);
    }

    @Test
    public void findByAuthorReturnsMatchingRecords() throws Exception {
        Institution institution = Generator.generateInstitution();
        institutionDao.persist(institution);

        User user1 = Generator.generateUser(institution);
        User user2 = Generator.generateUser(institution);
        userDao.persist(user1);
        userDao.persist(user2);
        user1 = userDao.findByUsername(user1.getUsername());
        user2 = userDao.findByUsername(user2.getUsername());

        PatientRecord record1 = Generator.generatePatientRecord(user1);
        PatientRecord record2 = Generator.generatePatientRecord(user1);
        PatientRecord record3 = Generator.generatePatientRecord(user2);

        patienRecordDao.persist(record1);
        patienRecordDao.persist(record2);
        patienRecordDao.persist(record3);

        List<PatientRecord> records1 = patienRecordDao.findByAuthor(user1);
        List<PatientRecord> records2 = patienRecordDao.findByAuthor(user2);

        assertEquals(2, records1.size());
        assertEquals(1, records2.size());
    }
}