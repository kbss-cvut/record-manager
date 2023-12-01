package cz.cvut.kbss.study.persistence.dao;

import cz.cvut.kbss.jopa.exceptions.NoResultException;
import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.model.descriptors.Descriptor;
import cz.cvut.kbss.jopa.model.descriptors.EntityDescriptor;
import cz.cvut.kbss.jopa.model.metamodel.EntityType;
import cz.cvut.kbss.study.dto.PatientRecordDto;
import cz.cvut.kbss.study.exception.PersistenceException;
import cz.cvut.kbss.study.exception.ValidationException;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.PatientRecord;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.model.Vocabulary;
import cz.cvut.kbss.study.persistence.dao.util.QuestionSaver;
import cz.cvut.kbss.study.util.Constants;
import cz.cvut.kbss.study.util.IdentificationUtils;
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
    public PatientRecord find(URI uri) {
        Objects.requireNonNull(uri);
        try {
            return em.find(PatientRecord.class, uri, getDescriptor(uri));
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public PatientRecord findByKey(String key) {
        Objects.requireNonNull(key);
        try {
            return em.createQuery("SELECT r FROM " + PatientRecord.class.getSimpleName() + " r WHERE r.key = :key",
                                  type)
                     .setParameter("key", key, Constants.PU_LANGUAGE)
                     .setDescriptor(getDescriptor(key)).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public void persist(PatientRecord entity) {
        Objects.requireNonNull(entity);
        entity.setKey(IdentificationUtils.generateKey());
        entity.setUri(generateRecordUriFromKey(entity.getKey()));
        try {
            final Descriptor descriptor = getDescriptor(entity.getUri());
            em.persist(entity, descriptor);
            final QuestionSaver questionSaver = new QuestionSaver(descriptor);
            questionSaver.persistIfNecessary(entity.getQuestion(), em);
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    private Descriptor getDescriptor(String recordKey) {
        return getDescriptor(generateRecordUriFromKey(recordKey));
    }

    private Descriptor getDescriptor(URI ctx) {
        final EntityDescriptor descriptor = new EntityDescriptor(ctx);
        final EntityType<PatientRecord> et = em.getMetamodel().entity(PatientRecord.class);
        descriptor.addAttributeContext(et.getAttribute("author"), null);
        descriptor.addAttributeContext(et.getAttribute("lastModifiedBy"), null);
        descriptor.addAttributeContext(et.getAttribute("institution"), null);
        return descriptor;
    }

    public static URI generateRecordUriFromKey(String recordKey) {
        return URI.create(Vocabulary.s_c_patient_record + "/" + Objects.requireNonNull(recordKey));
    }

    @Override
    public void update(PatientRecord entity) {
        Objects.requireNonNull(entity);
        final Descriptor descriptor = getDescriptor(entity.getUri());
        final PatientRecord orig = em.find(PatientRecord.class, entity.getUri(), descriptor);
        assert orig != null;
        orig.setQuestion(null);
        em.merge(entity, descriptor);
        // Evict cached instances loaded from the default context
        em.getEntityManagerFactory().getCache().evict(PatientRecord.class, entity.getUri(), null);
        em.getEntityManagerFactory().getCache().evict(PatientRecordDto.class, entity.getUri(), null);
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
        em.clear();
    }
}
