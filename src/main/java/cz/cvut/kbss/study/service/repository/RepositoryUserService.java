package cz.cvut.kbss.study.service.repository;

import cz.cvut.kbss.study.exception.UsernameExistsException;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.persistence.dao.GenericDao;
import cz.cvut.kbss.study.persistence.dao.UserDao;
import cz.cvut.kbss.study.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RepositoryUserService extends BaseRepositoryService<User> implements UserService {

    @Autowired
    private UserDao userDao;

    @Override
    protected GenericDao<User> getPrimaryDao() {
        return userDao;
    }

    @Override
    public User findByUsername(String username) {
        return userDao.findByUsername(username);
    }

    @Override
    protected void prePersist(User instance) {
        if (findByUsername(instance.getUsername()) != null) {
            throw new UsernameExistsException("Username " + instance.getUsername() + " already exists.");
        }
    }

    @Override
    protected void preUpdate(User instance) {
        final User orig = userDao.find(instance.getUri());
        if (orig == null) {
            throw new IllegalArgumentException("Cannot update user URI.");
        }
    }
}
