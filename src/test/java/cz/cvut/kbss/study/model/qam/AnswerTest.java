package cz.cvut.kbss.study.model.qam;

import cz.cvut.kbss.study.environment.generator.Generator;
import cz.cvut.kbss.study.model.Vocabulary;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AnswerTest {

    @Test
    public void copyConstructorsCopiesValuesAndTypesNoUri() {
        final Answer a = new Answer();
        a.setTextValue("Cough");
        a.setCodeValue(Generator.generateUri());
        a.getTypes().add(Vocabulary.ONTOLOGY_IRI_record_manager + "/infectious-disease/");

        final Answer res = new Answer(a);
        assertNull(res.getUri());
        assertEquals(a.getTextValue(), res.getTextValue());
        assertEquals(a.getCodeValue(), res.getCodeValue());
        assertEquals(a.getTypes(), res.getTypes());
    }
}