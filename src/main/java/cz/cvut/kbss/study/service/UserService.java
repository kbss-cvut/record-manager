package cz.cvut.kbss.study.service;

import cz.cvut.kbss.study.model.User;

public interface UserService extends BaseService<User> {

    User findByUsername(String username);
}
