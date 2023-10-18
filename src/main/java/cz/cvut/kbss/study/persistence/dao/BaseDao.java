package cz.cvut.kbss.study.persistence.dao;

import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.study.exception.PersistenceException;
import cz.cvut.kbss.study.model.util.EntityToOwlClassMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Base implementation of the generic DAO.
 */
public abstract class BaseDao<T> implements GenericDao<T> {

    protected static final Logger LOG = LoggerFactory.getLogger(BaseDao.class);

    protected final Class<T> type;
    final URI typeUri;

    protected final EntityManager em;

    protected BaseDao(Class<T> type, EntityManager em) {
        this.type = type;
        this.typeUri = URI.create(EntityToOwlClassMapper.getOwlClassForEntity(type));
        this.em = em;
    }

    @Override
    public T find(URI uri) {
        Objects.requireNonNull(uri);
            return em.find(type, uri);
    }

    @Override
    public List<T> findAll() {
        return em.createNativeQuery("SELECT ?x WHERE { ?x a ?type . }", type).setParameter("type", typeUri)
                 .getResultList();
    }

    protected List<T> findAll(EntityManager em) {
        return em.createNativeQuery("SELECT ?x WHERE { ?x a ?type . }", type).setParameter("type", typeUri)
                 .getResultList();
    }

    @Override
    public void persist(T entity) {
        Objects.requireNonNull(entity);
        try {
            em.persist(entity);
        } catch (RuntimeException e) {
            LOG.error("Error when persisting entity.", e);
            throw new PersistenceException(e);
        }
    }

    @Override
    public void persist(Collection<T> entities) {
        Objects.requireNonNull(entities);
        if (entities.isEmpty()) {
            return;
        }
        entities.forEach(this::persist);
    }

    @Override
    public void update(T entity) {
        Objects.requireNonNull(entity);
        try {
            em.merge(entity);
        } catch (RuntimeException e) {
            LOG.error("Error when updating entity.", e);
            throw new PersistenceException(e);
        }
    }

    @Override
    public void remove(T entity) {
        Objects.requireNonNull(entity);
        try {
            final T toRemove = em.merge(entity);
            assert toRemove != null;
            em.remove(toRemove);
        } catch (RuntimeException e) {
            LOG.error("Error when removing entity.", e);
            throw new PersistenceException(e);
        }
    }

    @Override
    public void remove(Collection<T> entities) {
        Objects.requireNonNull(entities);
        if (entities.isEmpty()) {
            return;
        }
        entities.forEach(this::remove);
    }

    @Override
    public boolean exists(URI uri) {
        if (uri == null) {
            return false;
        }
        final String owlClass = type.getDeclaredAnnotation(OWLClass.class).iri();
        return em.createNativeQuery("ASK { ?individual a ?type . }", Boolean.class).setParameter("individual", uri)
                 .setParameter("type", URI.create(owlClass)).getSingleResult();
    }
}
