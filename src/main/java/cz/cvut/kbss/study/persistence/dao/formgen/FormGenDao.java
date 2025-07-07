package cz.cvut.kbss.study.persistence.dao.formgen;

import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.model.EntityManagerFactory;
import cz.cvut.kbss.jopa.model.descriptors.Descriptor;
import cz.cvut.kbss.jopa.model.descriptors.EntityDescriptor;
import cz.cvut.kbss.jopa.model.metamodel.EntityType;
import cz.cvut.kbss.study.model.PatientRecord;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.persistence.dao.PatientRecordDao;
import cz.cvut.kbss.study.persistence.dao.util.QuestionSaver;
import cz.cvut.kbss.study.util.Constants;
import cz.cvut.kbss.study.util.IdentificationUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Repository
public class FormGenDao {

    private final EntityManagerFactory emf;

    public FormGenDao(@Qualifier("formGen") EntityManagerFactory emf) {
        this.emf = emf;
    }

    private static URI generateContextUri() {
        return URI.create(Constants.FORM_GEN_CONTEXT_BASE + System.currentTimeMillis());
    }

    public URI persist(PatientRecord record) {
        return persist(Collections.singletonList(record));
    }

    public URI persist(List<PatientRecord> records) {
        final EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();
            final Descriptor descriptor = new EntityDescriptor(generateContextUri());

            for (PatientRecord r : records) {
                Objects.requireNonNull(r);
                initRequiredFieldsIfNecessary(r);
                persistRelatedFieldsIfNecessary(r, em, descriptor);
                em.persist(r, descriptor);
                final QuestionSaver questionSaver = new QuestionSaver(descriptor);
                questionSaver.persistIfNecessary(r.getQuestion(), em);
            }

            em.getTransaction().commit();
            return descriptor.getSingleContext()
                    .orElseThrow(() -> new IllegalStateException("Generated context was not retrieved."));
        } finally {
            em.close();
        }
    }

    private void initRequiredFieldsIfNecessary(PatientRecord record) {
        if (record.getKey() == null) {  // Happens for unpersisted records
            record.setKey(IdentificationUtils.generateKey());
            record.setUri(PatientRecordDao.generateRecordUriFromKey(record.getKey()));
        }
    }

    private void persistRelatedFieldsIfNecessary(PatientRecord record, EntityManager em, Descriptor descriptor) {
        if(record.getAuthor().getRoleGroup() != null) {
            em.persist(record.getAuthor().getRoleGroup(), descriptor);
        }
        em.persist(record.getAuthor(), descriptor);
        if (record.getInstitution() != null) {
            em.persist(record.getInstitution(), descriptor);
        }
    }
}
