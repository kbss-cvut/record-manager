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

    public static final String ROLE_COMPLETE_RECORDS = "rm_complete_records";
    public static final String ROLE_DELETE_ALL_RECORDS = "rm_delete_all_records";
    public static final String ROLE_DELETE_ORGANIZATION_RECORDS = "rm_delete_organization_records";
    public static final String ROLE_EDIT_ALL_RECORDS = "rm_edit_all_records";
    public static final String ROLE_EDIT_ORGANIZATION_RECORDS = "rm_edit_organization_records";
    public static final String ROLE_EDIT_USERS = "rm_edit_users";
    public static final String ROLE_IMPORT_CODELISTS = "rm_import_codelists";
    public static final String ROLE_PUBLISH_RECORDS = "rm_publish_records";
    public static final String ROLE_REJECT_RECORDS = "rm_reject_records";
    public static final String ROLE_VIEW_ALL_RECORDS = "rm_view_all_records";
    public static final String ROLE_VIEW_ORGANIZATION_RECORDS = "rm_view_organization_records";


}
