package cz.cvut.kbss.study.util;

public enum ConfigParam {

    SECURITY_SAME_SITE("security.sameSite"),

    REPOSITORY_URL("repositoryUrl"),
    PERSISTENCE_DRIVER("persistenceDriver"),
    FORM_GEN_REPOSITORY_URL("formGenRepositoryUrl"),
    FORM_GEN_SERVICE_URL("formGenServiceUrl"),

    ON_UPDATE_RECORD_SERVICE_URL("onRecordUpdateServiceUrl"),
    ON_PUBLISH_RECORDS_SERVICE_URL("onPublishRecordsServiceUrl"),
    PUBLISH_RECORDS_SERVICE_SECRET("publishRecordsServiceSecret"),
    EXCHANGE_TOKEN_SERVICE_URL("exchangeTokenServiceUrl"),
    EXCEL_IMPORT_SERVICE_URL("excelImportServiceUrl"),

    APP_CONTEXT("appContext"),

    SMTP_HOST("smtp.host"),
    SMTP_PORT("smtp.port"),
    SMTP_USER("smtp.user"),
    SMTP_PASSWORD("smtp.password"),
    E_DISPLAY_NAME("email.displayName"),
    E_FROM_ADDRESS("email.from"),
    E_CC_ADDRESS_LIST("email.cc"),
    E_BCC_ADDRESS_LIST("email.bcc"),
    E_REPLY_TO_ADDRESS_LIST("email.replyTo"),

    E_PASSWORD_RESET_SUBJECT("email.passwordResetSubject"),
    E_PASSWORD_RESET_CONTENT("email.passwordResetContent"),

    E_INVITATION_SUBJECT("email.invitationSubject"),
    E_INVITATION_CONTENT("email.invitationContent"),

    E_PASSWORD_CHANGE_SUBJECT("email.passwordChangeSubject"),
    E_PASSWORD_CHANGE_CONTENT("email.passwordChangeContent"),

    E_PROFILE_UPDATE_SUBJECT("email.profileUpdateSubject"),
    E_PROFILE_UPDATE_CONTENT("email.profileUpdateContent"),

    SECURITY_PROVIDER("security.provider"),

    OIDC_ROLE_CLAIM("security.oidc.roleClaim"),

    CORS_ALLOWED_ORIGINS("security.cors.allowedOrigins"),

    RECORDS_ALLOWED_REJECT_REASON("records.allowedRejectReason");

    private final String name;

    ConfigParam(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
