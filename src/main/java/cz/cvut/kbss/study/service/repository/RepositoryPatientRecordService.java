package cz.cvut.kbss.study.service.repository;

import cz.cvut.kbss.study.dto.PatientRecordSummaryDto;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.PatientRecord;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.persistence.dao.OwlKeySupportingDao;
import cz.cvut.kbss.study.persistence.dao.PatientRecordDao;
import cz.cvut.kbss.study.service.PatientRecordService;
import cz.cvut.kbss.study.service.security.SecurityUtils;
import cz.cvut.kbss.study.util.IdentificationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class RepositoryPatientRecordService extends KeySupportingRepositoryService<PatientRecord>
        implements PatientRecordService {

    @Autowired
    private PatientRecordDao recordDao;

    @Autowired
    private SecurityUtils securityUtils;

    @Override
    protected OwlKeySupportingDao<PatientRecord> getPrimaryDao() {
        return recordDao;
    }

    @Override
    public List<PatientRecord> findByInstitution(Institution institution) {
        return recordDao.findByInstitution(institution);
    }

    @Override
    public List<PatientRecord> findByAuthor(User user) {
        return recordDao.findByAuthor(user);
    }

    @Override
    public List<PatientRecordSummaryDto> getRecordSummaries(Institution institution) {
        return recordDao.getRecordSummaries(institution);
    }

    @Override
    protected void prePersist(PatientRecord instance) {
        final User author = securityUtils.getCurrentUser();
        instance.setAuthor(author);
        instance.setDateCreated(new Date());
        instance.setInstitution(author.getInstitution());
        instance.setKey(IdentificationUtils.generateKey());
    }

    @Override
    protected void preUpdate(PatientRecord instance) {
        instance.setLastModifiedBy(securityUtils.getCurrentUser());
        instance.setLastModified(new Date());
    }
}
