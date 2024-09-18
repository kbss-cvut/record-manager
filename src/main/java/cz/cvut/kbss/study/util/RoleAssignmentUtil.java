package cz.cvut.kbss.study.util;

import cz.cvut.kbss.study.model.Vocabulary;

import java.util.*;

public class RoleAssignmentUtil {

    public static final Set<String> OPERATOR_ADMIN_ROLES = new HashSet<>(
            Set.of(
                    Vocabulary.s_c_administrator,
                    Vocabulary.s_c_doctor,
                    Vocabulary.s_i_edit_users_role,
                    Vocabulary.s_i_publish_records_role,
                    Vocabulary.s_i_reject_records_role,
                    Vocabulary.s_i_view_organization_records_role,
                    Vocabulary.s_i_edit_organization_records_role,
                    Vocabulary.s_i_delete_organization_records_role,
                    Vocabulary.s_i_complete_records_role,
                    Vocabulary.s_i_import_codelists_role
            )
    );

    public static final Set<String> OPERATOR_USER_ROLES = new HashSet<>(
            Set.of(
                    Vocabulary.s_i_complete_records_role
            )
    );

    public static final Set<String> SUPPLIER_ADMIN_ROLES = new HashSet<>(
            Set.of(
                    Vocabulary.s_c_administrator,
                    Vocabulary.s_c_doctor,
                    Vocabulary.s_i_edit_users_role,
                    Vocabulary.s_i_reject_records_role,
                    Vocabulary.s_i_view_organization_records_role,
                    Vocabulary.s_i_edit_organization_records_role,
                    Vocabulary.s_i_delete_organization_records_role,
                    Vocabulary.s_i_complete_records_role,
                    Vocabulary.s_i_import_codelists_role,
                    Vocabulary.s_i_edit_all_records_role,
                    Vocabulary.s_i_delete_all_records_role,
                    Vocabulary.s_i_view_all_records_role
            )
    );

    public static final Set<String> SUPPLIER_USER_ROLES = new HashSet<>(
            Set.of(
                    Vocabulary.s_i_complete_records_role
            )
    );

    public static final Map<String, Set<String>> roleGroups = Map.of(
            Constants.OPERATOR_ADMIN, OPERATOR_ADMIN_ROLES,
            Constants.OPERATOR_USER, OPERATOR_USER_ROLES,
            Constants.SUPPLIER_ADMIN, SUPPLIER_ADMIN_ROLES,
            Constants.SUPPLIER_USER, SUPPLIER_USER_ROLES,
            Constants.EXTERNAL_USER, defaultRoles()
    );


    public static Set<String> assignRolesForGroup(String group) {
        if(group != null)
          return roleGroups.getOrDefault(group, defaultRoles());
        return defaultRoles();
    }

    /**
     * Default roles to be assigned if the group is not recognized.
     *
     * @return A set of default roles
     */
    private static Set<String> defaultRoles() {
        Set<String> defaultRoles = new HashSet<>();
        defaultRoles.add(Vocabulary.s_c_doctor);
        return defaultRoles;
    }
}
