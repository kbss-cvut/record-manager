package cz.cvut.kbss.study.rest.util;

/**
 * Types of HTTP pagination links.
 */
public enum HttpPaginationLink {
    NEXT("next"), PREVIOUS("prev"), FIRST("first"), LAST("last");

    private final String name;

    HttpPaginationLink(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
