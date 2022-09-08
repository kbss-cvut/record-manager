package cz.cvut.kbss.study.model.qam;

import cz.cvut.kbss.study.environment.generator.Generator;
import cz.cvut.kbss.study.model.Vocabulary;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class QuestionTest {

    @Test
    public void copyConstructorCopiesSubQuestions() {
        final Question q = new Question();
        q.setUri(Generator.generateUri());
        q.getTypes().add(Vocabulary.ONTOLOGY_IRI_record_manager + "/infectious-disease/");
        for (int i = 0; i < Generator.randomInt(10); i++) {
            final Question child = new Question();
            child.setUri(Generator.generateUri());
            q.getSubQuestions().add(child);
        }

        final Question res = new Question(q);
        assertNull(res.getUri());
        assertEquals(q.getSubQuestions().size(), res.getSubQuestions().size());
        assertEquals(q.getTypes(), res.getTypes());
    }

    @Test
    public void copyConstructorCopiesAnswers() {
        final Question q = new Question();
        for (int i = 0; i < Generator.randomInt(10); i++) {
            final Answer a = new Answer();
            a.setTextValue("Rhitinis");
            q.getAnswers().add(a);
        }

        final Question res = new Question(q);
        assertEquals(q.getAnswers().size(), res.getAnswers().size());
    }
}