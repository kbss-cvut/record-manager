package cz.cvut.kbss.study.service.repository;

import cz.cvut.kbss.study.model.ActionHistory;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.persistence.dao.ActionHistoryDao;
import cz.cvut.kbss.study.persistence.dao.GenericDao;
import cz.cvut.kbss.study.persistence.dao.OwlKeySupportingDao;
import cz.cvut.kbss.study.persistence.dao.UserDao;
import cz.cvut.kbss.study.service.ActionHistoryService;
import cz.cvut.kbss.study.service.InstitutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Collection;
import java.util.List;

@Service
public class RepositoryActionHistoryService extends BaseRepositoryService<ActionHistory> implements ActionHistoryService {

    @Autowired
    private ActionHistoryDao actionHistoryDao;

    @Override
    protected GenericDao<ActionHistory> getPrimaryDao() {
        return actionHistoryDao;
    }
}
