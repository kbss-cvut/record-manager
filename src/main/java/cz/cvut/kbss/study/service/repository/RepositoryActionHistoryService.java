package cz.cvut.kbss.study.service.repository;

import cz.cvut.kbss.study.model.ActionHistory;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.persistence.dao.ActionHistoryDao;
import cz.cvut.kbss.study.persistence.dao.GenericDao;
import cz.cvut.kbss.study.service.ActionHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RepositoryActionHistoryService extends BaseRepositoryService<ActionHistory> implements ActionHistoryService {

    @Autowired
    private ActionHistoryDao actionHistoryDao;

    @Override
    protected GenericDao<ActionHistory> getPrimaryDao() {
        return actionHistoryDao;
    }

    @Override
    public List<ActionHistory> findAllByOrderAsc(int pageNumber) {
        return actionHistoryDao.findAllOrderByAsc(pageNumber);
    }

    @Override
    public ActionHistory findByKey(String key) {
        return actionHistoryDao.findByKey(key);
    }

    @Override
    public List<ActionHistory> findByType(String type, int pageNumber) {
        return actionHistoryDao.findByType(type, pageNumber);
    }

    @Override
    public List<ActionHistory> findByAuthor(User author, int pageNumber) {
        return actionHistoryDao.findByAuthor(author, pageNumber);
    }

    @Override
    public List<ActionHistory> findByTypeAndAuthor(String type, User author, int pageNumber) {
        return actionHistoryDao.findByTypeAndAuthor(type, author, pageNumber);
    }
}
