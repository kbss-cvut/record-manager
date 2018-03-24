package cz.cvut.kbss.study.service.repository;

import cz.cvut.kbss.study.exception.ValidationException;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.persistence.dao.InstitutionDao;
import cz.cvut.kbss.study.persistence.dao.OwlKeySupportingDao;
import cz.cvut.kbss.study.persistence.dao.PatientRecordDao;
import cz.cvut.kbss.study.persistence.dao.UserDao;
import cz.cvut.kbss.study.service.InstitutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RepositoryInstitutionService extends KeySupportingRepositoryService<Institution> implements InstitutionService {

    @Autowired
    private InstitutionDao institutionDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private PatientRecordDao patientRecordDao;

    @Override
    protected OwlKeySupportingDao<Institution> getPrimaryDao() {
        return institutionDao;
    }

    @Override
    public Institution findByName(String name) {
        return institutionDao.findByName(name);
    }

    @Override
    protected void preRemove(Institution instance) {
        if (!patientRecordDao.findByInstitution(instance).isEmpty() || !userDao.findByInstitution(instance).isEmpty()) {
            throw new ValidationException("Institution with members or patient records cannot be deleted.");
        }
    }
}
