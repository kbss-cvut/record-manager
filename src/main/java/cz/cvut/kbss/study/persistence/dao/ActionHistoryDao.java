package cz.cvut.kbss.study.persistence.dao;

import cz.cvut.kbss.jopa.exceptions.NoResultException;
import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.model.query.TypedQuery;
import cz.cvut.kbss.study.model.ActionHistory;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.model.Vocabulary;
import cz.cvut.kbss.study.util.Constants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.util.Objects;

@Repository
public class ActionHistoryDao extends OwlKeySupportingDao<ActionHistory> {

    public ActionHistoryDao(EntityManager em) {
        super(ActionHistory.class, em);
    }

    public ActionHistory findByKey(String key) {
        Objects.requireNonNull(key);
        try {
            ActionHistory action = em.createNativeQuery(
                                             "SELECT ?x WHERE { ?x ?hasKey ?key . }", ActionHistory.class)
                                     .setParameter("hasKey", URI.create(Vocabulary.s_p_key))
                                     .setParameter("key", key, Constants.PU_LANGUAGE)
                                     .getSingleResult();
            action.getPayload();
            return action;
        } catch (NoResultException e) {
            return null;
        }
    }

    public Page<ActionHistory> findAllWithParams(String typeFilter, User author, Pageable pageSpec) {
        String params;
        if (typeFilter == null && author == null) {
            params = " } ";
        } else if (typeFilter == null) {
            Objects.requireNonNull(author);
            params = "; ?hasOwner ?author } ";
        } else if (author == null) {
            Objects.requireNonNull(typeFilter);
            params = "; ?isType ?actionType . filter contains(?actionType, ?typeFilter) } ";
        } else {
            params = "; ?hasOwner ?author; ?isType ?actionType . filter contains(?actionType, ?typeFilter) } ";
        }
        TypedQuery<ActionHistory> q = em.createNativeQuery("SELECT ?r WHERE { ?r a ?type ; ?isCreated ?timestamp " +
                                                                   params + "ORDER BY DESC(?timestamp)",
                                                           ActionHistory.class)
                                        .setParameter("type", typeUri)
                                        .setParameter("isCreated", URI.create(Vocabulary.s_p_created));
        if (pageSpec.isPaged()) {
            q.setFirstResult((int) pageSpec.getOffset());
            q.setMaxResults(pageSpec.getPageSize());
        }
        if (author != null) {
            q.setParameter("hasOwner", URI.create(Vocabulary.s_p_has_owner))
             .setParameter("author", author.getUri());
        }
        if (typeFilter != null) {
            q.setParameter("typeFilter", typeFilter, Constants.PU_LANGUAGE)
             .setParameter("isType", URI.create(Vocabulary.s_p_action_type));
        }
        return new PageImpl<>(q.getResultList(), pageSpec, 0L);
    }
}
