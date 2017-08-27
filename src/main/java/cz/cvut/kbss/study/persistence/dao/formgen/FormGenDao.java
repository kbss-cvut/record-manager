package cz.cvut.kbss.study.persistence.dao.formgen;

import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.model.EntityManagerFactory;
import cz.cvut.kbss.jopa.model.descriptors.Descriptor;
import cz.cvut.kbss.jopa.model.descriptors.EntityDescriptor;
import cz.cvut.kbss.study.model.PatientRecord;
import cz.cvut.kbss.study.persistence.dao.util.QuestionSaver;
import cz.cvut.kbss.study.util.Constants;
import cz.cvut.kbss.study.util.IdentificationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.util.Objects;

@Repository
public class FormGenDao {

    @Autowired
    @Qualifier("formGen")
    private EntityManagerFactory emf;

    public URI persist(PatientRecord record) {
        Objects.requireNonNull(record);
        initRequiredFieldsIfNecessary(record);
        final EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            final Descriptor descriptor = new EntityDescriptor(generateContextUri());
            persistRelatedFieldsIfNecessary(record, em, descriptor);
            em.persist(record, descriptor);
            final QuestionSaver questionSaver = new QuestionSaver(descriptor);
            questionSaver.persistIfNecessary(record.getQuestion(), em);
            em.getTransaction().commit();
            return descriptor.getContext();
        } finally {
            em.close();
        }
    }

    private void initRequiredFieldsIfNecessary(PatientRecord record) {
        if (record.getKey() == null) {  // Happens for unpersisted records
            record.setKey(IdentificationUtils.generateKey());
        }
    }

    private void persistRelatedFieldsIfNecessary(PatientRecord record, EntityManager em, Descriptor descriptor) {
        em.persist(record.getAuthor(), descriptor);
        if (record.getInstitution() != null) {
            em.persist(record.getInstitution(), descriptor);
        }
    }

    private static URI generateContextUri() {
        return URI.create(Constants.FORM_GEN_CONTEXT_BASE + System.currentTimeMillis());
    }
}
