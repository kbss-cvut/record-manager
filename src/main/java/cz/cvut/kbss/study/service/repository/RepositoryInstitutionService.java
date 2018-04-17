package cz.cvut.kbss.study.service.repository;

import cz.cvut.kbss.study.exception.ValidationException;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.persistence.dao.InstitutionDao;
import cz.cvut.kbss.study.persistence.dao.OwlKeySupportingDao;
import cz.cvut.kbss.study.persistence.dao.PatientRecordDao;
import cz.cvut.kbss.study.persistence.dao.UserDao;
import cz.cvut.kbss.study.service.InstitutionService;
import cz.cvut.kbss.study.util.Validator;
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
        if (!userDao.findByInstitution(instance).isEmpty() || !patientRecordDao.findByInstitution(instance).isEmpty()) {
            throw new ValidationException("Institution with members or patient records cannot be deleted.");
        }
    }

    @Override
    protected void prePersist(Institution instance) {
        try {
            if (!instance.getEmailAddress().equals("")) {
                Validator.validateEmail(instance.getEmailAddress());
            }
        } catch (IllegalStateException e) {
            throw new ValidationException(e.getMessage());
        }
    }

    @Override
    protected void preUpdate(Institution instance) {
        try {
            if (!instance.getEmailAddress().equals("")) {
                Validator.validateEmail(instance.getEmailAddress());
            }
        } catch (IllegalStateException e) {
            throw new ValidationException(e.getMessage());
        }
    }
}
