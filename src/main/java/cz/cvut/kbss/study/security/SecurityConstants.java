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

    public static final String readAllRecords = "readAllRecords";

    public static final String writeAllRecords = "writeAllRecords";

    public static final String readOrganizationRecords = "readOrganizationRecords";

    public static final String writeOrganizationRecords = "writeOrganizationRecords";

    public static final String completeRecords = "completeRecords";

    public static final String rejectRecords = "rejectRecords";

    public static final String publishRecords = "publishRecords";

    public static final String importCodelists = "importCodelists";

    public static final String commentRecordQuestions = "commentRecordQuestions";

    public static final String impersonate = "impersonate";

    public static final String readAllUsers = "readAllUsers";

    public static final String writeAllUsers = "writeAllUsers";

    public static final String readOrganizationUsers = "readOrganizationUsers";

    public static final String writeOrganizationUsers = "writeOrganizationUsers";

    public static final String readOrganization = "readOrganization";

    public static final String writeOrganization = "writeOrganization";

    public static final String readAllOrganizations = "readAllOrganizations";

    public static final String writeAllOrganizations = "writeAllOrganizations";

    public static final String readActionHistory = "readActionHistory";

    public static final String readStatistics = "readStatistics";

}
