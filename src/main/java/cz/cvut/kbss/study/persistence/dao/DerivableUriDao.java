package cz.cvut.kbss.study.persistence.dao;

import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.study.model.util.HasDerivableUri;

/**
 * Data access object for classes with derivable URI.
 * <p>
 * Makes sure that the URI is generated before the instance is persisted.
 *
 * @param <T> Entity type managed by this DAO
 */
abstract class DerivableUriDao<T extends HasDerivableUri> extends BaseDao<T> {

    protected DerivableUriDao(Class<T> type) {
        super(type);
    }

    /**
     * Generates URI and then calls persist.
     *
     * @param entity Entity to persist
     * @param em     Current entity manager
     */
    @Override
    protected void persist(T entity, EntityManager em) {
        assert entity != null;
        entity.generateUri();
        super.persist(entity, em);
    }
}
