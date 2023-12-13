package cz.cvut.kbss.study.service.repository;

import cz.cvut.kbss.study.dto.PatientRecordDto;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.PatientRecord;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.persistence.dao.OwlKeySupportingDao;
import cz.cvut.kbss.study.persistence.dao.PatientRecordDao;
import cz.cvut.kbss.study.service.PatientRecordService;
import cz.cvut.kbss.study.service.security.SecurityUtils;
import cz.cvut.kbss.study.util.IdentificationUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Service
public class RepositoryPatientRecordService extends KeySupportingRepositoryService<PatientRecord>
        implements PatientRecordService {

    private final PatientRecordDao recordDao;

    private final SecurityUtils securityUtils;

    public RepositoryPatientRecordService(PatientRecordDao recordDao,
                                          SecurityUtils securityUtils) {
        this.recordDao = recordDao;
        this.securityUtils = securityUtils;
    }

    @Override
    protected OwlKeySupportingDao<PatientRecord> getPrimaryDao() {
        return recordDao;
    }

    @Transactional(readOnly = true)
    @Override
    public List<PatientRecordDto> findByInstitution(Institution institution) {
        return recordDao.findByInstitution(institution);
    }

    @Transactional(readOnly = true)
    @Override
    public List<PatientRecord> findByAuthor(User user) {
        return recordDao.findByAuthor(user);
    }

    @Transactional(readOnly = true)
    @Override
    public List<PatientRecordDto> findAllRecords() {
        return recordDao.findAllRecords();
    }

    @Transactional(readOnly = true)
    @Override
    public List<PatientRecord> findAllFull(LocalDate minDate, LocalDate maxDate) {
        return null;
    }

    @Transactional(readOnly = true)
    @Override
    public List<PatientRecord> findAllFull(Institution institution, LocalDate minDate, LocalDate maxDate) {
        return null;
    }

    @Override
    protected void prePersist(PatientRecord instance) {
        final User author = securityUtils.getCurrentUser();
        instance.setAuthor(author);
        instance.setDateCreated(new Date());
        instance.setInstitution(author.getInstitution());
        instance.setKey(IdentificationUtils.generateKey());
        recordDao.requireUniqueNonEmptyLocalName(instance);
    }

    @Override
    protected void preUpdate(PatientRecord instance) {
        instance.setLastModifiedBy(securityUtils.getCurrentUser());
        instance.setLastModified(new Date());
        recordDao.requireUniqueNonEmptyLocalName(instance);
    }
}
