package cz.cvut.kbss.study.model.util;

import cz.cvut.kbss.study.model.Record;
import cz.cvut.kbss.study.model.Vocabulary;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EntityToOwlClassMapperTest {

    @Test
    public void getOWlClassForEntityExtractsOwlClassIriFromEntityClass() {
        assertEquals(Vocabulary.s_c_record, EntityToOwlClassMapper.getOwlClassForEntity(Record.class));
    }

    @Test
    public void getOwlClassForEntityThrowsIllegalArgumentForNonEntity() {
        assertThrows(IllegalArgumentException.class, () -> EntityToOwlClassMapper.getOwlClassForEntity(Object.class));
    }
}