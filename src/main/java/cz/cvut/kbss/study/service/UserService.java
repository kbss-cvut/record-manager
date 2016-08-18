package cz.cvut.kbss.study.service;

import cz.cvut.kbss.study.model.Clinic;
import cz.cvut.kbss.study.model.User;

import java.util.List;

public interface UserService extends BaseService<User> {

    User findByUsername(String username);

    /**
     * Gets users associated with the specified clinic.
     *
     * @param clinic The clinic to filter by
     * @return Records of matching users
     */
    List<User> findByClinic(Clinic clinic);
}
