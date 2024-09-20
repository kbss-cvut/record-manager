package cz.cvut.kbss.study.rest;


import com.fasterxml.jackson.core.type.TypeReference;
import cz.cvut.kbss.study.environment.generator.Generator;
import cz.cvut.kbss.study.environment.util.Environment;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.Role;
import cz.cvut.kbss.study.model.RoleGroup;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.service.InstitutionService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest extends BaseControllerTestRunner {

    @Mock
    private UserService userServiceMock;

    @Mock
    private InstitutionService institutionServiceMock;

    @InjectMocks
    private UserController controller;

    private RoleGroup roleGroupAdmin;

    @BeforeEach
    public void setUp() {
        super.setUp(controller);
        Institution institution = Generator.generateInstitution();
        this.roleGroupAdmin = Generator.generateRoleGroupWithRoles(Role.administrator);
        User user = Generator.generateUser(institution, this.roleGroupAdmin);
        user.setUsername("tom");
        Environment.setCurrentUser(user);
    }

    @Test
    public void getUserByUsernameThrowsNotFoundWhenUserIsNotFound() throws Exception {
        final String username = "tom";
        when(userServiceMock.findByUsername(username)).thenReturn(null);

        final MvcResult result = mockMvc.perform(get("/users/" + username)).andReturn();
        assertEquals(HttpStatus.NOT_FOUND, HttpStatus.valueOf(result.getResponse().getStatus()));
    }

    @Test
    public void getUserByUsernameFoundUser() throws Exception {
        final String username = "tom";
        when(userServiceMock.findByUsername(username)).thenReturn(Environment.getCurrentUser());

        final MvcResult result = mockMvc.perform(get("/users/" + username)).andReturn();
        assertEquals(HttpStatus.OK, HttpStatus.valueOf(result.getResponse().getStatus()));
        final User res = objectMapper.readValue(result.getResponse().getContentAsString(), User.class);
        assertEquals(res.getUri(), Environment.getCurrentUser().getUri());
        verify(userServiceMock).findByUsername(username);
    }

    @Test
    public void createUserReturnsResponseStatusCreated() throws Exception {
        final MvcResult result = mockMvc.perform(post("/users/").content(toJson(Environment.getCurrentUser()))
                .contentType(MediaType.APPLICATION_JSON_VALUE)).andReturn();

        assertEquals(HttpStatus.CREATED, HttpStatus.valueOf(result.getResponse().getStatus()));
    }

    @Test
    public void getUsersReturnsEmptyListWhenNoUsersAreFound() throws Exception {
        when(userServiceMock.findAll()).thenReturn(Collections.emptyList());

        final MvcResult result = mockMvc.perform(get("/users/")).andReturn();

        assertEquals(HttpStatus.OK, HttpStatus.valueOf(result.getResponse().getStatus()));
        final List<User> body = objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<List<User>>() {
                });
        assertTrue(body.isEmpty());
    }

    @Test
    public void getUsersReturnsAllUsers() throws Exception {
        Institution institution = Generator.generateInstitution();

        User user1 = Generator.generateUser(institution, this.roleGroupAdmin);
        User user2 = Generator.generateUser(institution, this.roleGroupAdmin);
        User user3 = Generator.generateUser(institution, this.roleGroupAdmin);

        List<User> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);
        users.add(user3);

        when(userServiceMock.findAll()).thenReturn(users);

        final MvcResult result = mockMvc.perform(get("/users/")).andReturn();

        assertEquals(HttpStatus.OK, HttpStatus.valueOf(result.getResponse().getStatus()));
        final List<User> body = objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<List<User>>() {
                });
        assertEquals(3, body.size());
        verify(userServiceMock).findAll();
    }

    @Test
    public void findAllUsersByInstitutionReturnsInstitutionUsers() throws Exception {
        final String key = "12345";
        Institution institution = Generator.generateInstitution();
        institution.setKey(key);

        User user1 = Generator.generateUser(institution, this.roleGroupAdmin);
        User user2 = Generator.generateUser(institution, this.roleGroupAdmin);
        User user3 = Generator.generateUser(institution, this.roleGroupAdmin);

        List<User> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);
        users.add(user3);

        when(institutionServiceMock.findByKey(institution.getKey())).thenReturn(institution);
        when(userServiceMock.findByInstitution(institution)).thenReturn(users);

        final MvcResult result = mockMvc.perform(get("/users/").param("institution", key)).andReturn();

        assertEquals(HttpStatus.OK, HttpStatus.valueOf(result.getResponse().getStatus()));
        final List<User> body = objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<List<User>>() {
                });
        assertEquals(3, body.size());
        verify(userServiceMock).findByInstitution(institution);
    }

    @Test
    public void removeUserReturnsResponseStatusNoContent() throws Exception {
        final String username = "tom";

        when(userServiceMock.findByUsername(username)).thenReturn(Environment.getCurrentUser());

        final MvcResult result = mockMvc.perform(delete("/users/" + username )).andReturn();

        assertEquals(HttpStatus.NO_CONTENT, HttpStatus.valueOf(result.getResponse().getStatus()));
        verify(userServiceMock).findByUsername(username);
    }


    @Test
    public void updateUserReturnsResponseStatusNoContent() throws Exception {
        final String username = "tom";

        when(userServiceMock.findByUsername(username)).thenReturn(Environment.getCurrentUser());

        final MvcResult result = mockMvc.perform(put("/users/" + username).content(toJson(Environment.getCurrentUser()))
                .contentType(MediaType.APPLICATION_JSON_VALUE)).andReturn();

        assertEquals(HttpStatus.NO_CONTENT, HttpStatus.valueOf(result.getResponse().getStatus()));
        verify(userServiceMock).findByUsername(username);
    }

    @Test
    public void updateUserWithNonMatchingUsernameReturnsResponseStatusBadRequest() throws Exception {
                final MvcResult result = mockMvc.perform(put("/users/tomas").content(toJson(Environment.getCurrentUser()))
                .contentType(MediaType.APPLICATION_JSON_VALUE)).andReturn();

        assertEquals(HttpStatus.BAD_REQUEST, HttpStatus.valueOf(result.getResponse().getStatus()));
    }

    @Test
    public void updateUserReturnsResponseStatusNotFound() throws Exception {
        final String username = "tom";

        when(userServiceMock.findByUsername(username)).thenReturn(null);

        final MvcResult result = mockMvc.perform(put("/users/" + username).content(toJson(Environment.getCurrentUser()))
                .contentType(MediaType.APPLICATION_JSON_VALUE)).andReturn();

        assertEquals(HttpStatus.NOT_FOUND, HttpStatus.valueOf(result.getResponse().getStatus()));
        verify(userServiceMock).findByUsername(username);
    }

    @Test
    public void updatePasswordReturnsResponseStatusNoContent() throws Exception {
        final String username = "tom";

        Map<String, String> password = new HashMap<>();
        password.put("newPassword", "newPassword");
        password.put("currentPassword", "currentPassword");

        when(userServiceMock.findByUsername(username)).thenReturn(Environment.getCurrentUser());

        final MvcResult result = mockMvc.perform(put("/users/" + username + "/password-change/")
                .content(toJson(password))
                .contentType(MediaType.APPLICATION_JSON_VALUE)).andReturn();

        assertEquals(HttpStatus.NO_CONTENT, HttpStatus.valueOf(result.getResponse().getStatus()));
        verify(userServiceMock).findByUsername(username);
    }

    @Test
    public void resetPasswordReturnsResponseStatusNoContent() throws Exception {
        final String email = "tom@gmail.com";

        when(userServiceMock.findByEmail(email)).thenReturn(Environment.getCurrentUser());

        final MvcResult result = mockMvc.perform(post("/users/password-reset/")
                .content(email)
                .contentType(MediaType.TEXT_PLAIN_VALUE)).andReturn();

        assertEquals(HttpStatus.NO_CONTENT, HttpStatus.valueOf(result.getResponse().getStatus()));
        verify(userServiceMock).findByEmail(email);
    }

    @Test
    public void generateUsernameReturnsNewUsername() throws Exception {
        final String usernamePrefix = "admin";

        when(userServiceMock.generateUsername(usernamePrefix)).thenReturn(usernamePrefix + "1");

        final MvcResult result = mockMvc.perform(get("/users/generate-username/" + usernamePrefix)
                .content(usernamePrefix)
                .contentType(MediaType.TEXT_PLAIN_VALUE)).andReturn();

        assertEquals(HttpStatus.OK, HttpStatus.valueOf(result.getResponse().getStatus()));
        verify(userServiceMock).generateUsername(usernamePrefix);
    }

    @Test
    public void validateTokenReturnsResponseStatusNoContent() throws Exception {
        final String token = "12345";

        when(userServiceMock.findByToken(token)).thenReturn(Environment.getCurrentUser());

        final MvcResult result = mockMvc.perform(post("/users/validate-token/")
                .content(token)
                .contentType(MediaType.TEXT_PLAIN_VALUE)).andReturn();

        assertEquals(HttpStatus.NO_CONTENT, HttpStatus.valueOf(result.getResponse().getStatus()));
        verify(userServiceMock).findByToken(token);
    }

    @Test
    public void validateTokenReturnsResponseStatusBadRequest() throws Exception {
        final String token = "12345";

        when(userServiceMock.findByToken(token)).thenReturn(null);

        final MvcResult result = mockMvc.perform(post("/users/validate-token/")
                .content(token)
                .contentType(MediaType.TEXT_PLAIN_VALUE)).andReturn();

        assertEquals(HttpStatus.BAD_REQUEST, HttpStatus.valueOf(result.getResponse().getStatus()));
        verify(userServiceMock).findByToken(token);
    }

    @Test
    public void changePasswordByTokenReturnsResponseStatusNoContent() throws Exception {
        final String token = "123456";

        Map<String, String> data = new HashMap<>();
        data.put("token", token);
        data.put("password", "password");

        when(userServiceMock.findByToken(token)).thenReturn(Environment.getCurrentUser());

        final MvcResult result = mockMvc.perform(put("/users/password-change-token")
                .content(toJson(data))
                .contentType(MediaType.APPLICATION_JSON_VALUE)).andReturn();

        assertEquals(HttpStatus.NO_CONTENT, HttpStatus.valueOf(result.getResponse().getStatus()));
        verify(userServiceMock).findByToken(token);
    }

    @Test
    public void sendInvitationReturnsResponseStatusBadRequest() throws Exception {
        final String username = "tom";
        final Institution institution = Generator.generateInstitution();
        final User user = Generator.generateUser(institution, this.roleGroupAdmin);
        user.setIsInvited(true);

        when(userServiceMock.findByUsername(username)).thenReturn(user);

        final MvcResult result = mockMvc.perform(put("/users/send-invitation/" + username)).andReturn();

        assertEquals(HttpStatus.BAD_REQUEST, HttpStatus.valueOf(result.getResponse().getStatus()));
        verify(userServiceMock).findByUsername(username);
    }

    @Test
    public void sendInvitationReturnsResponseStatusNoContent() throws Exception {
        final String username = "tom";
        final Institution institution = Generator.generateInstitution();
        final User user = Generator.generateUser(institution, this.roleGroupAdmin);
        user.setIsInvited(false);

        when(userServiceMock.findByUsername(username)).thenReturn(user);

        final MvcResult result = mockMvc.perform(put("/users/send-invitation/" + username)).andReturn();

        assertEquals(HttpStatus.NO_CONTENT, HttpStatus.valueOf(result.getResponse().getStatus()));
        verify(userServiceMock).findByUsername(username);
    }

    @Test
    public void sendInvitationDeleteReturnsResponseStatusNoContent() throws Exception {
        final String username = "tom";
        final Institution institution = Generator.generateInstitution();
        final User user = Generator.generateUser(institution, this.roleGroupAdmin);
        user.setIsInvited(false);

        when(userServiceMock.findByUsername(username)).thenReturn(user);

        final MvcResult result = mockMvc.perform(post("/users/send-invitation/delete/")
                .content(username)
                .contentType(MediaType.TEXT_PLAIN_VALUE))
                .andReturn();

        assertEquals(HttpStatus.NO_CONTENT, HttpStatus.valueOf(result.getResponse().getStatus()));
        verify(userServiceMock).findByUsername(username);
    }
}
