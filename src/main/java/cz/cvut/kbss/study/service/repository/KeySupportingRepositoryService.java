package cz.cvut.kbss.study.service.repository;


import cz.cvut.kbss.study.model.util.HasOwlKey;
import cz.cvut.kbss.study.persistence.dao.OwlKeySupportingDao;

/**
 * Implements the {@link #findByKey(String)} method for all services which support key-based identification.
 *
 * @param <T> Entity type supporting keys
 */
abstract class KeySupportingRepositoryService<T extends HasOwlKey> extends BaseRepositoryService<T> {

    @Override
    protected abstract OwlKeySupportingDao<T> getPrimaryDao();

    public T findByKey(String key) {
        return getPrimaryDao().findByKey(key);
    }
}
