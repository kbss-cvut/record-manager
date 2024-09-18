package cz.cvut.kbss.study.persistence.dao;

import cz.cvut.kbss.jopa.exceptions.NoResultException;
import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.study.model.RoleGroup;
import cz.cvut.kbss.study.util.Constants;
import org.springframework.stereotype.Repository;
import java.net.URI;
import cz.cvut.kbss.study.model.Vocabulary;

@Repository
public class RoleGroupDao extends DerivableUriDao<RoleGroup> {

    protected RoleGroupDao(EntityManager em) {
        super(RoleGroup.class, em);
    }

    public RoleGroup findByName(String name) {
        if (name == null) {
            return null;
        }
        try {
            return em.createNativeQuery("SELECT ?x WHERE { ?x ?hasName ?name . }", RoleGroup.class)
                     .setParameter("hasName", URI.create(Vocabulary.s_p_label))
                     .setParameter("name", name, Constants.PU_LANGUAGE).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

}
