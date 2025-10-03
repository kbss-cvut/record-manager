package cz.cvut.kbss.study.model;

import com.fasterxml.jackson.annotation.JsonValue;
import cz.cvut.kbss.jopa.model.annotations.Individual;
import cz.cvut.kbss.study.security.SecurityConstants;
import java.util.Optional;

import static cz.cvut.kbss.study.util.Utils.kebabToCamel;

public enum Role {

    @Individual(iri = Vocabulary.s_i_read_all_records_role)
    readAllRecords(SecurityConstants.readAllRecords, Vocabulary.s_i_read_all_records_role),

    @Individual(iri = Vocabulary.s_i_write_all_records_role)
    writeAllRecords(SecurityConstants.writeAllRecords, Vocabulary.s_i_write_all_records_role),

    @Individual(iri = Vocabulary.s_i_read_organization_records_role)
    readOrganizationRecords(SecurityConstants.readOrganizationRecords, Vocabulary.s_i_read_organization_records_role),

    @Individual(iri = Vocabulary.s_i_write_organization_records_role)
    writeOrganizationRecords(SecurityConstants.writeOrganizationRecords, Vocabulary.s_i_write_organization_records_role),

    @Individual(iri = Vocabulary.s_i_complete_records_role)
    completeRecords(SecurityConstants.completeRecords, Vocabulary.s_i_complete_records_role),

    @Individual(iri = Vocabulary.s_i_reject_records_role)
    rejectRecords(SecurityConstants.rejectRecords, Vocabulary.s_i_reject_records_role),

    @Individual(iri = Vocabulary.s_i_publish_records_role)
    publishRecords(SecurityConstants.publishRecords, Vocabulary.s_i_publish_records_role),

    @Individual(iri = Vocabulary.s_i_import_codelists_role)
    importCodelists(SecurityConstants.importCodelists, Vocabulary.s_i_import_codelists_role),

    @Individual(iri = Vocabulary.s_i_comment_record_questions_role)
    commentRecordQuestions(SecurityConstants.commentRecordQuestions, Vocabulary.s_i_comment_record_questions_role),

    @Individual(iri = Vocabulary.s_i_read_all_users_role)
    readAllUsers(SecurityConstants.readAllUsers, Vocabulary.s_i_read_all_users_role),

    @Individual(iri = Vocabulary.s_i_write_all_users_role)
    writeAllUsers(SecurityConstants.writeAllUsers, Vocabulary.s_i_write_all_users_role),

    @Individual(iri = Vocabulary.s_i_read_organization_users_role)
    readOrganizationUsers(SecurityConstants.readOrganizationUsers, Vocabulary.s_i_read_organization_users_role),

    @Individual(iri = Vocabulary.s_i_write_organization_users_role)
    writeOrganizationUsers(SecurityConstants.writeOrganizationUsers, Vocabulary.s_i_write_organization_users_role),

    @Individual(iri = Vocabulary.s_i_read_organization_role)
    readOrganization(SecurityConstants.readOrganization, Vocabulary.s_i_read_organization_role),

    @Individual(iri = Vocabulary.s_i_write_organization_role)
    writeOrganization(SecurityConstants.writeOrganization, Vocabulary.s_i_write_organization_role),

    @Individual(iri = Vocabulary.s_i_read_all_organizations_role)
    readAllOrganizations(SecurityConstants.readAllOrganizations, Vocabulary.s_i_read_all_organizations_role),

    @Individual(iri = Vocabulary.s_i_write_all_organizations_role)
    writeAllOrganizations(SecurityConstants.writeAllOrganizations, Vocabulary.s_i_write_all_organizations_role),

    @Individual(iri = Vocabulary.s_i_read_action_history_role)
    readActionHistory(SecurityConstants.readActionHistory, Vocabulary.s_i_read_action_history_role),

    @Individual(iri = Vocabulary.s_i_read_statistics_role)
    readStatistics(SecurityConstants.readStatistics, Vocabulary.s_i_read_statistics_role),

    @Individual(iri = Vocabulary.s_i_impersonate_role)
    impersonate(SecurityConstants.impersonate, Vocabulary.s_i_impersonate_role);

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
     * Retrieves a role based on its name.
     *
     * <p>This method normalizes the provided name by removing a possible "-role"
     * suffix and converting from kebab-case (e.g., "publish-records-role") to camelCase
     * (e.g., "publishRecords") before comparison. It then iterates over all available
     * roles and checks if any role's name matches the normalized name (case-insensitive).
     * If a match is found, the corresponding role is returned as an Optional.
     * If no match is found, an empty Optional is returned.</p>
     *
     * @param name the name of the role to retrieve (may include "-role" suffix)
     * @return an Optional containing the corresponding Role if found,
     *         or an empty Optional if no matching role exists
     */
    public static Optional<Role> fromName(String name) {
        String normalizedInput = name.replaceAll("-role$", "");
        String camelCaseInput = kebabToCamel(normalizedInput);

        for (Role r : values()) {
            if (r.roleName.equalsIgnoreCase(camelCaseInput)) {
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
