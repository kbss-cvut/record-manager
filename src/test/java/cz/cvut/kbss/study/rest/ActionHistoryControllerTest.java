package cz.cvut.kbss.study.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import cz.cvut.kbss.study.environment.generator.Generator;
import cz.cvut.kbss.study.environment.util.Environment;
import cz.cvut.kbss.study.exception.NotFoundException;
import cz.cvut.kbss.study.model.*;
import cz.cvut.kbss.study.rest.event.PaginatedResultRetrievedEvent;
import cz.cvut.kbss.study.service.ActionHistoryService;
import cz.cvut.kbss.study.service.UserService;
import cz.cvut.kbss.study.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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

    @Mock
    private ApplicationEventPublisher eventPublisherMock;

    @InjectMocks
    private ActionHistoryController controller;

    private User user;

    private RoleGroup adminRoleGroup;

    @BeforeEach
    public void setUp() {
        super.setUp(controller);
        this.adminRoleGroup = Generator.generateAdminRoleGroup();
        Institution institution = Generator.generateInstitution();
        this.user = Generator.generateUser(institution, adminRoleGroup);
        Environment.setCurrentUser(user);
    }

    @Test
    public void createActionReturnsResponseStatusCreated() throws Exception {
        ActionHistory action = Generator.generateActionHistory(user);

        final MvcResult result = mockMvc.perform(post("/history").content(toJson(action))
                                                                 .contentType(MediaType.APPLICATION_JSON_VALUE))
                                        .andReturn();

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
        ActionHistory action = Generator.generateActionHistory(user);
        action.setKey(key);
        when(actionHistoryServiceMock.findByKey(key)).thenReturn(action);

        final MvcResult result = mockMvc.perform(get("/history/" + key)).andReturn();
        assertEquals(HttpStatus.OK, HttpStatus.valueOf(result.getResponse().getStatus()));
        final ActionHistory res =
                objectMapper.readValue(result.getResponse().getContentAsString(), ActionHistory.class);
        assertEquals(res.getUri(), action.getUri());
        verify(actionHistoryServiceMock).findByKey(key);
    }

    @Test
    public void getActionsReturnsEmptyListWhenNoActionsAreFound() throws Exception {
        when(actionHistoryServiceMock.findAllWithParams(any(), any(), any())).thenReturn(Page.empty());

        final MvcResult result = mockMvc.perform(get("/history/")).andReturn();

        assertEquals(HttpStatus.OK, HttpStatus.valueOf(result.getResponse().getStatus()));
        final List<ActionHistory> body = objectMapper.readValue(result.getResponse().getContentAsString(),
                                                                new TypeReference<>() {
                                                                });
        assertTrue(body.isEmpty());
    }

    @Test
    public void getActionsReturnsAllActions() throws Exception {
        ActionHistory action1 = Generator.generateActionHistory(user);
        ActionHistory action2 = Generator.generateActionHistory(user);
        List<ActionHistory> actions = List.of(action1, action2);

        when(actionHistoryServiceMock.findAllWithParams(any(), any(), any(
                Pageable.class))).thenReturn(new PageImpl<>(actions));

        final MvcResult result = mockMvc.perform(get("/history/").param("page", "1")).andReturn();

        assertEquals(HttpStatus.OK, HttpStatus.valueOf(result.getResponse().getStatus()));
        final List<ActionHistory> body = objectMapper.readValue(result.getResponse().getContentAsString(),
                                                                new TypeReference<>() {
                                                                });
        assertEquals(2, body.size());
        verify(actionHistoryServiceMock).findAllWithParams(null, null, PageRequest.of(1, Constants.DEFAULT_PAGE_SIZE));
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
                                                                new TypeReference<>() {
                                                                });
        assertTrue(body.isEmpty());
        verify(actionHistoryServiceMock, never()).findAllWithParams(any(), any(), any());
    }

    @Test
    public void getActionsByAuthorReturnsActions() throws Exception {
        ActionHistory action1 = Generator.generateActionHistory(user);
        ActionHistory action2 = Generator.generateActionHistory(user);
        List<ActionHistory> actions = List.of(action1, action2);

        when(actionHistoryServiceMock.findAllWithParams(any(), eq(user), any(
                Pageable.class))).thenReturn(new PageImpl<>(actions));
        when(userServiceMock.findByUsername(user.getUsername())).thenReturn(
                user);

        final MvcResult result = mockMvc.perform(get("/history/")
                                                         .param("author", user.getUsername())
                                                         .param("page", "1"))
                                        .andReturn();

        assertEquals(HttpStatus.OK, HttpStatus.valueOf(result.getResponse().getStatus()));
        final List<ActionHistory> body = objectMapper.readValue(result.getResponse().getContentAsString(),
                                                                new TypeReference<>() {
                                                                });
        assertEquals(2, body.size());
        verify(actionHistoryServiceMock).findAllWithParams(null, user,
                                                           PageRequest.of(1, Constants.DEFAULT_PAGE_SIZE));
    }

    @Test
    public void getActionsByTypeReturnsActions() throws Exception {
        ActionHistory action1 = Generator.generateActionHistory(user);
        ActionHistory action2 = Generator.generateActionHistory(user);
        List<ActionHistory> actions = List.of(action1, action2);

        when(actionHistoryServiceMock.findAllWithParams(eq("TYPE"), any(), any(Pageable.class))).thenReturn(
                new PageImpl<>(actions));

        final MvcResult result = mockMvc.perform(get("/history/")
                                                         .param("type", "TYPE")
                                                         .param("page", "1"))
                                        .andReturn();

        assertEquals(HttpStatus.OK, HttpStatus.valueOf(result.getResponse().getStatus()));
        final List<ActionHistory> body = objectMapper.readValue(result.getResponse().getContentAsString(),
                                                                new TypeReference<>() {
                                                                });
        assertEquals(2, body.size());
        verify(actionHistoryServiceMock).findAllWithParams("TYPE", null,
                                                           PageRequest.of(1, Constants.DEFAULT_PAGE_SIZE));
    }

    @Test
    public void getActionsByTypeAndAuthorReturnsActions() throws Exception {
        ActionHistory action1 = Generator.generateActionHistory(user);
        ActionHistory action2 = Generator.generateActionHistory(user);
        List<ActionHistory> actions = List.of(action1, action2);

        when(userServiceMock.findByUsername(user.getUsername())).thenReturn(user);
        when(actionHistoryServiceMock.findAllWithParams(eq("TYPE"), eq(user), any(Pageable.class))).thenReturn(
                new PageImpl<>(actions));

        final MvcResult result = mockMvc.perform(get("/history/")
                                                         .param("author", user.getUsername())
                                                         .param("type", "TYPE")
                                                         .param("page", "1"))
                                        .andReturn();

        assertEquals(HttpStatus.OK, HttpStatus.valueOf(result.getResponse().getStatus()));
        final List<ActionHistory> body = objectMapper.readValue(result.getResponse().getContentAsString(),
                                                                new TypeReference<>() {
                                                                });
        assertEquals(2, body.size());
        verify(actionHistoryServiceMock).findAllWithParams("TYPE", user,
                                                           PageRequest.of(1, Constants.DEFAULT_PAGE_SIZE));
    }

    @Test
    void getActionsPublishesPagingEvent() throws Exception {
        List<ActionHistory> actions =
                IntStream.range(0, 5).mapToObj(i -> Generator.generateActionHistory(user)).toList();
        final Page<ActionHistory> page = new PageImpl<>(actions, PageRequest.of(2, 5), 0L);
        when(actionHistoryServiceMock.findAllWithParams(any(), any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/history/").param("page", "2").param("size", "5"));
        verify(actionHistoryServiceMock).findAllWithParams(null, null, PageRequest.of(2, 5));
        final ArgumentCaptor<PaginatedResultRetrievedEvent> captor = ArgumentCaptor.forClass(
                PaginatedResultRetrievedEvent.class);
        verify(eventPublisherMock).publishEvent(captor.capture());
        final PaginatedResultRetrievedEvent event = captor.getValue();
        assertEquals(page, event.getPage());
    }
}
