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

    public static final String user = "ROLE_USER";

    public static final String administrator = "ROLE_ADMIN";

    public static final String impersonate = "impersonate";

    public static final String deleteAllRecords = "delete-all-records";

    public static final String viewAllRecords = "view-all-records";

    public static final String editAllRecords = "edit-all-records";

    public static final String deleteOrganizationRecords = "delete-organization-records";

    public static final String viewOrganizationRecords = "view-organization-records";

    public static final String editOrganizationRecords = "edit-organization-records";

    public static final String editUsers = "edit-users";

    public static final String completeRecords = "complete-records";

    public static final String rejectRecords = "reject-records";

    public static final String publishRecords = "publish-records";

    public static final String importCodelists = "import-codelists";

}
