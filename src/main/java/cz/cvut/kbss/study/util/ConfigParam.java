package cz.cvut.kbss.study.util;

public enum ConfigParam {

    REPOSITORY_URL("repositoryUrl"),
    DRIVER("driver"),
    FORM_GEN_REPOSITORY_URL("formGenRepositoryUrl"),
    FORM_GEN_SERVICE_URL("formGenServiceUrl"),

    APP_CONTEXT("appContext"),

    E_USERNAME("eUsername"),
    E_PASSWORD("ePassword"),
    E_DISPLAY_NAME("eDisplayName"),

    E_PASSWORD_RESET_SUBJECT("ePasswordResetSubject"),
    E_PASSWORD_RESET_CONTENT("ePasswordResetContent"),

    E_INVITATION_SUBJECT("eInvitationSubject"),
    E_INVITATION_CONTENT("eInvitationContent"),

    E_PASSWORD_CHANGE_SUBJECT("ePasswordChangeSubject"),
    E_PASSWORD_CHANGE_CONTENT("ePasswordChangeContent"),

    E_PROFILE_UPDATE_SUBJECT("eProfileUpdateSubject"),
    E_PROFILE_UPDATE_CONTENT("eProfileUpdateContent");

    private final String name;

    ConfigParam(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
