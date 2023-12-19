package cz.cvut.kbss.study.service.repository;

import cz.cvut.kbss.study.dto.RecordImportResult;
import cz.cvut.kbss.study.environment.generator.Generator;
import cz.cvut.kbss.study.environment.util.Environment;
import cz.cvut.kbss.study.model.PatientRecord;
import cz.cvut.kbss.study.model.RecordPhase;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.persistence.dao.PatientRecordDao;
import cz.cvut.kbss.study.service.security.SecurityUtils;
import cz.cvut.kbss.study.util.IdentificationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryPatientRecordServiceTest {

    @Mock
    private PatientRecordDao recordDao;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private RepositoryPatientRecordService sut;

    private User user;

    @BeforeEach
    void setUp() {
        this.user = Generator.generateUser(Generator.generateInstitution());
        Environment.setCurrentUser(user);
        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(recordDao.exists(any())).thenReturn(false);
    }

    @Test
    void importRecordsSetsCurrentUserAsAuthorAndImportsSpecifiedRecords() {
        final List<PatientRecord> toImport =
                List.of(Generator.generatePatientRecord(user), Generator.generatePatientRecord(user));
        final User originalAuthor = Generator.generateUser(Generator.generateInstitution());
        toImport.forEach(r -> {
            // Simulate that the records existed in another deployment from which they are imported
            r.setKey(IdentificationUtils.generateKey());
            r.setDateCreated(new Date(System.currentTimeMillis() - 10000L));
            r.setAuthor(originalAuthor);
        });

        final RecordImportResult result = sut.importRecords(toImport);
        assertEquals(toImport.size(), result.getTotalCount());
        assertEquals(toImport.size(), result.getImportedCount());
        assertThat(result.getErrors(), anyOf(nullValue(), empty()));
        final ArgumentCaptor<PatientRecord> captor = ArgumentCaptor.forClass(PatientRecord.class);
        verify(recordDao, times(toImport.size())).persist(captor.capture());
        final List<PatientRecord> imported = captor.getAllValues();
        assertEquals(toImport.size(), imported.size());
        for (int i = 0; i < toImport.size(); i++) {
            assertEquals(toImport.get(i).getUri(), imported.get(i).getUri());
            assertEquals(toImport.get(i).getKey(), imported.get(i).getKey());
            assertEquals(user, imported.get(i).getAuthor());
            assertThat(imported.get(i).getDateCreated().getTime(), greaterThan(System.currentTimeMillis() - 1000L));
        }
    }

    @Test
    void importRecordsSkipsImportingRecordsThatAlreadyExist() {
        final List<PatientRecord> toImport =
                List.of(Generator.generatePatientRecord(user), Generator.generatePatientRecord(user));
        final User originalAuthor = Generator.generateUser(Generator.generateInstitution());
        final PatientRecord existing = toImport.get(Generator.randomIndex(toImport));
        when(recordDao.exists(existing.getUri())).thenReturn(true);
        toImport.forEach(r -> {
            // Simulate that the records existed in another deployment from which they are imported
            r.setKey(IdentificationUtils.generateKey());
            r.setDateCreated(new Date(System.currentTimeMillis() - 10000L));
            r.setAuthor(originalAuthor);
        });

        final RecordImportResult result = sut.importRecords(toImport);
        assertEquals(toImport.size(), result.getTotalCount());
        assertEquals(toImport.size() - 1, result.getImportedCount());
        assertEquals(1, result.getErrors().size());
        toImport.forEach(r -> verify(recordDao).exists(r.getUri()));
    }

    @Test
    void importRecordsWithPhaseSetsSpecifiedPhaseToAllRecords() {
        final List<PatientRecord> toImport =
                List.of(Generator.generatePatientRecord(user), Generator.generatePatientRecord(user));
        final User originalAuthor = Generator.generateUser(Generator.generateInstitution());
        final RecordPhase targetPhase = RecordPhase.values()[Generator.randomInt(0, RecordPhase.values().length)];
        toImport.forEach(r -> {
            // Simulate that the records existed in another deployment from which they are imported
            r.setKey(IdentificationUtils.generateKey());
            r.setDateCreated(new Date(System.currentTimeMillis() - 10000L));
            r.setAuthor(originalAuthor);
        });

        sut.importRecords(toImport, targetPhase);
        final ArgumentCaptor<PatientRecord> captor = ArgumentCaptor.forClass(PatientRecord.class);
        verify(recordDao, times(toImport.size())).persist(captor.capture());
        captor.getAllValues().forEach(r -> assertEquals(targetPhase, r.getPhase()));
    }
}