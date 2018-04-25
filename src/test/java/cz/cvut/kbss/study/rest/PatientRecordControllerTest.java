package cz.cvut.kbss.study.rest;

import cz.cvut.kbss.study.environment.config.MockServiceConfig;
import cz.cvut.kbss.study.environment.generator.Generator;
import cz.cvut.kbss.study.environment.util.Environment;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.service.PatientRecordService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@ContextConfiguration(classes = {MockServiceConfig.class})
public class PatientRecordControllerTest extends BaseControllerTestRunner {

    @Autowired
    private PatientRecordService patientRecordServiceMock;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        Mockito.reset(patientRecordServiceMock);
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


}
