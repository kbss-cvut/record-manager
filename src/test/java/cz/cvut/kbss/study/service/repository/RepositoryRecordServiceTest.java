package cz.cvut.kbss.study.service.repository;

import cz.cvut.kbss.study.dto.RecordImportResult;
import cz.cvut.kbss.study.environment.generator.Generator;
import cz.cvut.kbss.study.environment.util.Environment;
import cz.cvut.kbss.study.exception.RecordAuthorNotFoundException;
import cz.cvut.kbss.study.model.*;
import cz.cvut.kbss.study.model.Record;
import cz.cvut.kbss.study.persistence.dao.RecordDao;
import cz.cvut.kbss.study.service.BaseServiceTestRunner;
import cz.cvut.kbss.study.service.UserService;
import cz.cvut.kbss.study.service.security.SecurityUtils;
import cz.cvut.kbss.study.util.IdentificationUtils;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

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


class RepositoryRecordServiceTest extends BaseServiceTestRunner {

    @InjectMocks
    private RepositoryRecordService sut;

    @Mock
    private RecordDao recordDao;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private UserService userService;
    ;

    @Test
    void importRecordsSetsCurrentUserAsAuthorWhenTheyAreRegularUserAndImportsSpecifiedRecords() {
        Environment.setCurrentUser(user);
        when(securityUtils.getCurrentUser()).thenReturn(user);
        final User originalAuthor = Generator.generateUser(Generator.generateInstitution(), this.userRoleGroup);
        final List<Record> toImport = generateRecordsToImport(originalAuthor);
        when(recordDao.exists(any())).thenReturn(false);

        final RecordImportResult result = sut.importRecords(toImport);
        assertEquals(toImport.size(), result.getTotalCount());
        assertEquals(toImport.size(), result.getImportedCount());
        assertThat(result.getErrors(), anyOf(nullValue(), empty()));
        final ArgumentCaptor<Record> captor = ArgumentCaptor.forClass(Record.class);
        verify(recordDao, times(toImport.size())).persist(captor.capture());
        final List<Record> imported = captor.getAllValues();
        assertEquals(toImport.size(), imported.size());
        for (int i = 0; i < toImport.size(); i++) {
            assertEquals(toImport.get(i).getUri(), imported.get(i).getUri());
            assertEquals(toImport.get(i).getKey(), imported.get(i).getKey());
            assertEquals(user, imported.get(i).getAuthor());
            assertThat(imported.get(i).getDateCreated().getTime(), greaterThan(System.currentTimeMillis() - 1000L));
        }
    }

    private List<Record> generateRecordsToImport(User originalAuthor) {
        final List<Record> toImport =
                List.of(Generator.generateRecord(originalAuthor), Generator.generateRecord(originalAuthor));
        toImport.forEach(r -> {
            // Simulate that the records existed in another deployment from which they are imported
            r.setKey(IdentificationUtils.generateKey());
            r.setDateCreated(new Date(System.currentTimeMillis() - 10000L));
        });
        return toImport;
    }

    @Test
    void importRecordsRetainsRecordProvenanceDataWhenCurrentUserIsAdmin() {
        final User originalAuthor = Generator.generateUser(Generator.generateInstitution(), this.adminRoleGroup);
        final List<Record> toImport = generateRecordsToImport(originalAuthor);
        Environment.setCurrentUser(this.admin);
        when(securityUtils.getCurrentUser()).thenReturn(admin);
        when(userService.exists(originalAuthor.getUri())).thenReturn(true);
        when(recordDao.exists(any())).thenReturn(false);

        sut.importRecords(toImport);
        final ArgumentCaptor<Record> captor = ArgumentCaptor.forClass(Record.class);
        verify(recordDao, times(toImport.size())).persist(captor.capture());
        final List<Record> imported = captor.getAllValues();
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
        Environment.setCurrentUser(admin);
        when(securityUtils.getCurrentUser()).thenReturn(admin);
        final User originalAuthor = Generator.generateUser(Generator.generateInstitution(), this.adminRoleGroup);
        final List<Record> toImport = generateRecordsToImport(originalAuthor);
        when(userService.exists(originalAuthor.getUri())).thenReturn(false);
        assertThrows(RecordAuthorNotFoundException.class, () -> sut.importRecords(toImport));
    }

    @Test
    void importRecordsSkipsImportingRecordsThatAlreadyExist() {
        Environment.setCurrentUser(admin);
        when(securityUtils.getCurrentUser()).thenReturn(admin);

        final User originalAuthor = Generator.generateUser(Generator.generateInstitution(), this.userRoleGroup);
        final List<Record> toImport = generateRecordsToImport(originalAuthor);
        final Record existing = toImport.get(Generator.randomIndex(toImport));
        when(recordDao.exists(any(URI.class))).thenReturn(false);
        when(recordDao.exists(existing.getUri())).thenReturn(true);
        when(userService.exists(originalAuthor.getUri())).thenReturn(true);

        final RecordImportResult result = sut.importRecords(toImport);
        assertEquals(toImport.size(), result.getTotalCount());
        assertEquals(toImport.size() - 1, result.getImportedCount());
        assertEquals(1, result.getErrors().size());
        toImport.forEach(r -> verify(recordDao).exists(r.getUri()));
    }

    @Test
    void importRecordsWithPhaseSetsSpecifiedPhaseToAllRecords() {
        Environment.setCurrentUser(admin);
        when(securityUtils.getCurrentUser()).thenReturn(admin);

        final User originalAuthor = Generator.generateUser(Generator.generateInstitution(), this.userRoleGroup);
        final List<Record> toImport = generateRecordsToImport(originalAuthor);
        final RecordPhase targetPhase = RecordPhase.values()[Generator.randomInt(0, RecordPhase.values().length)];
        when(recordDao.exists(any())).thenReturn(false);
        when(userService.exists(originalAuthor.getUri())).thenReturn(true);

        sut.importRecords(toImport, targetPhase);
        final ArgumentCaptor<Record> captor = ArgumentCaptor.forClass(Record.class);
        verify(recordDao, times(toImport.size())).persist(captor.capture());
        captor.getAllValues().forEach(r -> assertEquals(targetPhase, r.getPhase()));
    }

    @Test
    void importRecordsSetsRecordPhaseToOpenOnAllImportedRecordsWhenCurrentUserIsRegularUser() {
        Environment.setCurrentUser(user);
        when(securityUtils.getCurrentUser()).thenReturn(user);
        final User originalAuthor = Generator.generateUser(Generator.generateInstitution(), this.userRoleGroup);
        final List<Record> toImport = generateRecordsToImport(originalAuthor);
        when(recordDao.exists(any())).thenReturn(false);
        when(userService.exists(originalAuthor.getUri())).thenReturn(true);

        sut.importRecords(toImport);
        final ArgumentCaptor<Record> captor = ArgumentCaptor.forClass(Record.class);
        verify(recordDao, times(toImport.size())).persist(captor.capture());
        captor.getAllValues().forEach(r -> assertEquals(RecordPhase.open, r.getPhase()));
    }
}