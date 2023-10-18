package cz.cvut.kbss.study.service.repository;

import cz.cvut.kbss.study.persistence.dao.PatientRecordDao;
import cz.cvut.kbss.study.persistence.dao.UserDao;
import cz.cvut.kbss.study.service.StatisticsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RepositoryStatisticsService implements StatisticsService {

    private final UserDao userDao;

    private final PatientRecordDao recordDao;

    public RepositoryStatisticsService(UserDao userDao,
                                       PatientRecordDao recordDao) {
        this.userDao = userDao;
        this.recordDao = recordDao;
    }

    @Transactional(readOnly = true)
    @Override
    public int getNumberOfInvestigators() {
        return userDao.getNumberOfInvestigators();
    }

    @Transactional(readOnly = true)
    @Override
    public int getNumberOfProcessedRecords() {
        return recordDao.getNumberOfProcessedRecords();
    }
}
