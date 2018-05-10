package cz.cvut.kbss.study.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import cz.cvut.kbss.study.dto.PatientRecordDto;
import cz.cvut.kbss.study.environment.generator.Generator;
import cz.cvut.kbss.study.environment.util.Environment;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.PatientRecord;
import cz.cvut.kbss.study.model.User;
import static org.junit.Assert.*;

import cz.cvut.kbss.study.service.StatisticsService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;


public class StatisticsControllerTest extends BaseControllerTestRunner {

    @Mock
    private StatisticsService statisticsServiceMock;

    @InjectMocks
    private StatisticsController controller;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        super.setUp(controller);
        Institution institution = Generator.generateInstitution();
        User user = Generator.generateUser(institution);
        Environment.setCurrentUser(user);
    }

    @Test
    public void getRecordReturnsFoundRecord() throws Exception {
        when(statisticsServiceMock.getNumberOfInvestigators()).thenReturn(5);
        when(statisticsServiceMock.getNumberOfProcessedRecords()).thenReturn(10);

        final MvcResult result = mockMvc.perform(get("/statistics")).andReturn();
        assertEquals(HttpStatus.OK, HttpStatus.valueOf(result.getResponse().getStatus()));
        assertEquals("{\"number-of-processed-records\":10,\"number-of-investigators\":5}",
                result.getResponse().getContentAsString());
    }

}
