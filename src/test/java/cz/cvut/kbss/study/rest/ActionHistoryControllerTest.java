package cz.cvut.kbss.study.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import cz.cvut.kbss.study.environment.generator.Generator;
import cz.cvut.kbss.study.environment.util.Environment;
import cz.cvut.kbss.study.exception.NotFoundException;
import cz.cvut.kbss.study.model.ActionHistory;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.service.ActionHistoryService;
import cz.cvut.kbss.study.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ExtendWith(MockitoExtension.class)
public class ActionHistoryControllerTest extends BaseControllerTestRunner {

    @Mock
    private ActionHistoryService actionHistoryServiceMock;

    @Mock
    private UserService userServiceMock;

    @InjectMocks
    private ActionHistoryController controller;

    @BeforeEach
    public void setUp() {
        super.setUp(controller);
        Institution institution = Generator.generateInstitution();
        User user = Generator.generateUser(institution);
        Environment.setCurrentUser(user);
    }

    @Test
    public void createActionReturnsResponseStatusCreated() throws Exception {
        ActionHistory action = Generator.generateActionHistory(Environment.getCurrentUser());

        final MvcResult result = mockMvc.perform(post("/history").content(toJson(action))
                .contentType(MediaType.APPLICATION_JSON_VALUE)).andReturn();

        assertEquals(HttpStatus.CREATED, HttpStatus.valueOf(result.getResponse().getStatus()));
    }

    @Test
    public void getByKeyThrowsNotFoundWhenActionIsNotFound() throws Exception {
        final String key = "12345";
        when(actionHistoryServiceMock.findByKey(key)).thenReturn(null);

        final MvcResult result = mockMvc.perform(get("/history/" + key)).andReturn();
        assertEquals(HttpStatus.NOT_FOUND, HttpStatus.valueOf(result.getResponse().getStatus()));
    }

    @Test
    public void getByKeyReturnsFoundAction() throws Exception {
        final String key = "12345";
        ActionHistory action = Generator.generateActionHistory(Environment.getCurrentUser());
        action.setKey(key);
        when(actionHistoryServiceMock.findByKey(key)).thenReturn(action);

        final MvcResult result = mockMvc.perform(get("/history/" + key)).andReturn();
        assertEquals(HttpStatus.OK, HttpStatus.valueOf(result.getResponse().getStatus()));
        final ActionHistory res = objectMapper.readValue(result.getResponse().getContentAsString(), ActionHistory.class);
        assertEquals(res.getUri(),action.getUri());
        verify(actionHistoryServiceMock).findByKey(key);
    }

    @Test
    public void getActionsReturnsEmptyListWhenNoActionsAreFound() throws Exception {
        when(actionHistoryServiceMock.findAllWithParams(null, null, 1)).thenReturn(Collections.emptyList());

        final MvcResult result = mockMvc.perform(get("/history/").param("page", "1")).andReturn();

        assertEquals(HttpStatus.OK, HttpStatus.valueOf(result.getResponse().getStatus()));
        final List<ActionHistory> body = objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<List<ActionHistory>>() {
                });
        assertTrue(body.isEmpty());
    }

    @Test
    public void getActionsReturnsAllActions() throws Exception {
        ActionHistory action1 = Generator.generateActionHistory(Environment.getCurrentUser());
        ActionHistory action2 = Generator.generateActionHistory(Environment.getCurrentUser());
        List<ActionHistory> actions = new ArrayList<>();

        actions.add(action1);
        actions.add(action2);

        when(actionHistoryServiceMock.findAllWithParams(null, null, 1)).thenReturn(actions);

        final MvcResult result = mockMvc.perform(get("/history/").param("page", "1")).andReturn();

        assertEquals(HttpStatus.OK, HttpStatus.valueOf(result.getResponse().getStatus()));
        final List<ActionHistory> body = objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<List<ActionHistory>>() {
                });
        assertEquals(2, body.size());
        verify(actionHistoryServiceMock).findAllWithParams(null , null, 1);
    }

    @Test
    public void getActionsByUnknownAuthorReturnsEmptyList() throws Exception {
        String username = "Tom";
        when(userServiceMock.findByUsername(username)).thenThrow(NotFoundException.create("User", username));

        final MvcResult result = mockMvc.perform(get("/history/")
                .param("author", username)
                .param("page", "1"))
                .andReturn();

        assertEquals(HttpStatus.OK, HttpStatus.valueOf(result.getResponse().getStatus()));
        final List<ActionHistory> body = objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<List<ActionHistory>>() {
                });
        assertTrue(body.isEmpty());
    }

    @Test
    public void getActionsByAuthorReturnsActions() throws Exception {
        ActionHistory action1 = Generator.generateActionHistory(Environment.getCurrentUser());
        ActionHistory action2 = Generator.generateActionHistory(Environment.getCurrentUser());
        List<ActionHistory> actions = new ArrayList<>();

        actions.add(action1);
        actions.add(action2);

        when(actionHistoryServiceMock.findAllWithParams(null, Environment.getCurrentUser(), 1)).thenReturn(actions);
        when(userServiceMock.findByUsername(Environment.getCurrentUser().getUsername())).thenReturn(Environment.getCurrentUser());

        final MvcResult result = mockMvc.perform(get("/history/")
                .param("author", Environment.getCurrentUser().getUsername())
                .param("page", "1"))
                .andReturn();

        assertEquals(HttpStatus.OK, HttpStatus.valueOf(result.getResponse().getStatus()));
        final List<ActionHistory> body = objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<List<ActionHistory>>() {
                });
        assertEquals(2, body.size());
        verify(actionHistoryServiceMock).findAllWithParams(null, Environment.getCurrentUser(), 1);
    }

    @Test
    public void getActionsByTypeReturnsActions() throws Exception {
        ActionHistory action1 = Generator.generateActionHistory(Environment.getCurrentUser());
        ActionHistory action2 = Generator.generateActionHistory(Environment.getCurrentUser());
        List<ActionHistory> actions = new ArrayList<>();

        actions.add(action1);
        actions.add(action2);

        when(actionHistoryServiceMock.findAllWithParams("TYPE", null, 1)).thenReturn(actions);

        final MvcResult result = mockMvc.perform(get("/history/")
                .param("type", "TYPE")
                .param("page", "1"))
                .andReturn();

        assertEquals(HttpStatus.OK, HttpStatus.valueOf(result.getResponse().getStatus()));
        final List<ActionHistory> body = objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<List<ActionHistory>>() {
                });
        assertEquals(2, body.size());
        verify(actionHistoryServiceMock).findAllWithParams("TYPE", null, 1);
    }

    @Test
    public void getActionsByTypeAndAuthorReturnsActions() throws Exception {
        User user = Environment.getCurrentUser();
        ActionHistory action1 = Generator.generateActionHistory(user);
        ActionHistory action2 = Generator.generateActionHistory(user);
        List<ActionHistory> actions = new ArrayList<>();

        actions.add(action1);
        actions.add(action2);

        when(userServiceMock.findByUsername(user.getUsername())).thenReturn(user);
        when(actionHistoryServiceMock.findAllWithParams("TYPE", user, 1)).thenReturn(actions);

        final MvcResult result = mockMvc.perform(get("/history/")
                .param("author", user.getUsername())
                .param("type", "TYPE")
                .param("page", "1"))
                .andReturn();

        assertEquals(HttpStatus.OK, HttpStatus.valueOf(result.getResponse().getStatus()));
        final List<ActionHistory> body = objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<List<ActionHistory>>() {
                });
        assertEquals(2, body.size());
        verify(actionHistoryServiceMock).findAllWithParams("TYPE", user, 1);
    }
}
