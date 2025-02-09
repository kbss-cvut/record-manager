package cz.cvut.kbss.study.service.repository;

import cz.cvut.kbss.study.model.ActionHistory;
import cz.cvut.kbss.study.model.RoleGroup;
import cz.cvut.kbss.study.persistence.dao.GenericDao;
import cz.cvut.kbss.study.persistence.dao.RoleGroupDao;
import cz.cvut.kbss.study.service.RoleGroupService;
import org.springframework.stereotype.Service;

@Service
public class RepositoryRoleGroup extends BaseRepositoryService<RoleGroup> implements RoleGroupService {

    private final RoleGroupDao roleGroupDao;

    public RepositoryRoleGroup(RoleGroupDao roleGroupDao) {
        this.roleGroupDao = roleGroupDao;
    }

    @Override
    public RoleGroup findByName(String name) {
        return roleGroupDao.findByName(name);
    }

    @Override
    protected GenericDao<RoleGroup> getPrimaryDao() {
        return roleGroupDao;
    }
}
