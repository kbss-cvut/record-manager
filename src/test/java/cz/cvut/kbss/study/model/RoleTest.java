package cz.cvut.kbss.study.model;

import cz.cvut.kbss.study.security.SecurityConstants;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    @Test
    void fromIriReturnsCorrectRole() {
        assertEquals(Role.readAllRecords, Role.fromIri(Vocabulary.s_i_read_all_records_role).get());
    }

    @Test
    void fromNameReturnsCorrectRole() {
        assertEquals(Role.readAllRecords, Role.fromName(SecurityConstants.readAllRecords).get());
    }

    @Test
    void fromNameIsCaseInsensitive() {
        assertEquals(Role.readAllRecords, Role.fromName(SecurityConstants.readAllRecords.toUpperCase()).get());
    }

    @Test
    void fromIriOrNameReturnsRoleByIri() {
        assertEquals(Role.readAllRecords, Role.fromIriOrName(Vocabulary.s_i_read_all_records_role).get());
    }

    @Test
    void fromIriOrNameReturnsRoleByName() {
        assertEquals(Role.readAllRecords, Role.fromIriOrName(SecurityConstants.readAllRecords).get());
    }

    @Test
    void fromIriOrNameIsCaseInsensitiveForName() {
        assertEquals(Role.readAllRecords, Role.fromIriOrName(SecurityConstants.readAllRecords.toLowerCase()).get());
    }

    @Test
    void fromNameHandlesKebabCaseWithSuffix() {
        String kebabName = "read-all-records-role";
        assertEquals(Role.readAllRecords, Role.fromName(kebabName).get());
    }

}
