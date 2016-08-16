package cz.cvut.kbss.study.persistence.dao;

import cz.cvut.kbss.jopa.exceptions.NoResultException;
import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.study.model.Clinic;
import cz.cvut.kbss.study.model.Vocabulary;
import cz.cvut.kbss.study.util.Constants;
import org.springframework.stereotype.Repository;

import java.net.URI;

@Repository
public class ClinicDao extends OwlKeySupportingDao<Clinic> {

    public ClinicDao() {
        super(Clinic.class);
    }

    /**
     * Finds clinic with the specified name.
     *
     * @param name Clinic name
     * @return Clinic or {@code null}
     */
    public Clinic findByName(String name) {
        if (name == null) {
            return null;
        }
        final EntityManager em = entityManager();
        try {
            return em.createNativeQuery("SELECT ?x WHERE { ?x ?hasName ?name . }", Clinic.class)
                     .setParameter("hasName", URI.create(Vocabulary.s_p_label))
                     .setParameter("name", name, Constants.PU_LANGUAGE).getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }
}
