package cz.cvut.kbss.study.persistence.dao;

import cz.cvut.kbss.jopa.exceptions.NoResultException;
import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.study.model.Vocabulary;
import cz.cvut.kbss.study.model.util.HasOwlKey;
import cz.cvut.kbss.study.util.Constants;
import cz.cvut.kbss.study.util.IdentificationUtils;

import java.net.URI;
import java.util.Objects;

/**
 * DAO for entity classes which have an OWL key.
 *
 * @param <T> Entity type
 */
public abstract class OwlKeySupportingDao<T extends HasOwlKey> extends BaseDao<T> {

    protected OwlKeySupportingDao(Class<T> type, EntityManager em) {
        super(type, em);
    }

    /**
     * Generates key and then calls persist.
     *
     * @param entity The instance to persist
     */
    @Override
    public void persist(T entity) {
        Objects.requireNonNull(entity);
        entity.setKey(IdentificationUtils.generateKey());
        super.persist(entity);
    }

    /**
     * Finds entity instance by its unique key.
     *
     * @param key Instance key
     * @return Entity instance or {@code null} if no such matching exists
     */
    public T findByKey(String key) {
        Objects.requireNonNull(key);
        try {
            return em.createNativeQuery("SELECT ?x WHERE { ?x ?hasKey ?key ;" +
                                                "a ?type }", type)
                     .setParameter("hasKey", URI.create(Vocabulary.s_p_key))
                     .setParameter("key", key, Constants.PU_LANGUAGE).setParameter("type", typeUri).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
