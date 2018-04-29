package cz.cvut.kbss.study.rest;

import cz.cvut.kbss.study.environment.generator.Generator;
import cz.cvut.kbss.study.environment.util.Environment;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.PatientRecord;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.service.PatientRecordService;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.AssertionErrors;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


public class PatientRecordControllerTest extends BaseControllerTestRunner {

    @Mock
    private PatientRecordService patientRecordServiceMock;

    @InjectMocks
    private  PatientRecordController controller;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        super.setUp(controller);
        Institution institution = Generator.generateInstitution();
        User user = Generator.generateUser(institution);
        Environment.setCurrentUser(user);
    }

    /*
    @Test
    public void createPreliminaryReportReturnsLocationOfNewInstance() throws Exception {
        final PatientRecord record = Generator.generatePatientRecord(Generator.generateUser(Generator.generateInstitution()));
        final String key = "xxx";
        doAnswer(call -> {
            final PatientRecord pr = (PatientRecord) call.getArguments()[0];
            pr.setKey(key);
            return null;
        }).when(patientRecordServiceMock).persist(any(PatientRecord.class));

        final MvcResult result = mockMvc.perform(
                post("/records").content(toJson(reportMapper.preliminaryReportToPreliminaryReportDto(report)))
                        .contentType(
                                MediaType.APPLICATION_JSON_VALUE)).andReturn();
        assertEquals(HttpStatus.CREATED, HttpStatus.valueOf(result.getResponse().getStatus()));
        verifyLocationEquals("/reports/" + key, result);
        verify(patientRecordServiceMock).persist(any(PatientRecord.class));
    }*/

    @Test
    public void getRecordThrowsNotFoundWhenReportIsNotFound() throws Exception {
        final String key = "12345";
        when(patientRecordServiceMock.findByKey(key)).thenReturn(null);

        final MvcResult result = mockMvc.perform(get("/records/" + key)).andReturn();
        assertEquals(HttpStatus.NOT_FOUND, HttpStatus.valueOf(result.getResponse().getStatus()));
    }


    @Test
    public void getRecordReturnsFoundRecord() throws Exception {
        final String key = "12345";
        PatientRecord patientRecord = Generator.generatePatientRecord(Environment.getCurrentUser());
        when(patientRecordServiceMock.findByKey(key)).thenReturn(patientRecord);

        final MvcResult result = mockMvc.perform(get("/records/" + key)).andReturn();
        assertEquals(HttpStatus.OK, HttpStatus.valueOf(result.getResponse().getStatus()));
        final PatientRecord res = objectMapper.readValue(result.getResponse().getContentAsString(), PatientRecord.class);
        assertEquals(res.getUri(),patientRecord.getUri());
        verify(patientRecordServiceMock).findByKey(key);
    }
}
