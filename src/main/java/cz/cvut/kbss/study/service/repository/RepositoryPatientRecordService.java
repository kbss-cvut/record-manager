package cz.cvut.kbss.study.service.repository;

import cz.cvut.kbss.study.dto.PatientRecordDto;
import cz.cvut.kbss.study.dto.RecordImportResult;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.PatientRecord;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.persistence.dao.OwlKeySupportingDao;
import cz.cvut.kbss.study.persistence.dao.PatientRecordDao;
import cz.cvut.kbss.study.persistence.dao.util.RecordFilterParams;
import cz.cvut.kbss.study.service.PatientRecordService;
import cz.cvut.kbss.study.service.security.SecurityUtils;
import cz.cvut.kbss.study.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class RepositoryPatientRecordService extends KeySupportingRepositoryService<PatientRecord>
        implements PatientRecordService {

    private static final Logger LOG = LoggerFactory.getLogger(RepositoryPatientRecordService.class);

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
    public List<PatientRecord> findAllFull(RecordFilterParams filterParams) {
        return recordDao.findAllFull(filterParams);
    }

    @Override
    protected void prePersist(PatientRecord instance) {
        final User author = securityUtils.getCurrentUser();
        instance.setAuthor(author);
        instance.setDateCreated(new Date());
        instance.setInstitution(author.getInstitution());
        recordDao.requireUniqueNonEmptyLocalName(instance);
    }

    @Override
    protected void preUpdate(PatientRecord instance) {
        instance.setLastModifiedBy(securityUtils.getCurrentUser());
        instance.setLastModified(new Date());
        recordDao.requireUniqueNonEmptyLocalName(instance);
    }

    @Transactional
    @Override
    public RecordImportResult importRecords(List<PatientRecord> records) {
        Objects.requireNonNull(records);
        LOG.debug("Importing records.");
        final User author = securityUtils.getCurrentUser();
        final Date created = new Date();
        final RecordImportResult result = new RecordImportResult(records.size());
        records.forEach(r -> {
            r.setAuthor(author);
            r.setInstitution(author.getInstitution());
            r.setDateCreated(created);
            if (recordDao.exists(r.getUri())) {
                LOG.warn("Record {} already exists. Skipping it.", Utils.uriToString(r.getUri()));
                result.addError("Record " + Utils.uriToString(r.getUri()) + " already exists.");
            } else {
                recordDao.persist(r);
                result.incrementImportedCount();
            }
        });
        return result;
    }
}
