package cz.cvut.kbss.study.service.repository;

import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.persistence.dao.InstitutionDao;
import cz.cvut.kbss.study.persistence.dao.OwlKeySupportingDao;
import cz.cvut.kbss.study.service.InstitutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RepositoryInstitutionService extends KeySupportingRepositoryService<Institution> implements InstitutionService {

    @Autowired
    private InstitutionDao institutionDao;

    @Override
    protected OwlKeySupportingDao<Institution> getPrimaryDao() {
        return institutionDao;
    }

    @Override
    public Institution findByName(String name) {
        return institutionDao.findByName(name);
    }
}
