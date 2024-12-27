package cz.cvut.kbss.study.persistence.dao;

import cz.cvut.kbss.jopa.exceptions.NoResultException;
import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.model.descriptors.Descriptor;
import cz.cvut.kbss.jopa.model.descriptors.EntityDescriptor;
import cz.cvut.kbss.jopa.model.metamodel.EntityType;
import cz.cvut.kbss.study.exception.PersistenceException;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.model.Vocabulary;
import cz.cvut.kbss.study.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;

@Repository
public class UserDao extends DerivableUriDao<User> {

    protected static final Logger LOG = LoggerFactory.getLogger(UserDao.class);

    public UserDao(EntityManager em) {
        super(User.class, em);
    }

    private Descriptor getDescriptor(URI ctx){
        Descriptor descriptor =  new EntityDescriptor(ctx);
        EntityType<User> et = em.getMetamodel().entity(User.class);
        descriptor.addAttributeContext(et.getAttribute("institution"), null);
        return descriptor;
    }

    @Override
    public void persist(User entity) {
        Objects.requireNonNull(entity);
        try {
            entity.generateUri();
            Descriptor descriptor = getDescriptor(new URI(Vocabulary.s_c_Person));
            em.persist(entity, descriptor);
        } catch (RuntimeException | URISyntaxException e) {
            LOG.error("Error when persisting entity.", e);
            throw new PersistenceException(e);
        }
    }

    public User findByUsername(String username) {
        Objects.requireNonNull(username);
        try {
            return em.createNativeQuery("SELECT ?x WHERE { ?x ?hasUsername ?username . }", User.class)
                     .setParameter("hasUsername", URI.create(Vocabulary.s_p_accountName))
                     .setParameter("username", username, Constants.PU_LANGUAGE)
                     .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public User findByEmail(String email) {
        Objects.requireNonNull(email);
        final String normalizedEmail = email.trim().toLowerCase();
        try {
            return em.createNativeQuery(
                             "SELECT ?x WHERE { " +
                                     "?x ?hasEmail ?emailAddress . " +
                                     "FILTER(lcase(?emailAddress) = ?normalizedEmailAddress) }", User.class)
                     .setParameter("hasEmail", URI.create(Vocabulary.s_p_mbox))
                     .setParameter("normalizedEmailAddress", normalizedEmail, Constants.PU_LANGUAGE)
                     .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public User findByToken(String token) {
        Objects.requireNonNull(token);
        try {
            return em.createNativeQuery("SELECT ?x WHERE { ?x ?valid ?token . }", User.class)
                     .setParameter("valid", URI.create(Vocabulary.s_p_token))
                     .setParameter("token", token, Constants.PU_LANGUAGE)
                     .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Gets all users associated with the specified institution.
     *
     * @param institution Institution whose associates should be returned
     * @return List of matching users, possibly empty
     */
    public List<User> findByInstitution(Institution institution) {
        Objects.requireNonNull(institution);
        return em.createNativeQuery(
                         "SELECT ?x WHERE { ?x a ?type ; ?hasUsername ?username ; ?isMemberOf ?institution . } ORDER BY ?username",
                         User.class)
                 .setParameter("type", typeUri)
                 .setParameter("hasUsername", URI.create(Vocabulary.s_p_accountName))
                 .setParameter("isMemberOf", URI.create(Vocabulary.s_p_is_member_of))
                 .setParameter("institution", institution.getUri()).getResultList();
    }

    public int getNumberOfInvestigators() {
        return ((BigInteger) em.createNativeQuery(
                                       "SELECT (count(?p) as ?investigatorCount) WHERE { ?p a ?typeDoctor . MINUS {?p a ?typeAdmin}}")
                               .setParameter("typeDoctor", URI.create(Vocabulary.s_c_doctor))
                               .setParameter("typeAdmin", URI.create(Vocabulary.s_c_administrator)).getSingleResult()
        ).intValue();
    }
}
