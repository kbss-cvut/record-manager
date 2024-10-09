package cz.cvut.kbss.study.model;

import cz.cvut.kbss.study.security.SecurityConstants;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    @Test
    void fromIriReturnsCorrectRole() {
        assertEquals(Role.administrator, Role.fromIri(Vocabulary.s_i_RM_ADMIN).get());
        assertEquals(Role.viewAllRecords, Role.fromIri(Vocabulary.s_i_view_all_records_role).get());
    }

    @Test
    void fromNameReturnsCorrectRole() {
        assertEquals(Role.administrator, Role.fromName(SecurityConstants.ROLE_ADMIN).get());
        assertEquals(Role.viewAllRecords, Role.fromName(SecurityConstants.viewAllRecords).get());
    }

    @Test
    void fromNameIsCaseInsensitive() {
        assertEquals(Role.administrator, Role.fromName(SecurityConstants.ROLE_ADMIN.toLowerCase()).get());
        assertEquals(Role.viewAllRecords, Role.fromName(SecurityConstants.viewAllRecords.toUpperCase()).get());
    }

    @Test
    void fromIriOrNameReturnsRoleByIri() {
        assertEquals(Role.administrator, Role.fromIriOrName(Vocabulary.s_i_RM_ADMIN).get());
        assertEquals(Role.viewAllRecords, Role.fromIriOrName(Vocabulary.s_i_view_all_records_role).get());
    }

    @Test
    void fromIriOrNameReturnsRoleByName() {
        assertEquals(Role.administrator, Role.fromIriOrName(SecurityConstants.ROLE_ADMIN).get());
        assertEquals(Role.viewAllRecords, Role.fromIriOrName(SecurityConstants.viewAllRecords).get());
    }

    @Test
    void fromIriOrNameIsCaseInsensitiveForName() {
        assertEquals(Role.administrator, Role.fromIriOrName(SecurityConstants.ROLE_ADMIN.toLowerCase()).get());
    }

}
