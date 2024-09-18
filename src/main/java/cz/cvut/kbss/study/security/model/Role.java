package cz.cvut.kbss.study.security.model;

import cz.cvut.kbss.study.model.Vocabulary;
import cz.cvut.kbss.study.security.SecurityConstants;

import java.util.Optional;
import java.util.stream.Stream;

public enum Role {
    USER(SecurityConstants.ROLE_USER, Vocabulary.s_c_doctor),
    ADMIN(SecurityConstants.ROLE_ADMIN, Vocabulary.s_c_administrator),
    COMPLETE_RECORDS(SecurityConstants.ROLE_COMPLETE_RECORDS, Vocabulary.s_i_complete_records_role),
    DELETE_ALL_RECORDS(SecurityConstants.ROLE_DELETE_ALL_RECORDS, Vocabulary.s_i_delete_all_records_role),
    DELETE_ORGANIZATIONS_RECORDS(SecurityConstants.ROLE_DELETE_ORGANIZATION_RECORDS, Vocabulary.s_i_delete_organization_records_role),
    EDIT_ALL_RECORDS(SecurityConstants.ROLE_EDIT_ALL_RECORDS, Vocabulary.s_i_edit_all_records_role),
    EDIT_ORGANIZATIONS_RECORDS(SecurityConstants.ROLE_EDIT_ORGANIZATION_RECORDS, Vocabulary.s_i_edit_organization_records_role),
    EDIT_USERS(SecurityConstants.ROLE_EDIT_USERS, Vocabulary.s_i_edit_users_role),
    IMPORT_CODELISTS(SecurityConstants.ROLE_IMPORT_CODELISTS, Vocabulary.s_i_import_codelists_role),
    PUBLISH_RECORDS(SecurityConstants.ROLE_PUBLISH_RECORDS, Vocabulary.s_i_publish_records_role),
    REJECT_RECORDS(SecurityConstants.ROLE_REJECT_RECORDS, Vocabulary.s_i_reject_records_role),
    VIEW_ALL_RECORDS(SecurityConstants.ROLE_VIEW_ALL_RECORDS, Vocabulary.s_i_view_all_records_role),
    VIEW_ORGANIZATIONS_RECORDS(SecurityConstants.ROLE_VIEW_ORGANIZATION_RECORDS, Vocabulary.s_i_view_organization_records_role);

    private final String name;
    private final String type;

    Role(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public static Optional<Role> forType(String type) {
        return Stream.of(Role.values()).filter(r -> r.type.equals(type)).findAny();
    }

    public static Optional<Role> forName(String name) {
        return Stream.of(Role.values()).filter(r -> r.name.equals(name)).findAny();
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}
