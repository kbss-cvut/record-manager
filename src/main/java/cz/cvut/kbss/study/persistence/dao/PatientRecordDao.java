package cz.cvut.kbss.study.persistence.dao;

import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.study.model.Clinic;
import cz.cvut.kbss.study.model.PatientRecord;
import cz.cvut.kbss.study.model.Vocabulary;
import cz.cvut.kbss.study.persistence.dao.util.QuestionSaver;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.util.List;
import java.util.Objects;

@Repository
public class PatientRecordDao extends OwlKeySupportingDao<PatientRecord> {

    public PatientRecordDao() {
        super(PatientRecord.class);
    }

    @Override
    protected void persist(PatientRecord entity, EntityManager em) {
        assert entity != null;
        super.persist(entity, em);
        final QuestionSaver questionSaver = new QuestionSaver();
        questionSaver.persistIfNecessary(entity.getQuestion(), em);
    }

    /**
     * Gets records of patients treated at the specified clinic.
     *
     * @param clinic The clinic to filter by
     * @return Records of matching patients
     */
    public List<PatientRecord> findByClinic(Clinic clinic) {
        Objects.requireNonNull(clinic);
        final EntityManager em = entityManager();
        try {
            return em.createNativeQuery("SELECT ?r WHERE { ?r a ?type ; ?treatedAt ?clinic . }", PatientRecord.class)
                     .setParameter("type", typeUri)
                     .setParameter("treatedAt", URI.create(Vocabulary.s_p_was_treated_at))
                     .setParameter("clinic", clinic.getUri()).getResultList();
        } finally {
            em.close();
        }
    }
}
