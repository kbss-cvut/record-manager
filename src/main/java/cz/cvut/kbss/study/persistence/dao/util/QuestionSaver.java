package cz.cvut.kbss.study.persistence.dao.util;

import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.model.descriptors.Descriptor;
import cz.cvut.kbss.study.model.qam.Question;

import java.net.URI;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Recursively persists questions, handling references to the same question instance (they have the same URI).
 * <p>
 * The questions can be persisted into the default context, or into a named context specified by a {@link Descriptor}.
 */
public class QuestionSaver {

    private final Descriptor descriptor;
    private final Set<URI> visited = new HashSet<>();

    public QuestionSaver(Descriptor descriptor) {
        this.descriptor = Objects.requireNonNull(descriptor);
    }

    public void persistIfNecessary(Question root, EntityManager em) {
        if (root == null) {
            return;
        }
        if (visited.contains(root.getUri())) {
            return;
        }
        em.persist(root, descriptor);
        visited.add(root.getUri());
        root.getSubQuestions().forEach(q -> this.persistSubQuestionIfNecessary(q, em));
    }

    private void persistSubQuestionIfNecessary(Question question, EntityManager em) {
        if (visited.contains(question.getUri())) {
            return;
        }
        em.persist(question, descriptor);
        visited.add(question.getUri());
        question.getSubQuestions().forEach(q -> persistSubQuestionIfNecessary(q, em));
    }
}
