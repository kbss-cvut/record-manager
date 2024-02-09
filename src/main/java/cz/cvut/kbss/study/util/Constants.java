package cz.cvut.kbss.study.util;

public final class Constants {

    private Constants() {
        throw new AssertionError();
    }

    public static final String REST_API_REFIX = "/rest";

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
     * JSON-LD MIME type.
     */
    public static final String APPLICATION_JSON_LD_TYPE = "application/ld+json";

    /**
     * Number of history actions fetched from database. Needs to be changes also in front-end.
     */
    public static final int DEFAULT_PAGE_SIZE = 25;

    /**
     * Path to directory containing queries used by the system.
     * <p>
     * The path should be relative to the classpath, so that queries from it can be loaded using {@link
     * ClassLoader#getResourceAsStream(String)}.
     */
    public static final String QUERY_DIRECTORY = "query";

    /**
     * Name of the request parameter specifying page number.
     */
    public static final String PAGE_PARAM = "page";

    /**
     * Name of the request parameter specifying page size.
     */
    public static final String PAGE_SIZE_PARAM = "size";

    /**
     * Name of the request parameter specifying sorting.
     */
    public static final String SORT_PARAM = "sort";
}
