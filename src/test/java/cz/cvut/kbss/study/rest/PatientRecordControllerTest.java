package cz.cvut.kbss.study.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cvut.kbss.study.dto.PatientRecordDto;
import cz.cvut.kbss.study.dto.RecordImportResult;
import cz.cvut.kbss.study.environment.generator.Generator;
import cz.cvut.kbss.study.environment.util.Environment;
import cz.cvut.kbss.study.exception.RecordAuthorNotFoundException;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.PatientRecord;
import cz.cvut.kbss.study.model.RecordPhase;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.persistence.dao.util.RecordFilterParams;
import cz.cvut.kbss.study.persistence.dao.util.RecordSort;
import cz.cvut.kbss.study.rest.event.PaginatedResultRetrievedEvent;
import cz.cvut.kbss.study.rest.util.RestUtils;
import cz.cvut.kbss.study.service.PatientRecordService;
import cz.cvut.kbss.study.service.UserService;
import cz.cvut.kbss.study.util.Constants;
import cz.cvut.kbss.study.util.IdentificationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static cz.cvut.kbss.study.environment.util.ContainsSameEntities.containsSameEntities;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class PatientRecordControllerTest extends BaseControllerTestRunner {

    @Mock
    private PatientRecordService patientRecordServiceMock;

    @Mock
    private ApplicationEventPublisher eventPublisherMock;

    @Mock
    private UserService userService;

    @Spy
    private ObjectMapper objectMapper = Environment.getObjectMapper();

    @InjectMocks
    private PatientRecordController controller;

    private User user;

    @BeforeEach
    public void setUp() {
        super.setUp(controller);
        Institution institution = Generator.generateInstitution();
        institution.setKey(IdentificationUtils.generateKey());
        this.user = Generator.generateUser(institution);
        Environment.setCurrentUser(user);
    }

    @Test
    public void getRecordThrowsNotFoundWhenReportIsNotFound() throws Exception {
        final String key = "12345";
        when(patientRecordServiceMock.findByKey(key)).thenReturn(null);

        final MvcResult result = mockMvc.perform(get("/records/" + key)).andReturn();
        assertEquals(HttpStatus.NOT_FOUND, HttpStatus.valueOf(result.getResponse().getStatus()));
        verify(patientRecordServiceMock).findByKey(key);
    }

    @Test
    public void getRecordReturnsFoundRecord() throws Exception {
        final String key = "12345";
        PatientRecord patientRecord = Generator.generatePatientRecord(user);
        when(patientRecordServiceMock.findByKey(key)).thenReturn(patientRecord);

        final MvcResult result = mockMvc.perform(get("/records/" + key)).andReturn();
        assertEquals(HttpStatus.OK, HttpStatus.valueOf(result.getResponse().getStatus()));
        final PatientRecord res =
                objectMapper.readValue(result.getResponse().getContentAsString(), PatientRecord.class);
        assertEquals(res.getUri(), patientRecord.getUri());
        verify(patientRecordServiceMock).findByKey(key);
    }

    @Test
    public void getRecordsReturnsEmptyListWhenNoReportsAreFound() throws Exception {
        when(patientRecordServiceMock.findAll(any(RecordFilterParams.class), any(Pageable.class))).thenReturn(
                Page.empty());

        final MvcResult result = mockMvc.perform(get("/records/")).andReturn();

        assertEquals(HttpStatus.OK, HttpStatus.valueOf(result.getResponse().getStatus()));
        final List<PatientRecord> body = objectMapper.readValue(result.getResponse().getContentAsString(),
                                                                new TypeReference<>() {
                                                                });
        assertTrue(body.isEmpty());
    }

    @Test
    public void getRecordsReturnsAllRecords() throws Exception {
        Institution institution = Generator.generateInstitution();

        User user1 = Generator.generateUser(institution);
        User user2 = Generator.generateUser(institution);

        List<PatientRecordDto> records =
                List.of(Generator.generatePatientRecordDto(user1), Generator.generatePatientRecordDto(user1),
                        Generator.generatePatientRecordDto(user2));

        when(patientRecordServiceMock.findAll(any(RecordFilterParams.class), any(Pageable.class))).thenReturn(
                new PageImpl<>(records));

        final MvcResult result = mockMvc.perform(get("/records")).andReturn();

        assertEquals(HttpStatus.OK, HttpStatus.valueOf(result.getResponse().getStatus()));
        final List<PatientRecordDto> body = objectMapper.readValue(result.getResponse().getContentAsString(),
                                                                   new TypeReference<>() {
                                                                   });
        assertEquals(3, body.size());
        verify(patientRecordServiceMock).findAll(new RecordFilterParams(), Pageable.unpaged());
    }

    @Test
    public void findByInstitutionReturnsRecords() throws Exception {
        final String key = "12345";

        Institution institution = Generator.generateInstitution();
        institution.setKey(key);

        User user1 = Generator.generateUser(institution);
        User user2 = Generator.generateUser(institution);

        PatientRecordDto record1 = Generator.generatePatientRecordDto(user1);
        PatientRecordDto record2 = Generator.generatePatientRecordDto(user2);
        List<PatientRecordDto> records = new ArrayList<>();
        records.add(record1);
        records.add(record2);

        when(patientRecordServiceMock.findAll(any(RecordFilterParams.class), any(Pageable.class))).thenReturn(
                new PageImpl<>(records));
        System.out.println(institution.getKey());
        final MvcResult result =
                mockMvc.perform(get("/records").param("institution", institution.getKey())).andReturn();

        assertEquals(HttpStatus.OK, HttpStatus.valueOf(result.getResponse().getStatus()));
        final List<PatientRecordDto> body = objectMapper.readValue(result.getResponse().getContentAsString(),
                                                                   new TypeReference<>() {
                                                                   });
        assertEquals(2, body.size());
        verify(patientRecordServiceMock).findAll(new RecordFilterParams(institution.getKey()), Pageable.unpaged());
    }

    @Test
    public void createRecordReturnsResponseStatusCreated() throws Exception {
        PatientRecord record = Generator.generatePatientRecord(user);
        when(userService.getCurrentUser()).thenReturn(user);

        final MvcResult result = mockMvc.perform(post("/records").content(toJson(record))
                                                                 .contentType(MediaType.APPLICATION_JSON_VALUE))
                                        .andReturn();

        assertEquals(HttpStatus.CREATED, HttpStatus.valueOf(result.getResponse().getStatus()));
    }

    @Test
    public void createRecordWithoutInstitutionReturnsResponseStatusBadRequest() throws Exception {
        user.setInstitution(null);

        PatientRecord record = Generator.generatePatientRecord(user);

        when(userService.getCurrentUser()).thenReturn(user);

        final MvcResult result = mockMvc.perform(post("/records").content(toJson(record))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn();

        assertEquals(HttpStatus.CONFLICT, HttpStatus.valueOf(result.getResponse().getStatus()));
    }


    @Test
    public void updateRecordReturnsResponseStatusNoContent() throws Exception {
        final String key = "12345";

        PatientRecord record = Generator.generatePatientRecord(user);
        record.setKey(key);

        when(patientRecordServiceMock.findByKey(key)).thenReturn(record);

        final MvcResult result = mockMvc.perform(put("/records/" + key).content(toJson(record))
                                                                       .contentType(MediaType.APPLICATION_JSON_VALUE))
                                        .andReturn();

        assertEquals(HttpStatus.NO_CONTENT, HttpStatus.valueOf(result.getResponse().getStatus()));
        verify(patientRecordServiceMock).findByKey(key);
    }

    @Test
    public void updateRecordWithNonMatchingKeyReturnsResponseStatusBadRequest() throws Exception {
        final String key = "12345";

        PatientRecord record = Generator.generatePatientRecord(user);
        record.setKey(key);

        final MvcResult result = mockMvc.perform(put("/records/123456").content(toJson(record))
                                                                       .contentType(MediaType.APPLICATION_JSON_VALUE))
                                        .andReturn();

        assertEquals(HttpStatus.BAD_REQUEST, HttpStatus.valueOf(result.getResponse().getStatus()));
    }

    @Test
    public void updateRecordReturnsResponseStatusNotFound() throws Exception {
        final String key = "12345";

        PatientRecord record = Generator.generatePatientRecord(user);
        record.setKey(key);

        when(patientRecordServiceMock.findByKey(key)).thenReturn(null);

        final MvcResult result = mockMvc.perform(put("/records/" + key).content(toJson(record))
                                                                       .contentType(MediaType.APPLICATION_JSON_VALUE))
                                        .andReturn();

        assertEquals(HttpStatus.NOT_FOUND, HttpStatus.valueOf(result.getResponse().getStatus()));
        verify(patientRecordServiceMock).findByKey(key);
    }

    @Test
    public void deleteRecordReturnsResponseStatusNoContent() throws Exception {
        final String key = "12345";

        PatientRecord record = Generator.generatePatientRecord(user);
        record.setKey(key);

        when(patientRecordServiceMock.findByKey(key)).thenReturn(record);

        final MvcResult result = mockMvc.perform(delete("/records/12345")).andReturn();

        assertEquals(HttpStatus.NO_CONTENT, HttpStatus.valueOf(result.getResponse().getStatus()));
        verify(patientRecordServiceMock).findByKey(key);
    }

    @Test
    void exportRecordsParsesProvidedDateBoundsAndPassesThemToService() throws Exception {
        final LocalDate minDate = LocalDate.now().minusDays(35);
        final LocalDate maxDate = LocalDate.now().minusDays(5);
        final List<PatientRecord> records =
                List.of(Generator.generatePatientRecord(user), Generator.generatePatientRecord(user));
        when(patientRecordServiceMock.findAllFull(any(RecordFilterParams.class), any(
                Pageable.class))).thenReturn(new PageImpl<>(records));

        final MvcResult mvcResult = mockMvc.perform(get("/records/export")
                                                            .param("minDate", minDate.toString())
                                                            .param("maxDate", maxDate.toString()))
                                           .andReturn();
        final List<PatientRecord> result = readValue(mvcResult, new TypeReference<>() {
        });
        assertThat(result, containsSameEntities(records));
        verify(patientRecordServiceMock).findAllFull(
                new RecordFilterParams(null, minDate, maxDate, Collections.emptySet(), Collections.emptySet()), Pageable.unpaged());
    }

    @Test
    void exportRecordsUsesDefaultValuesForMinAndMaxDateWhenTheyAreNotProvidedByRequest() throws Exception {
        final List<PatientRecord> records =
                List.of(Generator.generatePatientRecord(user), Generator.generatePatientRecord(user));
        when(patientRecordServiceMock.findAllFull(any(RecordFilterParams.class), any(
                Pageable.class))).thenReturn(new PageImpl<>(records));

        final MvcResult mvcResult = mockMvc.perform(get("/records/export")).andReturn();
        final List<PatientRecord> result = readValue(mvcResult, new TypeReference<>() {
        });
        assertThat(result, containsSameEntities(records));
        verify(patientRecordServiceMock).findAllFull(new RecordFilterParams(), Pageable.unpaged());
    }

    @Test
    void exportRecordsExportsRecordsForProvidedInstitutionForSpecifiedPeriod() throws Exception {
        final LocalDate minDate = LocalDate.now().minusDays(35);
        final LocalDate maxDate = LocalDate.now().minusDays(5);
        final List<PatientRecord> records =
                List.of(Generator.generatePatientRecord(user), Generator.generatePatientRecord(user));
        when(patientRecordServiceMock.findAllFull(any(RecordFilterParams.class), any(
                Pageable.class))).thenReturn(new PageImpl<>(records));

        final MvcResult mvcResult = mockMvc.perform(get("/records/export")
                                                            .param("minDate", minDate.toString())
                                                            .param("maxDate", maxDate.toString())
                                                            .param("institution", user.getInstitution().getKey()))
                                           .andReturn();
        final List<PatientRecord> result = readValue(mvcResult, new TypeReference<>() {
        });
        assertThat(result, containsSameEntities(records));
        verify(patientRecordServiceMock).findAllFull(
                new RecordFilterParams(user.getInstitution().getKey(), minDate, maxDate, Collections.emptySet(), Collections.emptySet()),
                Pageable.unpaged());
    }


    @Test
    void importRecordsJsonImportsSpecifiedRecordsAndReturnsImportResult() throws Exception {
        final List<PatientRecord> records =
                List.of(Generator.generatePatientRecord(user), Generator.generatePatientRecord(user));
        final RecordImportResult importResult = new RecordImportResult(records.size());
        importResult.setImportedCount(records.size());

        when(patientRecordServiceMock.importRecords(anyList())).thenReturn(importResult);

        MockMultipartFile file = new MockMultipartFile("file", "records.json",
                MediaType.MULTIPART_FORM_DATA_VALUE, toJson(records).getBytes());

        final MvcResult mvcResult = mockMvc.perform(
                multipart("/records/import")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
        ).andReturn();

        final RecordImportResult result = readValue(mvcResult, RecordImportResult.class);
        assertEquals(importResult.getTotalCount(), result.getTotalCount());
        assertEquals(importResult.getImportedCount(), result.getImportedCount());
        assertThat(importResult.getErrors(), anyOf(nullValue(), empty()));

        @SuppressWarnings("unchecked")
        final ArgumentCaptor<List<PatientRecord>> captor = ArgumentCaptor.forClass(List.class);
        verify(patientRecordServiceMock).importRecords(captor.capture());
        assertEquals(records.size(), captor.getValue().size());
    }


    @Test
    void importRecordsJsonImportsSpecifiedRecordsWithSpecifiedPhaseAndReturnsImportResult() throws Exception {
        final List<PatientRecord> records =
                List.of(Generator.generatePatientRecord(user), Generator.generatePatientRecord(user));
        final RecordImportResult importResult = new RecordImportResult(records.size());
        importResult.setImportedCount(records.size());
        final RecordPhase targetPhase = RecordPhase.values()[Generator.randomInt(0, RecordPhase.values().length)];
        when(patientRecordServiceMock.importRecords(anyList(), any(RecordPhase.class))).thenReturn(importResult);

        MockMultipartFile file = new MockMultipartFile("file", "records.json",
            MediaType.MULTIPART_FORM_DATA_VALUE, toJson(records).getBytes());

        mockMvc.perform(
            multipart("/records/import")
                .file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                .param("phase", targetPhase.getIri())
        ).andExpect(status().isOk());

        verify(patientRecordServiceMock).importRecords(anyList(), eq(targetPhase));
    }

    @Test
    void importRecordsJsonReturnsConflictWhenServiceThrowsRecordAuthorNotFound() throws Exception {
        final List<PatientRecord> records =
                List.of(Generator.generatePatientRecord(user), Generator.generatePatientRecord(user));
        when(patientRecordServiceMock.importRecords(anyList())).thenThrow(RecordAuthorNotFoundException.class);

        MockMultipartFile file = new MockMultipartFile("file", "records.json",
                MediaType.MULTIPART_FORM_DATA_VALUE, toJson(records).getBytes());

        mockMvc.perform(
                multipart("/records/import")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
        ).andExpect(status().isConflict());
    }

    @Test
    void getRecordsResolvesPagingConfigurationFromRequestParameters() throws Exception {
        final LocalDate minDate = LocalDate.now().minusDays(35);
        final LocalDate maxDate = LocalDate.now().minusDays(5);
        final int page = Generator.randomInt(0, 5);
        final int pageSize = Generator.randomInt(30, 50);
        final List<PatientRecord> records =
                List.of(Generator.generatePatientRecord(user), Generator.generatePatientRecord(user));
        when(patientRecordServiceMock.findAllFull(any(RecordFilterParams.class), any(
                Pageable.class))).thenReturn(new PageImpl<>(records));

        final MvcResult mvcResult = mockMvc.perform(get("/records/export")
                                                            .param("minDate", minDate.toString())
                                                            .param("maxDate", maxDate.toString())
                                                            .param(Constants.PAGE_PARAM, Integer.toString(page))
                                                            .param(Constants.PAGE_SIZE_PARAM,
                                                                   Integer.toString(pageSize))
                                                            .param(Constants.SORT_PARAM,
                                                                   RestUtils.SORT_DESC + RecordSort.SORT_DATE_PROPERTY))
                                           .andReturn();
        final List<PatientRecord> result = readValue(mvcResult, new TypeReference<>() {
        });
        assertThat(result, containsSameEntities(records));
        verify(patientRecordServiceMock).findAllFull(
                new RecordFilterParams(null, minDate, maxDate, Collections.emptySet(), Collections.emptySet()),
                PageRequest.of(page, pageSize, Sort.Direction.DESC, RecordSort.SORT_DATE_PROPERTY));
    }

    @Test
    void getRecordsPublishesPagingEvent() throws Exception {
        List<PatientRecordDto> records =
                List.of(Generator.generatePatientRecordDto(user), Generator.generatePatientRecordDto(user),
                        Generator.generatePatientRecordDto(user));

        final Page<PatientRecordDto> page = new PageImpl<>(records, PageRequest.of(0, 5), 3);
        when(patientRecordServiceMock.findAll(any(RecordFilterParams.class), any(Pageable.class))).thenReturn(page);
        final MvcResult result = mockMvc.perform(get("/records").queryParam(Constants.PAGE_PARAM, "0")
                                                                .queryParam(Constants.PAGE_SIZE_PARAM, "5"))
                                        .andReturn();

        assertEquals(HttpStatus.OK, HttpStatus.valueOf(result.getResponse().getStatus()));
        final List<PatientRecordDto> body = objectMapper.readValue(result.getResponse().getContentAsString(),
                                                                   new TypeReference<>() {
                                                                   });
        assertEquals(3, body.size());
        verify(patientRecordServiceMock).findAll(new RecordFilterParams(), PageRequest.of(0, 5));
        final ArgumentCaptor<PaginatedResultRetrievedEvent> captor = ArgumentCaptor.forClass(
                PaginatedResultRetrievedEvent.class);
        verify(eventPublisherMock).publishEvent(captor.capture());
        final PaginatedResultRetrievedEvent event = captor.getValue();
        assertEquals(page, event.getPage());
    }

    @Test
    void exportRecordsPublishesPagingEvent() throws Exception {
        final LocalDate minDate = LocalDate.now().minusDays(35);
        final LocalDate maxDate = LocalDate.now().minusDays(5);
        final List<PatientRecord> records =
                List.of(Generator.generatePatientRecord(user), Generator.generatePatientRecord(user));
        final Page<PatientRecord> page = new PageImpl<>(records, PageRequest.of(0, 50), 100);
        when(patientRecordServiceMock.findAllFull(any(RecordFilterParams.class), any(Pageable.class))).thenReturn(page);

        final MvcResult mvcResult = mockMvc.perform(get("/records/export")
                                                            .param("minDate", minDate.toString())
                                                            .param("maxDate", maxDate.toString())
                                                            .param(Constants.PAGE_PARAM, "0")
                                                            .param(Constants.PAGE_SIZE_PARAM, "50"))
                                           .andReturn();
        final List<PatientRecord> result = readValue(mvcResult, new TypeReference<>() {
        });
        assertThat(result, containsSameEntities(records));
        verify(patientRecordServiceMock).findAllFull(
                new RecordFilterParams(null, minDate, maxDate, Collections.emptySet(), Collections.emptySet()), PageRequest.of(0, 50));
        final ArgumentCaptor<PaginatedResultRetrievedEvent> captor =
                ArgumentCaptor.forClass(PaginatedResultRetrievedEvent.class);
        verify(eventPublisherMock).publishEvent(captor.capture());
        final PaginatedResultRetrievedEvent event = captor.getValue();
        assertEquals(page, event.getPage());
    }
}
