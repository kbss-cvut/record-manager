package cz.cvut.kbss.study.service.repository;

import cz.cvut.kbss.study.model.Clinic;
import cz.cvut.kbss.study.persistence.dao.ClinicDao;
import cz.cvut.kbss.study.persistence.dao.OwlKeySupportingDao;
import cz.cvut.kbss.study.service.ClinicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RepositoryClinicService extends KeySupportingRepositoryService<Clinic> implements ClinicService {

    @Autowired
    private ClinicDao clinicDao;

    @Override
    protected OwlKeySupportingDao<Clinic> getPrimaryDao() {
        return clinicDao;
    }

    @Override
    public Clinic findByName(String name) {
        return clinicDao.findByName(name);
    }
}
