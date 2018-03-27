package cz.cvut.kbss.study.service;

import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.User;

import java.util.List;

public interface UserService extends BaseService<User> {

    User findByUsername(String username);

    /**
     * Gets users associated with the specified institution.
     *
     * @param institution The institution to filter by
     * @return Records of matching users
     */
    List<User> findByInstitution(Institution institution);

    String generateUsername(String usernamePrefix);
}
