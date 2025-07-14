package cz.cvut.kbss.study.security;

public class SecurityConstants {

    private SecurityConstants() {
        throw new AssertionError();
    }

    public static final String SESSION_COOKIE_NAME = "FSM_JSESSIONID";

    public static final String REMEMBER_ME_COOKIE_NAME = "remember-me";

    public static final String CSRF_COOKIE_NAME = "CSRF-TOKEN";

    public static final String USERNAME_PARAM = "username";

    public static final String PASSWORD_PARAM = "password";

    public static final String SECURITY_CHECK_URI = "/j_spring_security_check";

    public static final String LOGOUT_URI = "/j_spring_security_logout";

    public static final String COOKIE_URI = "/";

    /**
     * Session timeout in seconds.
     */
    public static final int SESSION_TIMEOUT = 12 * 60 * 60;

    public static final String ROLE_USER = "ROLE_USER";

    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    public static final String impersonate = "rm-impersonate";

    public static final String deleteAllRecords = "rm-delete-all-records";

    public static final String viewAllRecords = "rm-view-all-records";

    public static final String editAllRecords = "rm-edit-all-records";

    public static final String deleteOrganizationRecords = "rm-delete-organization-records";

    public static final String viewOrganizationRecords = "rm-view-organization-records";

    public static final String editOrganizationRecords = "rm-edit-organization-records";

    public static final String adminUsers = "rm-admin-users";

    public static final String completeRecords = "rm-complete-records";

    public static final String rejectRecords = "rm-reject-records";

    public static final String publishRecords = "rm-publish-records";

    public static final String importCodelists = "rm-import-codelists";

    public static final String adminStatistics = "rm-admin-statistics";

    public static final String adminRoleGroups = "rm-admin-role-groups";

    public static final String adminActionHistory = "rm-admin-action-history";

    public static final String adminInstitution = "rm-admin-institution";

}
