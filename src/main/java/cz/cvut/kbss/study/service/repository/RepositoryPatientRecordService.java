package cz.cvut.kbss.study.service.repository;

import cz.cvut.kbss.study.model.Clinic;
import cz.cvut.kbss.study.model.PatientRecord;
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
    public List<PatientRecord> findByClinic(Clinic clinic) {
        return recordDao.findByClinic(clinic);
    }

    @Override
    protected void prePersist(PatientRecord instance) {
        instance.setAuthor(securityUtils.getCurrentUser());
        instance.setDateCreated(new Date());
        instance.setKey(IdentificationUtils.generateKey());
    }

    @Override
    protected void preUpdate(PatientRecord instance) {
        instance.setLastModifiedBy(securityUtils.getCurrentUser());
        instance.setLastModified(new Date());
    }
}
