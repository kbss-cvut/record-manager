package cz.cvut.kbss.study.persistence.dao;

import cz.cvut.kbss.jopa.exceptions.NoResultException;
import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.Vocabulary;
import cz.cvut.kbss.study.util.Constants;
import org.springframework.stereotype.Repository;

import java.net.URI;

@Repository
public class InstitutionDao extends OwlKeySupportingDao<Institution> {

    public InstitutionDao(EntityManager em) {
        super(Institution.class, em);
    }

    /**
     * Finds institution with the specified name.
     *
     * @param name Institution name
     * @return Institution or {@code null}
     */
    public Institution findByName(String name) {
        if (name == null) {
            return null;
        }
        try {
            return em.createNativeQuery("SELECT ?x WHERE { ?x ?hasName ?name . }", Institution.class)
                     .setParameter("hasName", URI.create(Vocabulary.s_p_label))
                     .setParameter("name", name, Constants.PU_LANGUAGE).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
