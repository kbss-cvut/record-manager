package cz.cvut.kbss.study.persistence.dao;

import cz.cvut.kbss.jopa.exceptions.NoResultException;
import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.model.Vocabulary;
import cz.cvut.kbss.study.util.Constants;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.util.List;
import java.util.Objects;

@Repository
public class UserDao extends DerivableUriDao<User> {

    public UserDao() {
        super(User.class);
    }

    public User findByUsername(String username) {
        Objects.requireNonNull(username);
        final EntityManager em = entityManager();
        try {
            return em.createNativeQuery(
                    "SELECT ?x WHERE { ?x ?hasUsername ?username . }", User.class)
                     .setParameter("hasUsername", URI.create(Vocabulary.s_p_accountName))
                     .setParameter("username", username, Constants.PU_LANGUAGE)
                     .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
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
        final EntityManager em = entityManager();
        try {
            return em.createNativeQuery(
                    "SELECT ?x WHERE { ?x a ?type ; ?hasUsername ?username ; ?isMemberOf ?institution . } ORDER BY ?username",
                    User.class)
                     .setParameter("type", typeUri)
                     .setParameter("hasUsername", URI.create(Vocabulary.s_p_accountName))
                     .setParameter("isMemberOf", URI.create(Vocabulary.s_p_is_member_of))
                     .setParameter("institution", institution.getUri()).getResultList();
        } finally {
            em.close();
        }
    }
}
