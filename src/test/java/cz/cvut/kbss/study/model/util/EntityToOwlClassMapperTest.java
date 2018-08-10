package cz.cvut.kbss.study.model.util;

import cz.cvut.kbss.study.model.PatientRecord;
import cz.cvut.kbss.study.model.Vocabulary;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EntityToOwlClassMapperTest {

    @Test
    public void getOWlClassForEntityExtractsOwlClassIriFromEntityClass() {
        assertEquals(Vocabulary.s_c_patient_record, EntityToOwlClassMapper.getOwlClassForEntity(PatientRecord.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getOwlClassForEntityThrowsIllegalArgumentForNonEntity() {
        EntityToOwlClassMapper.getOwlClassForEntity(Object.class);
    }
}