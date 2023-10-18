package cz.cvut.kbss.study.persistence.dao;

import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.study.dto.PatientRecordDto;
import cz.cvut.kbss.study.exception.ValidationException;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.PatientRecord;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.model.Vocabulary;
import cz.cvut.kbss.study.persistence.dao.util.QuestionSaver;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.net.URI;
import java.util.List;
import java.util.Objects;

@Repository
public class PatientRecordDao extends OwlKeySupportingDao<PatientRecord> {

    public PatientRecordDao(EntityManager em) {
        super(PatientRecord.class, em);
    }

    @Override
    public void persist(PatientRecord entity) {
        super.persist(entity);
        final QuestionSaver questionSaver = new QuestionSaver();
        questionSaver.persistIfNecessary(entity.getQuestion(), em);
    }

    @Override
    public void update(PatientRecord entity) {
        Objects.requireNonNull(entity);
        final PatientRecord orig = em.find(PatientRecord.class, entity.getUri());
        assert orig != null;
        orig.setQuestion(null);
        em.merge(entity);
    }

    public List<PatientRecordDto> findAllRecords() {
        return em.createNativeQuery("SELECT ?x WHERE { ?x a ?type . }", PatientRecordDto.class)
                 .setParameter("type", typeUri)
                 .getResultList();
    }

    /**
     * Gets records of patients treated at the specified institution.
     *
     * @param institution The institution to filter by
     * @return Records of matching patients
     */
    public List<PatientRecordDto> findByInstitution(Institution institution) {
        Objects.requireNonNull(institution);
        return em.createNativeQuery("SELECT ?r WHERE { ?r a ?type ; ?treatedAt ?institution . }",
                                    PatientRecordDto.class)
                 .setParameter("type", typeUri)
                 .setParameter("treatedAt", URI.create(Vocabulary.s_p_was_treated_at))
                 .setParameter("institution", institution.getUri())
                 .getResultList();
    }

    /**
     * Gets records of patients created by specified author.
     *
     * @param author The author to filter by
     * @return Records of matching patients
     */
    public List<PatientRecord> findByAuthor(User author) {
        Objects.requireNonNull(author);
        return em.createNativeQuery("SELECT ?r WHERE { ?r a ?type ; ?createdBy ?author . }", PatientRecord.class)
                 .setParameter("type", typeUri)
                 .setParameter("createdBy", URI.create(Vocabulary.s_p_has_author))
                 .setParameter("author", author.getUri()).getResultList();
    }

    public int getNumberOfProcessedRecords() {
        return ((BigInteger) em.createNativeQuery(
                                       "SELECT (count(?p) as ?patientRecordsCount) WHERE { ?p a ?record . }")
                               .setParameter("record", URI.create(Vocabulary.s_c_patient_record))
                               .getSingleResult()
        ).intValue();
    }

    /**
     * Ensure that local name of provided record is unique within its organization.
     *
     * @param entity The local name to be checked for uniqueness
     */
    public void requireUniqueNonEmptyLocalName(PatientRecord entity) {
        Objects.requireNonNull(entity.getInstitution());
        if (entity.getLocalName() == null || entity.getLocalName().isEmpty()) {
            throw new ValidationException("error.record.localNameOfRecordIsEmpty",
                                          "Local name of record is empty for entity " + entity);
        }
        boolean unique = findByInstitution(entity.getInstitution()).stream()
                                                                   .filter(pr -> (entity.getFormTemplate() != null) && entity.getFormTemplate()
                                                                                                                             .equals(pr.getFormTemplate()))
                                                                   .filter(pr -> pr.getLocalName()
                                                                                   .equals(entity.getLocalName()))
                                                                   .allMatch(pr -> pr.getUri().equals(entity.getUri()));
        if (!unique) {
            throw new ValidationException("error.record.localNameOfRecordIsNotUnique",
                                          "Local name of record is not unique for entity " + entity);
        }
    }

}
