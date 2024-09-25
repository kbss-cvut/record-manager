package cz.cvut.kbss.study.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.RoleGroup;
import cz.cvut.kbss.study.service.RoleGroupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@ExtendWith(MockitoExtension.class)
public class RoleGroupControllerTest extends BaseControllerTestRunner {

    @Mock
    private RoleGroupService roleGroupServiceMock;


    @InjectMocks
    private RoleGroupController controller;

    @BeforeEach
    public void setUp() {
        super.setUp(controller);
    }

    @Test
    public void testGetRoleGroups() throws Exception {
        RoleGroup roleGroup = new RoleGroup();
        roleGroup.setName("admin-role-group");

        when(roleGroupServiceMock.findAll()).thenReturn(List.of(roleGroup));

        final MvcResult result =  mockMvc.perform(get("/roleGroup/")).andReturn();

        final List<RoleGroup> body = objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });

        assertEquals(HttpStatus.OK, HttpStatus.valueOf(result.getResponse().getStatus()));
        assertEquals(body, List.of(roleGroup));
    }

    @Test
    public void testFindByName() throws Exception {
        String roleName = "admin-role-group";
        RoleGroup roleGroup = new RoleGroup();
        roleGroup.setName(roleName);

        when(roleGroupServiceMock.findByName(roleName)).thenReturn(roleGroup);

        final MvcResult result =  mockMvc.perform(get("/roleGroup/" + roleName)).andReturn();

        final RoleGroup body = objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });

        assertEquals(HttpStatus.OK, HttpStatus.valueOf(result.getResponse().getStatus()));
        assertEquals(body, roleGroup);
    }

    @Test
    public void testFindByName_NotFound() throws Exception {
        String roleName = "NonExistentRole";

        when(roleGroupServiceMock.findByName(roleName)).thenReturn(null);

        mockMvc.perform(get("/roleGroup/{name}", roleName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

}
