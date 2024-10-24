package cz.cvut.kbss.study.service.repository;

import cz.cvut.kbss.study.dto.PatientRecordDto;
import cz.cvut.kbss.study.dto.RecordImportResult;
import cz.cvut.kbss.study.exception.RecordAuthorNotFoundException;
import cz.cvut.kbss.study.model.PatientRecord;
import cz.cvut.kbss.study.model.RecordPhase;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.model.export.RawRecord;
import cz.cvut.kbss.study.persistence.dao.OwlKeySupportingDao;
import cz.cvut.kbss.study.persistence.dao.PatientRecordDao;
import cz.cvut.kbss.study.persistence.dao.util.RecordFilterParams;
import cz.cvut.kbss.study.service.PatientRecordService;
import cz.cvut.kbss.study.service.UserService;
import cz.cvut.kbss.study.service.security.SecurityUtils;
import cz.cvut.kbss.study.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.*;

@Service
public class RepositoryPatientRecordService extends KeySupportingRepositoryService<PatientRecord>
        implements PatientRecordService {

    private static final Logger LOG = LoggerFactory.getLogger(RepositoryPatientRecordService.class);

    private final PatientRecordDao recordDao;

    private final SecurityUtils securityUtils;

    private final UserService userService;
    private final PatientRecordDao patientRecordDao;

    public RepositoryPatientRecordService(PatientRecordDao recordDao, SecurityUtils securityUtils,
                                          UserService userService, PatientRecordDao patientRecordDao) {
        this.recordDao = recordDao;
        this.securityUtils = securityUtils;
        this.userService = userService;
        this.patientRecordDao = patientRecordDao;
    }

    @Override
    protected OwlKeySupportingDao<PatientRecord> getPrimaryDao() {
        return recordDao;
    }

    @Transactional(readOnly = true)
    @Override
    public Page<PatientRecordDto> findAll(RecordFilterParams filters, Pageable pageSpec) {
        return recordDao.findAllRecords(filters, pageSpec);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<PatientRecord> findAllFull(RecordFilterParams filters, Pageable pageSpec) {
        return recordDao.findAllRecordsFull(filters, pageSpec);
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
        if(instance.getPhase() != RecordPhase.rejected) {
            instance.setRejectMessage(null);
        }
        instance.setLastModifiedBy(securityUtils.getCurrentUser());
        instance.setLastModified(new Date());
        recordDao.requireUniqueNonEmptyLocalName(instance);
    }

    @Transactional
    @Override
    public RecordImportResult importRecords(List<PatientRecord> records) {
        Objects.requireNonNull(records);
        LOG.debug("Importing records.");
        return importRecordsImpl(records, Optional.empty());
    }

    private RecordImportResult importRecordsImpl(List<PatientRecord> records, Optional<RecordPhase> targetPhase) {
        final User author = securityUtils.getCurrentUser();
        final Date created = new Date();
        final RecordImportResult result = new RecordImportResult(records.size());
        records.forEach(r -> {
            setImportedRecordProvenance(author, created, targetPhase, r);
            if (recordDao.exists(r.getUri())) {
                LOG.warn("Record {} already exists. Skipping it.", Utils.uriToString(r.getUri()));
                result.addError("Record " + Utils.uriToString(r.getUri()) + " already exists.");
            } else {
                recordDao.persist(r);
                result.addImportedRecord(r.getUri().toString());
            }
        });
        return result;
    }

    private void setImportedRecordProvenance(User currentUser, Date now, Optional<RecordPhase> targetPhase,
                                             PatientRecord record) {
        if (!currentUser.isAdmin()) {
            record.setAuthor(currentUser);
            record.setLastModifiedBy(currentUser);
            record.setInstitution(currentUser.getInstitution());
            record.setDateCreated(now);
            targetPhase.ifPresentOrElse(record::setPhase, () -> record.setPhase(RecordPhase.open));
        } else {
            targetPhase.ifPresent(record::setPhase);
            if (!userService.exists(record.getAuthor().getUri())) {
                throw new RecordAuthorNotFoundException("Author of record " + record + "not found during import.");
            }
        }
    }

    @Transactional
    @Override
    public void setPhase(Set<String> recordUris, RecordPhase targetPhase){
        for(String uri : recordUris){
            patientRecordDao.updateStatus(URI.create(uri), targetPhase);
        }
    }

    @Transactional
    @Override
    public RecordImportResult importRecords(List<PatientRecord> records, RecordPhase targetPhase) {
        Objects.requireNonNull(records);
        LOG.debug("Importing records to target phase '{}'.", targetPhase);
        return importRecordsImpl(records, Optional.ofNullable(targetPhase));
    }

    @Override
    public Page<RawRecord> exportRecords(RecordFilterParams filters, Pageable pageSpec){
        return patientRecordDao.findAllRecordsRaw(filters, pageSpec);
    }

    @Transactional(readOnly = true)
    @Override
    public Set<RecordPhase> findUsedRecordPhases() {
        return recordDao.findUsedRecordPhases();
    }
}
