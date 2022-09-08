package cz.cvut.kbss.study.util;

public final class Constants {

    private Constants() {
        throw new AssertionError();
    }

    public static final String BASE_URI = "http://onto.fel.cvut.cz/ontologies/record-manager/";

    /**
     * Language used by the persistence unit.
     */
    public static final String PU_LANGUAGE = "en";

    /**
     * Base URI for temporary contexts used by the form generation.
     */
    public static final String FORM_GEN_CONTEXT_BASE = "http://onto.fel.cvut.cz/ontologies/record-manager/formGen";

    /**
     * UTF-8 encoding identifier.
     */
    public static final String UTF_8_ENCODING = "UTF-8";

    /**
     * JSON-LD MIME type.
     */
    public static final String APPLICATION_JSON_LD_TYPE = "application/ld+json";

    /**
     * Prefix for basic authentication for the Authorization HTTP header.
     */
    public static final String BASIC_AUTHORIZATION_PREFIX = "Basic ";

    /**
     * Number of history actions fetched from database. Needs to be changes also in front-end.
     */
    public static final int ACTIONS_PER_PAGE = 25;

    /**
     * Path to directory containing queries used by the system.
     * <p>
     * The path should be relative to the classpath, so that queries from it can be loaded using {@link
     * ClassLoader#getResourceAsStream(String)}.
     */
    public static final String QUERY_DIRECTORY = "query";
}
