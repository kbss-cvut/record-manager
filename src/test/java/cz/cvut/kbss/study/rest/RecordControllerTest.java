package cz.cvut.kbss.study.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cvut.kbss.study.dto.RecordDto;
import cz.cvut.kbss.study.dto.RecordImportResult;
import cz.cvut.kbss.study.environment.generator.Generator;
import cz.cvut.kbss.study.environment.util.Environment;
import cz.cvut.kbss.study.exception.RecordAuthorNotFoundException;
import cz.cvut.kbss.study.model.*;
import cz.cvut.kbss.study.model.Record;
import cz.cvut.kbss.study.persistence.dao.util.RecordFilterParams;
import cz.cvut.kbss.study.persistence.dao.util.RecordSort;
import cz.cvut.kbss.study.rest.event.PaginatedResultRetrievedEvent;
import cz.cvut.kbss.study.rest.util.RestUtils;
import cz.cvut.kbss.study.service.ConfigReader;
import cz.cvut.kbss.study.service.RecordService;
import cz.cvut.kbss.study.service.UserService;
import cz.cvut.kbss.study.util.ConfigParam;
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
import java.util.*;

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
public class RecordControllerTest extends BaseControllerTestRunner {

    @Mock
    private RecordService recordServiceMock;

    @Mock
    private ApplicationEventPublisher eventPublisherMock;

    @Mock
    private ConfigReader configReaderMock;

    @Mock
    private UserService userService;

    @Spy
    private ObjectMapper objectMapper = Environment.getObjectMapper();

    @InjectMocks
    private RecordController controller;

    private User user;

    private RoleGroup adminRoleGroup;

    @BeforeEach
    public void setUp() {
        super.setUp(controller);
        Institution institution = Generator.generateInstitution();
        institution.setKey(IdentificationUtils.generateKey());
        this.adminRoleGroup = Generator.generateAdminRoleGroup();
        this.user = Generator.generateUser(institution, adminRoleGroup);
        Environment.setCurrentUser(user);
    }

    @Test
    public void getRecordThrowsNotFoundWhenReportIsNotFound() throws Exception {
        final String key = "12345";
        when(recordServiceMock.findByKey(key)).thenReturn(null);

        final MvcResult result = mockMvc.perform(get("/records/" + key)).andReturn();
        assertEquals(HttpStatus.NOT_FOUND, HttpStatus.valueOf(result.getResponse().getStatus()));
        verify(recordServiceMock).findByKey(key);
    }

    @Test
    public void  testGetUsedRecordPhases() throws Exception{

        Set<RecordPhase> phases = new HashSet<>(Arrays.asList(RecordPhase.completed, RecordPhase.valid));

        when(recordServiceMock.findUsedRecordPhases()).thenReturn(phases);

        final MvcResult result = mockMvc.perform(get("/records/used-record-phases"))
                .andReturn();
        assertEquals(HttpStatus.OK, HttpStatus.valueOf(result.getResponse().getStatus()));
        final Set<RecordPhase> body = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});
        assertEquals(phases, body);
    }

    @Test
    public void getRecordReturnsFoundRecord() throws Exception {
        final String key = "12345";
        Record record = Generator.generateRecord(user);
        when(recordServiceMock.findByKey(key)).thenReturn(record);

        final MvcResult result = mockMvc.perform(get("/records/" + key)).andReturn();
        assertEquals(HttpStatus.OK, HttpStatus.valueOf(result.getResponse().getStatus()));
        final Record res =
                objectMapper.readValue(result.getResponse().getContentAsString(), Record.class);
        assertEquals(res.getUri(), record.getUri());
        verify(recordServiceMock).findByKey(key);
    }

    @Test
    public void getRecordsReturnsEmptyListWhenNoReportsAreFound() throws Exception {
        when(recordServiceMock.findAll(any(RecordFilterParams.class), any(Pageable.class))).thenReturn(
                Page.empty());

        final MvcResult result = mockMvc.perform(get("/records/").param("institution", user.getInstitution().toString())).andReturn();

        assertEquals(HttpStatus.OK, HttpStatus.valueOf(result.getResponse().getStatus()));
        final List<Record> body = objectMapper.readValue(result.getResponse().getContentAsString(),
                                                                new TypeReference<>() {
                                                                });
        assertTrue(body.isEmpty());
    }

    @Test
    public void getRecordsReturnsAllRecords() throws Exception {
        Institution institution = Generator.generateInstitution();

        User user1 = Generator.generateUser(institution, adminRoleGroup);
        User user2 = Generator.generateUser(institution, adminRoleGroup);

        List<RecordDto> records =
                List.of(Generator.generateRecordDto(user1), Generator.generateRecordDto(user1),
                        Generator.generateRecordDto(user2));

        when(recordServiceMock.findAll(any(RecordFilterParams.class), any(Pageable.class))).thenReturn(
                new PageImpl<>(records));


        final MvcResult result = mockMvc.perform(get("/records/").param("institution", user.getInstitution().toString())).andReturn();

        assertEquals(HttpStatus.OK, HttpStatus.valueOf(result.getResponse().getStatus()));
        final List<RecordDto> body = objectMapper.readValue(result.getResponse().getContentAsString(),
                                                                   new TypeReference<>() {
                                                                   });
        assertEquals(3, body.size());
        verify(recordServiceMock).findAll(any(RecordFilterParams.class), any(Pageable.class));
    }

    @Test
    public void findByInstitutionReturnsRecords() throws Exception {
        final String key = "12345";

        Institution institution = Generator.generateInstitution();
        institution.setKey(key);

        User user1 = Generator.generateUser(institution, adminRoleGroup);
        User user2 = Generator.generateUser(institution, adminRoleGroup);

        RecordDto record1 = Generator.generateRecordDto(user1);
        RecordDto record2 = Generator.generateRecordDto(user2);
        List<RecordDto> records = new ArrayList<>();
        records.add(record1);
        records.add(record2);

        when(recordServiceMock.findAll(any(RecordFilterParams.class), any(Pageable.class))).thenReturn(
                new PageImpl<>(records));
        System.out.println(institution.getKey());
        final MvcResult result =
                mockMvc.perform(get("/records").param("institution", institution.getKey())).andReturn();

        assertEquals(HttpStatus.OK, HttpStatus.valueOf(result.getResponse().getStatus()));
        final List<RecordDto> body = objectMapper.readValue(result.getResponse().getContentAsString(),
                                                                   new TypeReference<>() {
                                                                   });
        assertEquals(2, body.size());
        verify(recordServiceMock).findAll(new RecordFilterParams(Set.of(institution.getKey())), Pageable.unpaged());
    }

    @Test
    public void createRecordReturnsResponseStatusCreated() throws Exception {
        Record record = Generator.generateRecord(user);
        when(userService.getCurrentUser()).thenReturn(user);

        final MvcResult result = mockMvc.perform(post("/records").content(toJson(record))
                                                                 .contentType(MediaType.APPLICATION_JSON_VALUE))
                                        .andReturn();

        assertEquals(HttpStatus.CREATED, HttpStatus.valueOf(result.getResponse().getStatus()));
    }

    @Test
    public void createRecordWithoutInstitutionReturnsResponseStatusBadRequest() throws Exception {
        user.setInstitution(null);

        Record record = Generator.generateRecord(user);

        when(userService.getCurrentUser()).thenReturn(user);

        final MvcResult result = mockMvc.perform(post("/records").content(toJson(record))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn();

        assertEquals(HttpStatus.CONFLICT, HttpStatus.valueOf(result.getResponse().getStatus()));
    }


    @Test
    public void updateRecordReturnsResponseStatusNoContent() throws Exception {
        final String key = "12345";

        Record record = Generator.generateRecord(user);
        record.setKey(key);

        when(recordServiceMock.findByKey(key)).thenReturn(record);

        final MvcResult result = mockMvc.perform(put("/records/" + key).content(toJson(record))
                                                                       .contentType(MediaType.APPLICATION_JSON_VALUE))
                                        .andReturn();

        assertEquals(HttpStatus.NO_CONTENT, HttpStatus.valueOf(result.getResponse().getStatus()));
        verify(recordServiceMock).findByKey(key);
    }

    @Test
    public void updateRecordWithNonMatchingKeyReturnsResponseStatusBadRequest() throws Exception {
        final String key = "12345";

        Record record = Generator.generateRecord(user);
        record.setKey(key);

        final MvcResult result = mockMvc.perform(put("/records/123456").content(toJson(record))
                                                                       .contentType(MediaType.APPLICATION_JSON_VALUE))
                                        .andReturn();

        assertEquals(HttpStatus.BAD_REQUEST, HttpStatus.valueOf(result.getResponse().getStatus()));
    }

    @Test
    public void updateRecordReturnsResponseStatusNotFound() throws Exception {
        final String key = "12345";

        Record record = Generator.generateRecord(user);
        record.setKey(key);

        when(recordServiceMock.findByKey(key)).thenReturn(null);

        final MvcResult result = mockMvc.perform(put("/records/" + key).content(toJson(record))
                                                                       .contentType(MediaType.APPLICATION_JSON_VALUE))
                                        .andReturn();

        assertEquals(HttpStatus.NOT_FOUND, HttpStatus.valueOf(result.getResponse().getStatus()));
        verify(recordServiceMock).findByKey(key);
    }

    @Test
    public void deleteRecordReturnsResponseStatusNoContent() throws Exception {
        final String key = "12345";

        Record record = Generator.generateRecord(user);
        record.setKey(key);

        when(recordServiceMock.findByKey(key)).thenReturn(record);

        final MvcResult result = mockMvc.perform(delete("/records/12345")).andReturn();

        assertEquals(HttpStatus.NO_CONTENT, HttpStatus.valueOf(result.getResponse().getStatus()));
        verify(recordServiceMock).findByKey(key);
    }

    @Test
    void exportRecordsParsesProvidedDateBoundsAndPassesThemToService() throws Exception {
        final LocalDate minDate = LocalDate.now().minusDays(35);
        final LocalDate maxDate = LocalDate.now().minusDays(5);
        final List<Record> records =
                List.of(Generator.generateRecord(user), Generator.generateRecord(user));
        when(recordServiceMock.findAllFull(any(RecordFilterParams.class), any(
                Pageable.class))).thenReturn(new PageImpl<>(records));

        final MvcResult mvcResult = mockMvc.perform(get("/records/export")
                                                            .param("minDate", minDate.toString())
                                                            .param("maxDate", maxDate.toString()))
                                           .andReturn();
        final List<Record> result = readValue(mvcResult, new TypeReference<>() {
        });
        assertThat(result, containsSameEntities(records));
        verify(recordServiceMock).findAllFull(
                new RecordFilterParams(null,Set.of(), minDate, maxDate, Collections.emptySet(), Collections.emptySet()), Pageable.unpaged());
    }

    @Test
    void exportRecordsUsesDefaultValuesForMinAndMaxDateWhenTheyAreNotProvidedByRequest() throws Exception {
        final List<Record> records =
                List.of(Generator.generateRecord(user), Generator.generateRecord(user));
        when(recordServiceMock.findAllFull(any(RecordFilterParams.class), any(
                Pageable.class))).thenReturn(new PageImpl<>(records));

        final MvcResult mvcResult = mockMvc.perform(get("/records/export")).andReturn();
        final List<Record> result = readValue(mvcResult, new TypeReference<>() {
        });
        assertThat(result, containsSameEntities(records));
        verify(recordServiceMock).findAllFull(new RecordFilterParams(), Pageable.unpaged());
    }

    @Test
    void exportRecordsExportsRecordsForProvidedInstitutionForSpecifiedPeriod() throws Exception {
        final LocalDate minDate = LocalDate.now().minusDays(35);
        final LocalDate maxDate = LocalDate.now().minusDays(5);
        final List<Record> records =
                List.of(Generator.generateRecord(user), Generator.generateRecord(user));
        when(recordServiceMock.findAllFull(any(RecordFilterParams.class), any(
                Pageable.class))).thenReturn(new PageImpl<>(records));

        final MvcResult mvcResult = mockMvc.perform(get("/records/export")
                                                            .param("minDate", minDate.toString())
                                                            .param("maxDate", maxDate.toString())
                                                            .param("institution", user.getInstitution().getKey()))
                                           .andReturn();
        final List<Record> result = readValue(mvcResult, new TypeReference<>() {
        });
        assertThat(result, containsSameEntities(records));
        verify(recordServiceMock).findAllFull(
                new RecordFilterParams(null, Set.of(user.getInstitution().getKey()), minDate, maxDate, Collections.emptySet(), Collections.emptySet()),
                Pageable.unpaged());
    }


    @Test
    void importRecordsJsonImportsSpecifiedRecordsAndReturnsImportResult() throws Exception {
        final List<Record> records =
                List.of(Generator.generateRecord(user), Generator.generateRecord(user));
        final RecordImportResult importResult = new RecordImportResult(records.size());
        importResult.setImportedCount(records.size());

        when(recordServiceMock.importRecords(anyList())).thenReturn(importResult);

        MockMultipartFile file = new MockMultipartFile("file", "records.json",
                MediaType.MULTIPART_FORM_DATA_VALUE, toJson(records).getBytes());

        final MvcResult mvcResult = mockMvc.perform(
                multipart("/records/import/json")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
        ).andReturn();

        final RecordImportResult result = readValue(mvcResult, RecordImportResult.class);
        assertEquals(importResult.getTotalCount(), result.getTotalCount());
        assertEquals(importResult.getImportedCount(), result.getImportedCount());
        assertThat(importResult.getErrors(), anyOf(nullValue(), empty()));

        @SuppressWarnings("unchecked")
        final ArgumentCaptor<List<Record>> captor = ArgumentCaptor.forClass(List.class);
        verify(recordServiceMock).importRecords(captor.capture());
        assertEquals(records.size(), captor.getValue().size());
    }


    @Test
    void importRecordsJsonImportsSpecifiedRecordsWithSpecifiedPhaseAndReturnsImportResult() throws Exception {
        final List<Record> records =
                List.of(Generator.generateRecord(user), Generator.generateRecord(user));
        final RecordImportResult importResult = new RecordImportResult(records.size());
        importResult.setImportedCount(records.size());
        final RecordPhase targetPhase = RecordPhase.values()[Generator.randomInt(0, RecordPhase.values().length)];
        when(recordServiceMock.importRecords(anyList(), any(RecordPhase.class))).thenReturn(importResult);

        MockMultipartFile file = new MockMultipartFile("file", "records.json",
            MediaType.MULTIPART_FORM_DATA_VALUE, toJson(records).getBytes());

        mockMvc.perform(
            multipart("/records/import/json")
                .file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                .param("phase", targetPhase.getIri())
        ).andExpect(status().isOk());

        verify(recordServiceMock).importRecords(anyList(), eq(targetPhase));
    }

    @Test
    void importRecordsJsonReturnsConflictWhenServiceThrowsRecordAuthorNotFound() throws Exception {
        final List<Record> records =
                List.of(Generator.generateRecord(user), Generator.generateRecord(user));
        when(recordServiceMock.importRecords(anyList())).thenThrow(RecordAuthorNotFoundException.class);

        MockMultipartFile file = new MockMultipartFile("file", "records.json",
                MediaType.MULTIPART_FORM_DATA_VALUE, toJson(records).getBytes());

        mockMvc.perform(
                multipart("/records/import/json")
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
        final List<Record> records =
                List.of(Generator.generateRecord(user), Generator.generateRecord(user));
        when(recordServiceMock.findAllFull(any(RecordFilterParams.class), any(
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
        final List<Record> result = readValue(mvcResult, new TypeReference<>() {
        });
        assertThat(result, containsSameEntities(records));
        verify(recordServiceMock).findAllFull(
                new RecordFilterParams(null,Set.of(), minDate, maxDate, Collections.emptySet(), Collections.emptySet()),
                PageRequest.of(page, pageSize, Sort.Direction.DESC, RecordSort.SORT_DATE_PROPERTY));
    }

    @Test
    void getRecordsPublishesPagingEvent() throws Exception {
        List<RecordDto> records =
                List.of(Generator.generateRecordDto(user), Generator.generateRecordDto(user),
                        Generator.generateRecordDto(user));

        final Page<RecordDto> page = new PageImpl<>(records, PageRequest.of(0, 5), 3);
        when(recordServiceMock.findAll(any(RecordFilterParams.class), any(Pageable.class))).thenReturn(page);
        final MvcResult result = mockMvc.perform(get("/records").param("institution", user.getInstitution().toString()).queryParam(Constants.PAGE_PARAM, "0")
                                                                .queryParam(Constants.PAGE_SIZE_PARAM, "5"))
                                        .andReturn();

        assertEquals(HttpStatus.OK, HttpStatus.valueOf(result.getResponse().getStatus()));
        final List<RecordDto> body = objectMapper.readValue(result.getResponse().getContentAsString(),
                                                                   new TypeReference<>() {
                                                                   });
        assertEquals(3, body.size());
        verify(recordServiceMock).findAll(any(RecordFilterParams.class), eq(PageRequest.of(0, 5)));
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
        final List<Record> records =
                List.of(Generator.generateRecord(user), Generator.generateRecord(user));
        final Page<Record> page = new PageImpl<>(records, PageRequest.of(0, 50), 100);
        when(recordServiceMock.findAllFull(any(RecordFilterParams.class), any(Pageable.class))).thenReturn(page);

        final MvcResult mvcResult = mockMvc.perform(get("/records/export")
                                                            .param("minDate", minDate.toString())
                                                            .param("maxDate", maxDate.toString())
                                                            .param(Constants.PAGE_PARAM, "0")
                                                            .param(Constants.PAGE_SIZE_PARAM, "50"))
                                           .andReturn();
        final List<Record> result = readValue(mvcResult, new TypeReference<>() {
        });
        assertThat(result, containsSameEntities(records));
        verify(recordServiceMock).findAllFull(
                new RecordFilterParams(null,Set.of(), minDate, maxDate, Collections.emptySet(), Collections.emptySet()), PageRequest.of(0, 50));
        final ArgumentCaptor<PaginatedResultRetrievedEvent> captor =
                ArgumentCaptor.forClass(PaginatedResultRetrievedEvent.class);
        verify(eventPublisherMock).publishEvent(captor.capture());
        final PaginatedResultRetrievedEvent event = captor.getValue();
        assertEquals(page, event.getPage());
    }

    @Test
    void getAllowedRejectReasonTrue() throws Exception {
        String expectedValue = "true";
        when(configReaderMock.getConfig(ConfigParam.RECORDS_ALLOWED_REJECT_REASON)).thenReturn(expectedValue);
          final MvcResult result = mockMvc.perform(get("/records/allowedRejectReason").param("institution", user.getInstitution().toString())).andReturn();

        final String body = objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertEquals(result.getResponse().getStatus(), HttpStatus.OK.value());
        assertEquals(expectedValue, body);
    }

    @Test
    void getAllowedRejectReasonFalse() throws Exception {
        String expectedValue = "false";
        when(configReaderMock.getConfig(ConfigParam.RECORDS_ALLOWED_REJECT_REASON)).thenReturn(expectedValue);
        final MvcResult result = mockMvc.perform(get("/records/allowedRejectReason").param("institution", user.getInstitution().toString())).andReturn();

        final String body = objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertEquals(result.getResponse().getStatus(), HttpStatus.OK.value());
        assertEquals(expectedValue, body);
    }
}
