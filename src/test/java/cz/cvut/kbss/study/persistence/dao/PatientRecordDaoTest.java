package cz.cvut.kbss.study.persistence.dao;

import cz.cvut.kbss.study.dto.PatientRecordDto;
import cz.cvut.kbss.study.environment.generator.Generator;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.PatientRecord;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.persistence.BaseDaoTestRunner;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PatientRecordDaoTest extends BaseDaoTestRunner {

    @Autowired
    private PatientRecordDao patientRecordDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private InstitutionDao institutionDao;

    @Test
    public void findByInstitutionReturnsMatchingRecords() {
        Institution institution = Generator.generateInstitution();
        Institution institutionOther = Generator.generateInstitution();
        User user1 = Generator.generateUser(institution);
        User user2 = Generator.generateUser(institutionOther);
        PatientRecord record1 = Generator.generatePatientRecord(user1);
        PatientRecord record2 = Generator.generatePatientRecord(user1);
        PatientRecord recordOther = Generator.generatePatientRecord(user2);

        transactional(() -> {
            institutionDao.persist(institution);
            institutionDao.persist(institutionOther);
            userDao.persist(user1);
            userDao.persist(user2);
            patientRecordDao.persist(record1);
            patientRecordDao.persist(record2);
            patientRecordDao.persist(recordOther);
        });

        List<PatientRecordDto> records = patientRecordDao.findByInstitution(institution);

        assertEquals(2, records.size());
        assertEquals(1, records.stream().filter(rs -> record1.getUri().equals(rs.getUri())).count());
        assertEquals(1, records.stream().filter(rs -> record2.getUri().equals(rs.getUri())).count());
    }

    @Test
    public void findAllRecordsReturnAllRecords() {
        Institution institution1 = Generator.generateInstitution();
        Institution institution2 = Generator.generateInstitution();
        User user1 = Generator.generateUser(institution1);
        User user2 = Generator.generateUser(institution2);
        PatientRecord record1 = Generator.generatePatientRecord(user1);
        PatientRecord record2 = Generator.generatePatientRecord(user1);
        PatientRecord record3 = Generator.generatePatientRecord(user2);

        transactional(() -> {
            institutionDao.persist(institution1);
            institutionDao.persist(institution2);
            userDao.persist(user1);
            userDao.persist(user2);
            patientRecordDao.persist(record1);
            patientRecordDao.persist(record2);
            patientRecordDao.persist(record3);
        });

        List<PatientRecordDto> records = patientRecordDao.findAllRecords();

        assertEquals(3, records.size());
    }

    @Test
    public void getNumberOfProcessedRecords() {
        Institution institution = Generator.generateInstitution();
        User user = Generator.generateUser(institution);
        PatientRecord record1 = Generator.generatePatientRecord(user);
        PatientRecord record2 = Generator.generatePatientRecord(user);
        transactional(() -> {
            institutionDao.persist(institution);
            userDao.persist(user);
            patientRecordDao.persist(record1);
            patientRecordDao.persist(record2);
        });

        int numberOfProcessedRecords = patientRecordDao.getNumberOfProcessedRecords();

        assertEquals(2, numberOfProcessedRecords);
    }

    @Test
    public void findByAuthorReturnsMatchingRecords() {
        Institution institution = Generator.generateInstitution();

        User user1 = Generator.generateUser(institution);
        User user2 = Generator.generateUser(institution);
        userDao.persist(user1);
        userDao.persist(user2);

        PatientRecord record1 = Generator.generatePatientRecord(user1);
        PatientRecord record2 = Generator.generatePatientRecord(user1);
        PatientRecord record3 = Generator.generatePatientRecord(user2);

        transactional(() -> {
            institutionDao.persist(institution);
            userDao.persist(user1);
            userDao.persist(user2);
            patientRecordDao.persist(record1);
            patientRecordDao.persist(record2);
            patientRecordDao.persist(record3);
        });

        List<PatientRecord> records1 = patientRecordDao.findByAuthor(user1);
        List<PatientRecord> records2 = patientRecordDao.findByAuthor(user2);

        assertEquals(2, records1.size());
        assertEquals(1, records2.size());
    }
}