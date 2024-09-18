package cz.cvut.kbss.study.service;

import cz.cvut.kbss.study.model.RoleGroup;
import org.springframework.stereotype.Service;

public interface RoleGroupService {
    RoleGroup findByName(String name);
}
