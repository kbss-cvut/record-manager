package cz.cvut.kbss.study.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    @Test
    void fromIriReturnsCorrectRole() {
        assertEquals(Role.administrator, Role.fromIri(Vocabulary.s_i_administrator));
        assertEquals(Role.viewAllRecords, Role.fromIri(Vocabulary.s_i_view_all_records_role));
    }

    @Test
    void fromIriThrowsExceptionForUnknownIri() {
        String unknownIri = "unknown_iri";
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Role.fromIri(unknownIri);
        });
        assertEquals("Unknown role identifier '" + unknownIri + "'.", exception.getMessage());
    }


    @Test
    void fromNameReturnsCorrectRole() {
        assertEquals(Role.administrator, Role.fromName("administrator"));
        assertEquals(Role.viewAllRecords, Role.fromName("viewAllRecords"));
    }

    @Test
    void fromNameIsCaseInsensitive() {
        assertEquals(Role.administrator, Role.fromName("ADMINISTRATOR"));
        assertEquals(Role.viewAllRecords, Role.fromName("VIEWALLRECORDS"));
    }

    @Test
    void fromNameThrowsExceptionForUnknownName() {
        String unknownName = "unknown_role";
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Role.fromName(unknownName);
        });
        assertEquals("Unknown role '" + unknownName + "'.", exception.getMessage());
    }


    @Test
    void fromIriOrNameReturnsRoleByIri() {
        assertEquals(Role.administrator, Role.fromIriOrName(Vocabulary.s_i_administrator));
        assertEquals(Role.viewAllRecords, Role.fromIriOrName(Vocabulary.s_i_view_all_records_role));
    }

    @Test
    void fromIriOrNameReturnsRoleByName() {
        assertEquals(Role.administrator, Role.fromIriOrName("administrator"));
        assertEquals(Role.viewAllRecords, Role.fromIriOrName("viewAllRecords"));
    }

    @Test
    void fromIriOrNameIsCaseInsensitiveForName() {
        assertEquals(Role.administrator, Role.fromIriOrName("ADMINISTRATOR"));
    }

    @Test
    void fromIriOrNameThrowsExceptionForUnknownIdentifier() {
        String unknownIdentifier = "unknown_identifier";
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Role.fromIriOrName(unknownIdentifier);
        });
        assertEquals("Unknown role '" + unknownIdentifier + "'.", exception.getMessage());
    }
}
