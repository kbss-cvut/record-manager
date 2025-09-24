package cz.cvut.kbss.study.service;

import cz.cvut.kbss.study.model.RoleGroup;

import java.util.List;

public interface RoleGroupService extends BaseService<RoleGroup> {

    RoleGroup findByName(String name);

    List<RoleGroup> findAvailable();

}
