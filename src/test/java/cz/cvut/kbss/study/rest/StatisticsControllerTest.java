package cz.cvut.kbss.study.rest;

import cz.cvut.kbss.study.environment.generator.Generator;
import cz.cvut.kbss.study.environment.util.Environment;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.RoleGroup;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.service.StatisticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@ExtendWith(MockitoExtension.class)
public class StatisticsControllerTest extends BaseControllerTestRunner {

    @Mock
    private StatisticsService statisticsServiceMock;

    @InjectMocks
    private StatisticsController controller;

    @BeforeEach
    public void setUp() {
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
