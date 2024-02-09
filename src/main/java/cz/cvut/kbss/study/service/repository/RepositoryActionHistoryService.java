package cz.cvut.kbss.study.service.repository;

import cz.cvut.kbss.study.model.ActionHistory;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.persistence.dao.ActionHistoryDao;
import cz.cvut.kbss.study.persistence.dao.GenericDao;
import cz.cvut.kbss.study.service.ActionHistoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RepositoryActionHistoryService extends BaseRepositoryService<ActionHistory> implements ActionHistoryService {

    private final ActionHistoryDao actionHistoryDao;

    public RepositoryActionHistoryService(ActionHistoryDao actionHistoryDao) {
        this.actionHistoryDao = actionHistoryDao;
    }

    @Override
    protected GenericDao<ActionHistory> getPrimaryDao() {
        return actionHistoryDao;
    }

    @Transactional(readOnly = true)
    @Override
    public ActionHistory findByKey(String key) {
        return actionHistoryDao.findByKey(key);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<ActionHistory> findAllWithParams(String type, User author, Pageable pageSpec) {
        return actionHistoryDao.findAllWithParams(type, author, pageSpec);
    }
}
