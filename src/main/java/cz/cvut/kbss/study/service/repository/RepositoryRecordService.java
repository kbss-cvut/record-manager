package cz.cvut.kbss.study.service.repository;

import cz.cvut.kbss.study.dto.RecordDto;
import cz.cvut.kbss.study.dto.RecordImportResult;
import cz.cvut.kbss.study.exception.RecordAuthorNotFoundException;
import cz.cvut.kbss.study.model.Record;
import cz.cvut.kbss.study.model.RecordPhase;
import cz.cvut.kbss.study.model.Role;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.model.export.RawRecord;
import cz.cvut.kbss.study.persistence.dao.OwlKeySupportingDao;
import cz.cvut.kbss.study.persistence.dao.RecordDao;
import cz.cvut.kbss.study.persistence.dao.util.RecordFilterParams;
import cz.cvut.kbss.study.security.SecurityConstants;
import cz.cvut.kbss.study.service.RecordService;
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
public class RepositoryRecordService extends KeySupportingRepositoryService<Record>
        implements RecordService {

    private static final Logger LOG = LoggerFactory.getLogger(RepositoryRecordService.class);

    private final RecordDao recordDao;

    private final SecurityUtils securityUtils;

    private final UserService userService;

    public RepositoryRecordService(RecordDao recordDao, SecurityUtils securityUtils,
                                   UserService userService) {
        this.recordDao = recordDao;
        this.securityUtils = securityUtils;
        this.userService = userService;
    }

    @Override
    protected OwlKeySupportingDao<Record> getPrimaryDao() {
        return recordDao;
    }

    @Transactional(readOnly = true)
    @Override
    public Page<RecordDto> findAll(RecordFilterParams filters, Pageable pageSpec) {
        User currentUser = securityUtils.getCurrentUser();
        boolean hasReadAllRecords = currentUser.getRoleGroup().hasRole(Role.valueOf(SecurityConstants.readAllRecords));

        if(currentUser.getInstitution() == null && !hasReadAllRecords) {
            filters.setAuthor(currentUser.getUsername());
        }

        return recordDao.findAllRecords(filters, pageSpec);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Record> findAllFull(RecordFilterParams filters, Pageable pageSpec) {
        return recordDao.findAllRecordsFull(filters, pageSpec);
    }

    @Override
    protected void prePersist(Record instance) {
        final User author = securityUtils.getCurrentUser();
        instance.setAuthor(author);
        instance.setDateCreated(new Date());
        instance.setInstitution(author.getInstitution());
        recordDao.requireUniqueNonEmptyLocalName(instance);
    }

    @Override
    protected void preUpdate(Record instance) {
        if(instance.getPhase() != RecordPhase.rejected) {
            instance.setRejectReason(null);
        }
        instance.setLastModifiedBy(securityUtils.getCurrentUser());
        instance.setLastModified(new Date());
        recordDao.requireUniqueNonEmptyLocalName(instance);
    }

    @Transactional
    @Override
    public RecordImportResult importRecords(List<Record> records) {
        Objects.requireNonNull(records);
        LOG.debug("Importing records.");
        return importRecordsImpl(records, Optional.empty());
    }

    private RecordImportResult importRecordsImpl(List<Record> records, Optional<RecordPhase> targetPhase) {
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

    // TODO reconsider the logic for new roles
    private void setImportedRecordProvenance(User currentUser, Date now, Optional<RecordPhase> targetPhase,
                                             Record record) {

        if(currentUser.getRoleGroup().getRoles().containsAll(List.of(Role.readAllOrganizations, Role.readAllUsers, Role.rejectRecords, Role.completeRecords))){
            targetPhase.ifPresent(record::setPhase);
            if (!userService.exists(record.getAuthor().getUri())) {
                throw new RecordAuthorNotFoundException("Author of record " + record + " not found during import.");
            }
        }else{
            record.setAuthor(currentUser);
            record.setLastModifiedBy(currentUser);
            record.setInstitution(currentUser.getInstitution());
            record.setDateCreated(now);
            targetPhase.ifPresentOrElse(record::setPhase, () -> record.setPhase(RecordPhase.open));
        }
    }

    @Transactional
    @Override
    public void setPhase(Set<String> recordUris, RecordPhase targetPhase){
        for(String uri : recordUris){
            recordDao.updateStatus(URI.create(uri), targetPhase);
        }
    }

    @Transactional
    @Override
    public RecordImportResult importRecords(List<Record> records, RecordPhase targetPhase) {
        Objects.requireNonNull(records);
        LOG.debug("Importing records to target phase '{}'.", targetPhase);
        return importRecordsImpl(records, Optional.ofNullable(targetPhase));
    }

    @Override
    public Page<RawRecord> exportRecords(RecordFilterParams filters, Pageable pageSpec){
        return recordDao.findAllRecordsRaw(filters, pageSpec);
    }

    @Transactional(readOnly = true)
    @Override
    public Set<RecordPhase> findUsedRecordPhases() {
        return recordDao.findUsedRecordPhases();
    }
}
