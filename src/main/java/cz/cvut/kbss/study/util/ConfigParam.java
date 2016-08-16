package cz.cvut.kbss.study.util;

public enum ConfigParam {

    REPOSITORY_URL("repositoryUrl"),
    DRIVER("driver"),
    FORM_GEN_REPOSITORY_URL("formGenRepositoryUrl"),
    FORM_GEN_SERVICE_URL("formGenServiceUrl");

    private final String name;

    ConfigParam(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
