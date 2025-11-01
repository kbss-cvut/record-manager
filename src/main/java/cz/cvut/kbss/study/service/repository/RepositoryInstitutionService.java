package cz.cvut.kbss.study.service.repository;

import cz.cvut.kbss.study.exception.ValidationException;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.persistence.dao.InstitutionDao;
import cz.cvut.kbss.study.persistence.dao.OwlKeySupportingDao;
import cz.cvut.kbss.study.persistence.dao.RecordDao;
import cz.cvut.kbss.study.persistence.dao.UserDao;
import cz.cvut.kbss.study.service.InstitutionService;
import cz.cvut.kbss.study.util.Validator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RepositoryInstitutionService extends KeySupportingRepositoryService<Institution> implements InstitutionService {

    private final InstitutionDao institutionDao;

    private final UserDao userDao;

    private final RecordDao recordDao;

    public RepositoryInstitutionService(InstitutionDao institutionDao,
                                        UserDao userDao,
                                        RecordDao recordDao) {
        this.institutionDao = institutionDao;
        this.userDao = userDao;
        this.recordDao = recordDao;
    }

    @Override
    protected OwlKeySupportingDao<Institution> getPrimaryDao() {
        return institutionDao;
    }

    @Transactional(readOnly = true)
    @Override
    public Institution findByName(String name) {
        return institutionDao.findByName(name);
    }

    @Override
    protected void preRemove(Institution instance) {
        if (!userDao.findByInstitution(instance).isEmpty() || !recordDao.findByInstitution(instance).isEmpty()) {
            throw new ValidationException(
                    "error.institution.remove.institutionWithMembersOrRecordsCannotBeDeleted",
                    "Institution with members or records cannot be deleted.");
        }
    }

    @Override
    protected void prePersist(Institution instance) {
        try {
            if (instance.getEmailAddress() != null && !instance.getEmailAddress().equals("")) {
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
