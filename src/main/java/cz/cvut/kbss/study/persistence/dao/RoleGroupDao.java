package cz.cvut.kbss.study.persistence.dao;

import cz.cvut.kbss.jopa.exceptions.NoResultException;
import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.study.exception.PersistenceException;
import cz.cvut.kbss.study.model.RoleGroup;
import cz.cvut.kbss.study.util.Constants;
import java.util.Objects;
import org.springframework.stereotype.Repository;
import java.net.URI;
import cz.cvut.kbss.study.model.Vocabulary;

@Repository
public class RoleGroupDao extends BaseDao<RoleGroup> {

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

    @Override
    public void persist(RoleGroup entity) {
        Objects.requireNonNull(entity);
        try {
            em.persist(entity);
        } catch (RuntimeException e) {
            LOG.error("Error when persisting entity.", e);
            throw new PersistenceException(e);
        }
    }

}
