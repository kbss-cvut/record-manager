package cz.cvut.kbss.study.model;

import cz.cvut.kbss.study.environment.generator.Generator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RecordPhaseTest {

    @Test
    void fromIriReturnsMatchingRecordPhase() {
        for (RecordPhase p : RecordPhase.values()) {
            assertEquals(p, RecordPhase.fromIri(p.getIri()));
        }
    }

    @Test
    void fromIriThrowsIllegalArgumentForUnknownPhaseIri() {
        assertThrows(IllegalArgumentException.class, () -> RecordPhase.fromIri(Generator.generateUri().toString()));
    }

    @Test
    void fromIriThrowsIllegalArgumentForNullArgument() {
        assertThrows(IllegalArgumentException.class, () -> RecordPhase.fromIri(null));
    }

    @Test
    void fromNameReturnsMatchingRecordPhase() {
        for (RecordPhase p : RecordPhase.values()) {
            assertEquals(p, RecordPhase.fromName(p.name()));
        }
    }

    @Test
    void fromNameMatchesIgnoringCase() {
        for (RecordPhase p : RecordPhase.values()) {
            assertEquals(p, RecordPhase.fromName(p.name().toUpperCase()));
        }
    }

    @Test
    void fromNameThrowsIllegalArgumentForUnknownPhaseIri() {
        assertThrows(IllegalArgumentException.class, () -> RecordPhase.fromName("unknown"));
    }

    @Test
    void fromNameThrowsIllegalArgumentForNullArgument() {
        assertThrows(IllegalArgumentException.class, () -> RecordPhase.fromName(null));
    }

    @Test
    void fromNameOrIriMatchesPhaseByIriAndName() {
        for (RecordPhase p : RecordPhase.values()) {
            assertEquals(p, RecordPhase.fromIriOrName(p.getIri()));
            assertEquals(p, RecordPhase.fromIriOrName(p.name()));
        }
    }
}