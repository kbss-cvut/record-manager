package cz.cvut.kbss.study.service.repository;

import cz.cvut.kbss.study.model.ActionHistory;
import cz.cvut.kbss.study.model.RoleGroup;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.persistence.dao.GenericDao;
import cz.cvut.kbss.study.persistence.dao.RoleGroupDao;
import cz.cvut.kbss.study.service.RoleGroupService;
import cz.cvut.kbss.study.service.security.SecurityUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RepositoryRoleGroup extends BaseRepositoryService<RoleGroup> implements RoleGroupService {

    private final RoleGroupDao roleGroupDao;

    private final SecurityUtils securityUtils;

    public RepositoryRoleGroup(RoleGroupDao roleGroupDao, SecurityUtils securityUtils) {
        this.roleGroupDao = roleGroupDao;
        this.securityUtils = securityUtils;
    }

    @Override
    public RoleGroup findByName(String name) {
        return roleGroupDao.findByName(name);
    }

    @Override
    public List<RoleGroup> findAvailable() {
        final User current = securityUtils.getCurrentUser();
        List<RoleGroup> roleGroups = roleGroupDao.findAll();

        return current.getRoleGroup() != null
                ? roleGroups.stream().filter(roleGroup -> current.getRoleGroup().getRoles().containsAll(roleGroup.getRoles())).toList()
                : List.of();
    }

    @Override
    protected GenericDao<RoleGroup> getPrimaryDao() {
        return roleGroupDao;
    }
}
