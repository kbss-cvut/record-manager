package cz.cvut.kbss.study.model;

import com.fasterxml.jackson.annotation.JsonValue;
import cz.cvut.kbss.jopa.model.annotations.Individual;
import cz.cvut.kbss.study.security.SecurityConstants;
import java.util.Optional;

public enum Role {

    // TODO deprecated -- should be removed.
    @Individual(iri = Vocabulary.s_i_RM_ADMIN)
    administrator(SecurityConstants.ROLE_ADMIN, Vocabulary.s_i_RM_ADMIN),
    // TODO deprecated -- should be removed.
    @Individual(iri = Vocabulary.s_i_RM_USER)
    user(SecurityConstants.ROLE_USER, Vocabulary.s_i_RM_USER),

    @Individual(iri = Vocabulary.s_i_impersonate_role)
    impersonate(SecurityConstants.impersonate, Vocabulary.s_i_impersonate_role),

    @Individual(iri = Vocabulary.s_i_delete_all_records_role)
    deleteAllRecords(SecurityConstants.deleteAllRecords, Vocabulary.s_i_delete_all_records_role),

    @Individual(iri = Vocabulary.s_i_view_all_records_role)
    viewAllRecords(SecurityConstants.viewAllRecords, Vocabulary.s_i_view_all_records_role),

    @Individual(iri = Vocabulary.s_i_edit_all_records_role)
    editAllRecords(SecurityConstants.editAllRecords, Vocabulary.s_i_edit_all_records_role),

    @Individual(iri = Vocabulary.s_i_delete_organization_records_role)
    deleteOrganizationRecords(SecurityConstants.deleteOrganizationRecords, Vocabulary.s_i_delete_organization_records_role),

    @Individual(iri = Vocabulary.s_i_view_organization_records_role)
    viewOrganizationRecords(SecurityConstants.viewOrganizationRecords, Vocabulary.s_i_view_organization_records_role),

    @Individual(iri = Vocabulary.s_i_edit_organization_records_role)
    editOrganizationRecords(SecurityConstants.editOrganizationRecords, Vocabulary.s_i_edit_organization_records_role),

    @Individual(iri = Vocabulary.s_i_admin_users_role)
    adminUsers(SecurityConstants.adminUsers, Vocabulary.s_i_admin_users_role),

    @Individual(iri = Vocabulary.s_i_complete_records_role)
    completeRecords(SecurityConstants.completeRecords, Vocabulary.s_i_complete_records_role),

    @Individual(iri = Vocabulary.s_i_reject_records_role)
    rejectRecords(SecurityConstants.rejectRecords, Vocabulary.s_i_reject_records_role),

    @Individual(iri = Vocabulary.s_i_publish_records_role)
    publishRecords(SecurityConstants.publishRecords, Vocabulary.s_i_publish_records_role),

    @Individual(iri = Vocabulary.s_i_import_codelists_role)
    importCodelists(SecurityConstants.importCodelists, Vocabulary.s_i_import_codelists_role),

    @Individual(iri = Vocabulary.s_i_admin_institution_role)
    adminInstitution(SecurityConstants.adminInstitution, Vocabulary.s_i_admin_institution_role),

    @Individual(iri = Vocabulary.s_i_admin_statistics_role)
    adminStatistics(SecurityConstants.adminStatistics, Vocabulary.s_i_admin_statistics_role),

    @Individual(iri = Vocabulary.s_i_admin_role_groups_role)
    adminRoleGroups(SecurityConstants.adminRoleGroups, Vocabulary.s_i_admin_role_groups_role),


    @Individual(iri = Vocabulary.s_i_admin_action_history_role)
    adminActionHistory(SecurityConstants.adminActionHistory, Vocabulary.s_i_admin_action_history_role);

    private final String iri;

    public final String roleName;

    Role(String roleName, String iri) {
        this.iri = iri;
        this.roleName = roleName;
    }

    @JsonValue
    public String getRoleName() {
        return roleName;
    }

    public String getIri() {
        return iri;
    }

    /**
     * Retrieves a role based on its IRI.
     *
     * <p>This method iterates over all available roles and checks if any role's IRI
     * matches the provided IRI string. If a match is found, the corresponding role
     * is returned as an Optional. If no match is found, an empty Optional is returned.</p>
     *
     * @param iri the IRI of the role to retrieve
     * @return an Optional containing the corresponding Role if found,
     * or an empty Optional if no matching role exists
     */
    public static Optional<Role> fromIri(String iri) {
        for (Role r : values()) {
            if (r.getIri().equals(iri)) {
                return Optional.of(r);
            }
        }
        return Optional.empty();
    }

    /**
     * Retrieves a role based on its role name.
     *
     * <p>This method iterates over all available roles and checks if any role's
     * name matches the provided name string (case-insensitive). If a match is found,
     * the corresponding role is returned as an Optional. If no match is found,
     * an empty Optional is returned.</p>
     *
     * @param name the name of the role to retrieve
     * @return an Optional containing the corresponding Role if found,
     *         or an empty Optional if no matching role exists
     */
    public static Optional<Role> fromName(String name) {
        for (Role r : values()) {
            if (r.roleName.equalsIgnoreCase(name)) {
                return Optional.of(r);
            }
        }
        return Optional.empty();
    }

    /**
     * Retrieves a role based on either its IRI or role name.
     *
     * <p>This method first attempts to find a role using the provided identification string
     * as an IRI. If no role is found, it then attempts to find a role using the
     * identification string as a role name. The first successful match will be returned
     * as an Optional. If no match is found, an empty Optional is returned.</p>
     *
     * @param identification the IRI or role name of the role to retrieve
     * @return an Optional containing the corresponding Role if found,
     *         or an empty Optional if no matching role exists
     */
    public static Optional<Role> fromIriOrName(String identification) {
        Optional<Role> role = fromIri(identification);
        return role.isPresent() ? role : fromName(identification);
    }
}
