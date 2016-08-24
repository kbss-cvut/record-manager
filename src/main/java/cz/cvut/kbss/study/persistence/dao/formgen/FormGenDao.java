package cz.cvut.kbss.study.persistence.dao.formgen;

import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.model.EntityManagerFactory;
import cz.cvut.kbss.jopa.model.descriptors.Descriptor;
import cz.cvut.kbss.jopa.model.descriptors.EntityDescriptor;
import cz.cvut.kbss.study.model.Clinic;
import cz.cvut.kbss.study.model.PatientRecord;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.persistence.dao.util.QuestionSaver;
import cz.cvut.kbss.study.util.Constants;
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
        final EntityManager em = emf.createEntityManager();
        try {
            final Descriptor descriptor = new EntityDescriptor(generateContextUri());
            persistRelatedFieldsIfNecessary(record, em);    // Author and clinic can be in the default context
            em.persist(record, descriptor);
            final QuestionSaver questionSaver = new QuestionSaver(descriptor);
            questionSaver.persistIfNecessary(record.getQuestion(), em);
            return descriptor.getContext();
        } finally {
            em.close();
        }
    }

    private void persistRelatedFieldsIfNecessary(PatientRecord record, EntityManager em) {
        if (em.find(User.class, record.getAuthor().getUri()) == null) {
            em.persist(record.getAuthor());
        }
        if (record.getClinic() != null && em.find(Clinic.class, record.getClinic().getUri()) == null) {
            em.persist(record.getClinic());
        }
    }

    private static URI generateContextUri() {
        return URI.create(Constants.FORM_GEN_CONTEXT_BASE + System.currentTimeMillis());
    }
}
