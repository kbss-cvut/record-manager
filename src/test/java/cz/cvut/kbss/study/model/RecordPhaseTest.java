package cz.cvut.kbss.study.model;

import cz.cvut.kbss.study.environment.generator.Generator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RecordPhaseTest {

    @Test
    void fromStringReturnsMatchingRecordPhase() {
        for (RecordPhase p : RecordPhase.values()) {
            assertEquals(p, RecordPhase.fromString(p.getIri()));
        }
    }

    @Test
    void fromStringThrowsIllegalArgumentForUnknownPhaseIri() {
        assertThrows(IllegalArgumentException.class, () -> RecordPhase.fromString(Generator.generateUri().toString()));
    }

    @Test
    void fromStringThrowsIllegalArgumentForNullArgument() {
        assertThrows(IllegalArgumentException.class, () -> RecordPhase.fromString(null));
    }
}