package cz.cvut.kbss.study.model;

import com.fasterxml.jackson.annotation.JsonValue;
import cz.cvut.kbss.jopa.model.annotations.Individual;
import cz.cvut.kbss.study.security.SecurityConstants;

public enum Role {

    // TODO deprecated -- should be removed.
    @Individual(iri=Vocabulary.s_i_RM_ADMIN)
    administrator(SecurityConstants.administrator, Vocabulary.s_i_RM_ADMIN),
    // TODO deprecated -- should be removed.
    @Individual(iri = Vocabulary.s_i_RM_USER)
    user(SecurityConstants.user, Vocabulary.s_i_RM_USER),

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

    @Individual(iri = Vocabulary.s_i_edit_users_role)
    editUsers(SecurityConstants.editUsers, Vocabulary.s_i_edit_users_role),

    @Individual(iri = Vocabulary.s_i_complete_records_role)
    completeRecords(SecurityConstants.completeRecords, Vocabulary.s_i_complete_records_role),

    @Individual(iri = Vocabulary.s_i_reject_records_role)
    rejectRecords(SecurityConstants.rejectRecords, Vocabulary.s_i_reject_records_role),

    @Individual(iri = Vocabulary.s_i_publish_records_role)
    publishRecords(SecurityConstants.publishRecords ,Vocabulary.s_i_publish_records_role),

    @Individual(iri = Vocabulary.s_i_import_codelists_role)
    importCodelists(SecurityConstants.importCodelists, Vocabulary.s_i_import_codelists_role);

    private final String iri;

    public final String roleName;

    Role(String roleName, String iri) {
        this.iri = iri;
        this.roleName = roleName;
    }

    @JsonValue
    public String getRoleName(){
        return roleName;
    }

    public String getIri() {
        return iri;
    }

    /**
     * Returns {@link Role} with the specified IRI.
     *
     * @param iri role identifier
     * @return matching {@code Role}
     * @throws IllegalArgumentException When no matching role is found
     */
    public static Role fromIri(String iri) {
        for (Role r : values()) {
            if (r.getIri().equals(iri)) {
                return r;
            }
        }
        throw new IllegalArgumentException("Unknown role identifier '" + iri + "'.");
    }

    /**
     * Returns {@link Role} with the specified constant name.
     *
     * @param name role name
     * @return matching {@code Role}
     * @throws IllegalArgumentException When no matching role is found
     */
    public static Role fromName(String name) {
        for (Role r : values()) {
            if (r.roleName.equalsIgnoreCase(name)) {
                return r;
            }
        }
        throw new IllegalArgumentException("Unknown role '" + name + "'.");
    }

    /**
     * Returns a {@link Role} with the specified IRI or constant name.
     * <p>
     * This function first tries to find the enum constant by IRI. If it is not found, constant name matching is
     * attempted.
     *
     * @param identification Constant IRI or name to find match by
     * @return matching {@code Role}
     * @throws IllegalArgumentException When no matching role is found
     */
    public static Role fromIriOrName(String identification) {
        try {
            return fromIri(identification);
        } catch (IllegalArgumentException e) {
            return fromName(identification);
        }
    }
}
