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

    public static final String user = "user";

    public static final String administrator = "administrator";

    public static final String impersonate = "impersonate";

    public static final String deleteAllRecords = "deleteAllRecords";

    public static final String viewAllRecords = "viewAllRecords";

    public static final String editAllRecords = "editAllRecords";

    public static final String deleteOrganizationRecords = "deleteOrganizationRecords";

    public static final String viewOrganizationRecords = "viewOrganizationRecords";

    public static final String editOrganizationRecords = "editOrganizationRecords";

    public static final String editUsers = "editUsers";

    public static final String completeRecords = "completeRecords";

    public static final String rejectRecords = "rejectRecords";

    public static final String publishRecords = "publishRecords";

    public static final String importCodelists = "importCodelists";

}
