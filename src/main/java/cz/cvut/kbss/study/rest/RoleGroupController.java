package cz.cvut.kbss.study.rest;


import cz.cvut.kbss.study.exception.NotFoundException;
import cz.cvut.kbss.study.model.RoleGroup;
import cz.cvut.kbss.study.security.SecurityConstants;
import cz.cvut.kbss.study.service.RoleGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@ConditionalOnProperty(prefix = "security", name = "provider", havingValue = "internal", matchIfMissing = true)
@RestController
@RequestMapping("/roleGroups")
public class RoleGroupController extends BaseController {

    private final RoleGroupService roleGroupService;

    @Autowired
    public RoleGroupController(RoleGroupService roleGroupService) {
        this.roleGroupService = roleGroupService;
    }

    @PreAuthorize("hasAuthority('" + SecurityConstants.adminRoleGroups + "')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<RoleGroup> getRoleGroups() {
        return roleGroupService.findAll();
    }

    @PreAuthorize("hasAuthority('" + SecurityConstants.adminRoleGroups + "')")
    @GetMapping(value = "/{name}",produces = MediaType.APPLICATION_JSON_VALUE)
    public RoleGroup findByName(@PathVariable("name") String name) {
        RoleGroup result = roleGroupService.findByName(name);
        if(result == null){
            throw NotFoundException.create("RoleGroup", name);
        }
        return result;
    }

}
