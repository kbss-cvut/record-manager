package cz.cvut.kbss.study.service.repository;

import cz.cvut.kbss.study.dto.RecordImportResult;
import cz.cvut.kbss.study.environment.generator.Generator;
import cz.cvut.kbss.study.environment.util.Environment;
import cz.cvut.kbss.study.exception.RecordAuthorNotFoundException;
import cz.cvut.kbss.study.model.*;
import cz.cvut.kbss.study.persistence.dao.PatientRecordDao;
import cz.cvut.kbss.study.service.UserService;
import cz.cvut.kbss.study.service.security.SecurityUtils;
import cz.cvut.kbss.study.util.IdentificationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Mock
    private UserService userService;

    @InjectMocks
    private RepositoryPatientRecordService sut;

    private User user;

    private RoleGroup roleGroupAdmin;
    private RoleGroup roleGroupUser;

    @BeforeEach
    void setUp() {
        this.roleGroupAdmin =  Generator.generateRoleGroupWithRoles(Role.administrator);
        this.roleGroupUser = Generator.generateRoleGroupWithRoles();
        this.user = Generator.generateUser(Generator.generateInstitution(), roleGroupUser);
        Environment.setCurrentUser(user);
        when(securityUtils.getCurrentUser()).thenReturn(user);
    }

    @Test
    void importRecordsSetsCurrentUserAsAuthorWhenTheyAreRegularUserAndImportsSpecifiedRecords() {
        final User originalAuthor = Generator.generateUser(Generator.generateInstitution(), this.roleGroupUser);
        final List<PatientRecord> toImport = generateRecordsToImport(originalAuthor);
        when(recordDao.exists(any())).thenReturn(false);

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

    private List<PatientRecord> generateRecordsToImport(User originalAuthor) {
        final List<PatientRecord> toImport =
                List.of(Generator.generatePatientRecord(originalAuthor), Generator.generatePatientRecord(originalAuthor));
        toImport.forEach(r -> {
            // Simulate that the records existed in another deployment from which they are imported
            r.setKey(IdentificationUtils.generateKey());
            r.setDateCreated(new Date(System.currentTimeMillis() - 10000L));
        });
        return toImport;
    }

    @Test
    void importRecordsRetainsRecordProvenanceDataWhenCurrentUserIsAdmin() {
        final User originalAuthor = Generator.generateUser(Generator.generateInstitution(), this.roleGroupAdmin);
        final List<PatientRecord> toImport = generateRecordsToImport(originalAuthor);
        Environment.setCurrentUser(user);
        user.getRoleGroup().addRole(Role.administrator);
        when(userService.exists(originalAuthor.getUri())).thenReturn(true);
        when(recordDao.exists(any())).thenReturn(false);

        sut.importRecords(toImport);
        final ArgumentCaptor<PatientRecord> captor = ArgumentCaptor.forClass(PatientRecord.class);
        verify(recordDao, times(toImport.size())).persist(captor.capture());
        final List<PatientRecord> imported = captor.getAllValues();
        assertEquals(toImport.size(), imported.size());
        for (int i = 0; i < toImport.size(); i++) {
            assertEquals(toImport.get(i).getUri(), imported.get(i).getUri());
            assertEquals(toImport.get(i).getKey(), imported.get(i).getKey());
            assertEquals(originalAuthor, imported.get(i).getAuthor());
            assertEquals(toImport.get(i).getDateCreated(), imported.get(i).getDateCreated());
            assertEquals(toImport.get(i).getPhase(), imported.get(i).getPhase());
        }
    }

    @Test
    void importRecordsThrowsRecordAuthorNotFoundExceptionWhenAdminImportsRecordsAndRecordAuthorIsNotFound() {
        final User originalAuthor = Generator.generateUser(Generator.generateInstitution(), this.roleGroupAdmin);
        final List<PatientRecord> toImport = generateRecordsToImport(originalAuthor);
        user.setRoleGroup(Generator.generateRoleGroupWithRoles(Role.administrator));
        Environment.setCurrentUser(user);

        assertThrows(RecordAuthorNotFoundException.class, () -> sut.importRecords(toImport));
    }

    @Test
    void importRecordsSkipsImportingRecordsThatAlreadyExist() {
        final User originalAuthor = Generator.generateUser(Generator.generateInstitution(), this.roleGroupUser);
        final List<PatientRecord> toImport = generateRecordsToImport(originalAuthor);
        final PatientRecord existing = toImport.get(Generator.randomIndex(toImport));
        when(recordDao.exists(any(URI.class))).thenReturn(false);
        when(recordDao.exists(existing.getUri())).thenReturn(true);

        final RecordImportResult result = sut.importRecords(toImport);
        assertEquals(toImport.size(), result.getTotalCount());
        assertEquals(toImport.size() - 1, result.getImportedCount());
        assertEquals(1, result.getErrors().size());
        toImport.forEach(r -> verify(recordDao).exists(r.getUri()));
    }

    @Test
    void importRecordsWithPhaseSetsSpecifiedPhaseToAllRecords() {
        final User originalAuthor = Generator.generateUser(Generator.generateInstitution(), this.roleGroupUser);
        final List<PatientRecord> toImport = generateRecordsToImport(originalAuthor);
        final RecordPhase targetPhase = RecordPhase.values()[Generator.randomInt(0, RecordPhase.values().length)];
        when(recordDao.exists(any())).thenReturn(false);

        sut.importRecords(toImport, targetPhase);
        final ArgumentCaptor<PatientRecord> captor = ArgumentCaptor.forClass(PatientRecord.class);
        verify(recordDao, times(toImport.size())).persist(captor.capture());
        captor.getAllValues().forEach(r -> assertEquals(targetPhase, r.getPhase()));
    }

    @Test
    void importRecordsSetsRecordPhaseToOpenOnAllImportedRecordsWhenCurrentUserIsRegularUser() {
        final User originalAuthor = Generator.generateUser(Generator.generateInstitution(), this.roleGroupUser);
        final List<PatientRecord> toImport = generateRecordsToImport(originalAuthor);
        when(recordDao.exists(any())).thenReturn(false);

        sut.importRecords(toImport);
        final ArgumentCaptor<PatientRecord> captor = ArgumentCaptor.forClass(PatientRecord.class);
        verify(recordDao, times(toImport.size())).persist(captor.capture());
        captor.getAllValues().forEach(r -> assertEquals(RecordPhase.open, r.getPhase()));
    }
}