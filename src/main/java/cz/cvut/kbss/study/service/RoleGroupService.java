package cz.cvut.kbss.study.service;

import cz.cvut.kbss.study.model.RoleGroup;
import cz.cvut.kbss.study.service.repository.BaseRepositoryService;
import org.springframework.stereotype.Service;

public interface RoleGroupService extends BaseService<RoleGroup> {

    RoleGroup findByName(String name);


}
